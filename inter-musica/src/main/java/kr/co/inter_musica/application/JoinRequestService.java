package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.policy.JoinRequestPolicy;
import kr.co.inter_musica.domain.enums.JoinRequestStatus;
import kr.co.inter_musica.infrastructure.persistence.entity.*;
import kr.co.inter_musica.infrastructure.persistence.jpa.*;
import kr.co.inter_musica.presentation.exception.ApiException;
import kr.co.inter_musica.presentation.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JoinRequestService {

    private final TeamJpaRepository teamRepo;
    private final PositionSlotJpaRepository positionRepo;
    private final JoinRequestJpaRepository joinRepo;
    private final ProfileJpaRepository profileRepo;
    private final TeamMemberJpaRepository teamMemberRepo;

    private final JoinRequestPolicy policy = new JoinRequestPolicy();

    public JoinRequestService(TeamJpaRepository teamRepo,
                              PositionSlotJpaRepository positionRepo,
                              JoinRequestJpaRepository joinRepo,
                              ProfileJpaRepository profileRepo,
                              TeamMemberJpaRepository teamMemberRepo) {
        this.teamRepo = teamRepo;
        this.positionRepo = positionRepo;
        this.joinRepo = joinRepo;
        this.profileRepo = profileRepo;
        this.teamMemberRepo = teamMemberRepo;
    }

    @Transactional
    public Long apply(long currentUserId, Long teamId, Long positionId) {
        TeamJpaEntity team = teamRepo.findById(teamId)
                .orElseThrow(() -> new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다."));

        PositionSlotJpaEntity slot = positionRepo.findById(positionId)
                .orElseThrow(() -> new ApiException(ErrorCode.POSITION_NOT_FOUND, "포지션을 찾을 수 없습니다."));

        // 소속 검증
        if (!slot.getTeamId().equals(teamId)) {
            throw new ApiException(ErrorCode.POSITION_NOT_FOUND, "해당 팀에 속한 포지션이 아닙니다.");
        }

        // (설계서 힌트) 리더는 지원 불가 정책을 원한다면: "어딘가의 팀장이라면 지원 불가"
        if (teamRepo.existsByLeaderUserId(currentUserId)) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_FORBIDDEN, "팀장은 지원할 수 없습니다.");
        }

        // 프로필 필수
        ProfileJpaEntity profile = profileRepo.findById(currentUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROFILE_NOT_FOUND, "프로필을 찾을 수 없습니다."));

        // region match
        policy.validateRegionMatch(profile.getRegion(), team.getPracticeRegion());

        // instrument/level match
        policy.validateInstrumentMatch(profile.getInstrument(), slot.getInstrument());
        policy.validateLevel(profile.getLevel(), slot.getRequiredLevelMin());

        // 이미 팀 멤버면 지원 금지
        if (teamMemberRepo.existsByTeamIdAndUserId(teamId, currentUserId)) {
            throw new ApiException(ErrorCode.ALREADY_TEAM_MEMBER, "이미 팀 멤버입니다.");
        }

        // APPLIED 중복 방지
        if (joinRepo.existsByTeamIdAndPositionSlotIdAndApplicantUserIdAndStatus(
                teamId, positionId, currentUserId, JoinRequestStatus.APPLIED
        )) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_DUPLICATED, "이미 지원한 상태입니다.");
        }

        // capacity 정책: apply 시점에 막기(권장)
        long acceptedCount = joinRepo.countByPositionSlotIdAndStatus(positionId, JoinRequestStatus.ACCEPTED);
        policy.ensureCapacityAvailable(acceptedCount, slot.getCapacity());

        JoinRequestJpaEntity jr = new JoinRequestJpaEntity(teamId, positionId, currentUserId);
        joinRepo.save(jr);
        return jr.getId();
    }

    @Transactional
    public void cancel(long currentUserId, Long joinRequestId) {
        JoinRequestJpaEntity jr = joinRepo.findById(joinRequestId)
                .orElseThrow(() -> new ApiException(ErrorCode.JOIN_REQUEST_NOT_FOUND, "지원 정보를 찾을 수 없습니다."));

        if (!jr.getApplicantUserId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_FORBIDDEN, "본인만 지원을 취소할 수 있습니다.");
        }

        policy.ensureCancelable(jr.getStatus());
        jr.setStatus(JoinRequestStatus.CANCELED);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestJpaEntity> listApplicants(long currentUserId, Long teamId, Long positionId, JoinRequestStatus status) {
        TeamJpaEntity team = teamRepo.findById(teamId)
                .orElseThrow(() -> new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다."));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.TEAM_FORBIDDEN, "팀장만 지원자 목록을 조회할 수 있습니다.");
        }

        PositionSlotJpaEntity slot = positionRepo.findById(positionId)
                .orElseThrow(() -> new ApiException(ErrorCode.POSITION_NOT_FOUND, "포지션을 찾을 수 없습니다."));

        if (!slot.getTeamId().equals(teamId)) {
            throw new ApiException(ErrorCode.POSITION_NOT_FOUND, "해당 팀에 속한 포지션이 아닙니다.");
        }

        if (status == null) {
            return joinRepo.findByTeamIdAndPositionSlotId(teamId, positionId);
        }
        return joinRepo.findByTeamIdAndPositionSlotIdAndStatus(teamId, positionId, status);
    }

    @Transactional
    public void accept(long currentUserId, Long joinRequestId) {
        decide(currentUserId, joinRequestId, true);
    }

    @Transactional
    public void reject(long currentUserId, Long joinRequestId) {
        decide(currentUserId, joinRequestId, false);
    }

    private void decide(long currentUserId, Long joinRequestId, boolean accept) {
        JoinRequestJpaEntity jr = joinRepo.findById(joinRequestId)
                .orElseThrow(() -> new ApiException(ErrorCode.JOIN_REQUEST_NOT_FOUND, "지원 정보를 찾을 수 없습니다."));

        TeamJpaEntity team = teamRepo.findById(jr.getTeamId())
                .orElseThrow(() -> new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다."));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.TEAM_FORBIDDEN, "팀장만 수락/거절할 수 있습니다.");
        }

        policy.ensureDecidable(jr.getStatus());

        if (!accept) {
            jr.setStatus(JoinRequestStatus.REJECTED);
            return;
        }

        // Accept: capacity 경쟁 조건을 조금이라도 줄이기 위해 slot을 FOR UPDATE로 잡음
        PositionSlotJpaEntity slot = positionRepo.findByIdForUpdate(jr.getPositionSlotId())
                .orElseThrow(() -> new ApiException(ErrorCode.POSITION_NOT_FOUND, "포지션을 찾을 수 없습니다."));

        long acceptedCount = joinRepo.countByPositionSlotIdAndStatus(slot.getId(), JoinRequestStatus.ACCEPTED);
        policy.ensureCapacityAvailable(acceptedCount, slot.getCapacity());

        jr.setStatus(JoinRequestStatus.ACCEPTED);

        // 멤버십 생성
        if (!teamMemberRepo.existsByTeamIdAndUserId(jr.getTeamId(), jr.getApplicantUserId())) {
            teamMemberRepo.save(new TeamMemberJpaEntity(jr.getTeamId(), jr.getApplicantUserId()));
        }
    }
}
