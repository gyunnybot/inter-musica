package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.joinrequest.JoinRequestPolicy;
import kr.co.inter_musica.domain.enums.JoinRequestStatus;
import kr.co.inter_musica.infrastructure.persistence.entity.*;
import kr.co.inter_musica.infrastructure.persistence.jpa.*;
import kr.co.inter_musica.domain.exception.ApiException;
import kr.co.inter_musica.domain.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JoinRequestService {

    private final TeamJpaRepository teamJpaRepository;
    private final PositionSlotJpaRepository positionSlotJpaRepository;
    private final JoinRequestJpaRepository joinRequestJpaRepository;
    private final ProfileJpaRepository profileJpaRepository;
    private final TeamMemberJpaRepository teamMemberJpaRepository;

    private final JoinRequestPolicy policy = new JoinRequestPolicy();

    @Autowired
    public JoinRequestService(TeamJpaRepository teamJpaRepository,
                              PositionSlotJpaRepository positionSlotJpaRepository,
                              JoinRequestJpaRepository joinRequestJpaRepository,
                              ProfileJpaRepository profileJpaRepository,
                              TeamMemberJpaRepository teamMemberJpaRepository) {

        this.teamJpaRepository = teamJpaRepository;
        this.positionSlotJpaRepository = positionSlotJpaRepository;
        this.joinRequestJpaRepository = joinRequestJpaRepository;
        this.profileJpaRepository = profileJpaRepository;
        this.teamMemberJpaRepository = teamMemberJpaRepository;

    }

    @Transactional
    public Long applyJoinRequest(long currentUserId, Long teamId, Long positionId) {
        TeamJpaEntity team = teamJpaRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다."));

        PositionSlotJpaEntity slot = positionSlotJpaRepository.findById(positionId)
                .orElseThrow(() -> new ApiException(ErrorCode.POSITION_NOT_FOUND, "포지션을 찾을 수 없습니다."));

        // 포지션 슬롯 검증
        if (!slot.getTeamId().equals(teamId)) {
            throw new ApiException(ErrorCode.POSITION_NOT_FOUND, "해당 팀에 속한 포지션이 아닙니다.");
        }

        // 팀장인지 여부 확인
        if (teamJpaRepository.existsByLeaderUserId(currentUserId)) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_FORBIDDEN, "팀장은 지원할 수 없습니다.");
        }

        // 프로필 필수 (사실 없는 에러)
        ProfileJpaEntity profile = profileJpaRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROFILE_NOT_FOUND, "프로필을 찾을 수 없습니다."));

        // region/instrument/level match
        policy.validateRegionMatch(profile.getRegion(), team.getPracticeRegion());
        policy.validateInstrumentMatch(profile.getInstrument(), slot.getInstrument());
        policy.validateLevel(profile.getLevel(), slot.getRequiredLevelMin());

        // 이미 팀 멤버라면 지원 금지
        if (teamMemberJpaRepository.existsByTeamIdAndUserId(teamId, currentUserId)) {
            throw new ApiException(ErrorCode.ALREADY_TEAM_MEMBER, "이미 팀 멤버입니다.");
        }

        // 지원 중복 방지
        if (joinRequestJpaRepository.existsByTeamIdAndPositionSlotIdAndApplicantUserIdAndStatus(
                teamId, positionId, currentUserId, JoinRequestStatus.APPLIED
        )) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_DUPLICATED, "이미 지원한 상태입니다.");
        }

        long acceptedCount = joinRequestJpaRepository.countByPositionSlotIdAndStatus(positionId, JoinRequestStatus.ACCEPTED);

        policy.ensureCapacityAvailable(acceptedCount, slot.getCapacity());

        JoinRequestJpaEntity joinRequestJpaEntity = new JoinRequestJpaEntity(teamId, positionId, currentUserId);
        joinRequestJpaRepository.save(joinRequestJpaEntity);

        return joinRequestJpaEntity.getId();
    }

    @Transactional
    public void cancelJoinRequest(long currentUserId, Long joinRequestId) {
        JoinRequestJpaEntity joinRequestJpaEntity = joinRequestJpaRepository.findById(joinRequestId)
                .orElseThrow(() -> new ApiException(ErrorCode.JOIN_REQUEST_NOT_FOUND, "지원 정보를 찾을 수 없습니다."));

        if (!joinRequestJpaEntity.getApplicantUserId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.JOIN_REQUEST_FORBIDDEN, "본인만 지원을 취소할 수 있습니다.");
        }

        policy.ensureCancelable(joinRequestJpaEntity.getStatus());

        joinRequestJpaEntity.setStatus(JoinRequestStatus.CANCELED);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestJpaEntity> getApplicantList(long currentUserId, Long teamId, Long positionId, JoinRequestStatus status) {
        TeamJpaEntity team = teamJpaRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다."));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.TEAM_FORBIDDEN, "팀장만 지원자 목록을 조회할 수 있습니다.");
        }

        PositionSlotJpaEntity slot = positionSlotJpaRepository.findById(positionId)
                .orElseThrow(() -> new ApiException(ErrorCode.POSITION_NOT_FOUND, "포지션을 찾을 수 없습니다."));

        if (!slot.getTeamId().equals(teamId)) {
            throw new ApiException(ErrorCode.POSITION_NOT_FOUND, "해당 팀에 속한 포지션이 아닙니다.");
        }

        if (status == null) {
            return joinRequestJpaRepository.findByTeamIdAndPositionSlotId(teamId, positionId);
        }

        return joinRequestJpaRepository.findByTeamIdAndPositionSlotIdAndStatus(teamId, positionId, status);
    }

    @Transactional
    public void acceptJoinRequest(long currentUserId, Long joinRequestId) {
        decideJoinRequest(currentUserId, joinRequestId, true);
    }

    @Transactional
    public void rejectJoinRequest(long currentUserId, Long joinRequestId) {
        decideJoinRequest(currentUserId, joinRequestId, false);
    }

    private void decideJoinRequest(long currentUserId, Long joinRequestId, boolean accept) {
        JoinRequestJpaEntity joinRequestJpaEntity = joinRequestJpaRepository.findById(joinRequestId)
                .orElseThrow(() -> new ApiException(ErrorCode.JOIN_REQUEST_NOT_FOUND, "지원 정보를 찾을 수 없습니다."));

        TeamJpaEntity team = teamJpaRepository.findById(joinRequestJpaEntity.getTeamId())
                .orElseThrow(() -> new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다."));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.TEAM_FORBIDDEN, "팀장만 수락/거절할 수 있습니다.");
        }

        policy.ensureDecidable(joinRequestJpaEntity.getStatus());

        if (!accept) {
            joinRequestJpaEntity.setStatus(JoinRequestStatus.REJECTED);

            return;
        }

        // 레이스 컨디션 체크 (For Update)
        PositionSlotJpaEntity slot = positionSlotJpaRepository.findByIdForUpdate(joinRequestJpaEntity.getPositionSlotId())
                .orElseThrow(() -> new ApiException(ErrorCode.POSITION_NOT_FOUND, "포지션을 찾을 수 없습니다."));

        long acceptedCount = joinRequestJpaRepository.countByPositionSlotIdAndStatus(slot.getId(), JoinRequestStatus.ACCEPTED);

        policy.ensureCapacityAvailable(acceptedCount, slot.getCapacity());

        joinRequestJpaEntity.setStatus(JoinRequestStatus.ACCEPTED);

        // 팀원 등록
        if (!teamMemberJpaRepository.existsByTeamIdAndUserId(joinRequestJpaEntity.getTeamId(), joinRequestJpaEntity.getApplicantUserId())) {
            teamMemberJpaRepository.save(new TeamMemberJpaEntity(
                    joinRequestJpaEntity.getTeamId(),
                    joinRequestJpaEntity.getApplicantUserId()
                )
            );
        }
    }
}
