package kr.co.inter_musica.team.service;

import jakarta.transaction.Transactional;
import kr.co.inter_musica.exception.AppException;
import kr.co.inter_musica.exception.ErrorCode;
import kr.co.inter_musica.team.domain.JoinRequest;
import kr.co.inter_musica.team.domain.Slot;
import kr.co.inter_musica.team.domain.Team;
import kr.co.inter_musica.team.domain.TeamMember;
import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;
import kr.co.inter_musica.team.domain.enumm.TeamMemberStatus;
import kr.co.inter_musica.team.dto.AcceptResponse;
import kr.co.inter_musica.team.dto.JoinRequestResponse;
import kr.co.inter_musica.team.dto.JoinRequestStatusResponse;
import kr.co.inter_musica.team.dto.MyJoinRequestResponse;
import kr.co.inter_musica.team.repository.JoinRequestRepository;
import kr.co.inter_musica.team.repository.SlotRepository;
import kr.co.inter_musica.team.repository.TeamMemberRepository;
import kr.co.inter_musica.team.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class JoinRequestServiceImpl implements JoinRequestService {

    private final TeamRepository teamRepository;
    private final SlotRepository slotRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final TeamMemberRepository teamMemberRepository;

    public JoinRequestServiceImpl(
            TeamRepository teamRepository,
            SlotRepository slotRepository,
            JoinRequestRepository joinRequestRepository,
            TeamMemberRepository teamMemberRepository
    ) {
        this.teamRepository = teamRepository;
        this.slotRepository = slotRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    @Transactional
    public JoinRequestResponse apply(Long userId, Long teamId, Long slotId) {
        if (!teamRepository.existsById(teamId)) throw new AppException(ErrorCode.TEAM_NOT_FOUND);

        Slot slot = slotRepository.findByIdAndTeamId(slotId, teamId)
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));

        boolean exists = joinRequestRepository.existsBySlotIdAndApplicantUserIdAndStatus(
                slot.getId(), userId, JoinRequestStatus.APPLIED
        );
        if (exists) throw new AppException(ErrorCode.JOIN_REQUEST_ALREADY_APPLIED);

        JoinRequest jr = new JoinRequest();
        jr.setTeamId(teamId);
        jr.setSlotId(slotId);
        jr.setApplicantUserId(userId);
        jr.setStatus(JoinRequestStatus.APPLIED);
        jr.setCreatedAt(LocalDateTime.now());
        jr.setUpdatedAt(LocalDateTime.now());

        joinRequestRepository.save(jr);
        return toJoinRequestResponse(jr);
    }

    @Override
    @Transactional
    public JoinRequestStatusResponse cancel(Long userId, Long joinRequestId) {
        JoinRequest jr = joinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        // 다른 사용자가 등록을 취소할 수 없다
        if (!jr.getApplicantUserId().equals(userId)) {
            throw new AppException(ErrorCode.JOIN_REQUEST_CANNOT_CANCEL_FORBIDDEN);
        }

        // applied 상태가 아니라면 취소할 수 없다
        if (jr.getStatus() != JoinRequestStatus.APPLIED) {
            throw new AppException(ErrorCode.JOIN_REQUEST_CANNOT_CANCEL_NOT_APPLIED);
        }

        jr.setStatus(JoinRequestStatus.CANCELED);
        jr.setUpdatedAt(LocalDateTime.now());

        JoinRequestStatusResponse res = new JoinRequestStatusResponse();
        res.setJoinRequestId(jr.getId());
        res.setStatus(jr.getStatus());
        return res;
    }

    @Override
    @Transactional
    public AcceptResponse accept(Long leaderUserId, Long joinRequestId) {
        JoinRequest jr = joinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (jr.getStatus() != JoinRequestStatus.APPLIED) {
            throw new AppException(ErrorCode.JOIN_REQUEST_NOT_APPLIED);
        }

        Team team = teamRepository.findById(jr.getTeamId())
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.getLeaderUserId().equals(leaderUserId)) {
            throw new AppException(ErrorCode.TEAM_FORBIDDEN);
        }

        Slot slot = slotRepository.findByIdAndTeamId(jr.getSlotId(), jr.getTeamId())
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));

        int activeCount = teamMemberRepository.countBySlotIdAndStatus(slot.getId(), TeamMemberStatus.ACTIVE);
        if (activeCount >= slot.getCapacity()) {
            throw new AppException(ErrorCode.SLOT_CAPACITY_FULL);
        }

        // (옵션 정책) 동시에 1팀만 소속
        if (teamMemberRepository.existsByUserIdAndStatus(jr.getApplicantUserId(), TeamMemberStatus.ACTIVE)) {
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE_IN_ANOTHER_TEAM);
        }

        jr.setStatus(JoinRequestStatus.ACCEPTED);
        jr.setDecidedAt(LocalDateTime.now());
        jr.setDecidedByUserId(leaderUserId);
        jr.setUpdatedAt(LocalDateTime.now());

        TeamMember tm = new TeamMember();
        tm.setTeamId(jr.getTeamId());
        tm.setUserId(jr.getApplicantUserId());
        tm.setSlotId(jr.getSlotId());
        tm.setStatus(TeamMemberStatus.ACTIVE);
        tm.setJoinedAt(LocalDateTime.now());

        teamMemberRepository.save(tm);

        AcceptResponse res = new AcceptResponse();
        res.setJoinRequestId(jr.getId());
        res.setStatus(jr.getStatus());
        res.setTeamMemberId(tm.getId());
        return res;
    }

    @Override
    @Transactional
    public JoinRequestStatusResponse reject(Long leaderUserId, Long joinRequestId) {
        JoinRequest jr = joinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (jr.getStatus() != JoinRequestStatus.APPLIED) {
            throw new AppException(ErrorCode.JOIN_REQUEST_NOT_APPLIED);
        }

        Team team = teamRepository.findById(jr.getTeamId())
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.getLeaderUserId().equals(leaderUserId)) {
            throw new AppException(ErrorCode.TEAM_FORBIDDEN);
        }

        jr.setStatus(JoinRequestStatus.REJECTED);
        jr.setDecidedAt(LocalDateTime.now());
        jr.setDecidedByUserId(leaderUserId);
        jr.setUpdatedAt(LocalDateTime.now());

        JoinRequestStatusResponse res = new JoinRequestStatusResponse();
        res.setJoinRequestId(jr.getId());
        res.setStatus(jr.getStatus());
        return res;
    }

    @Override
    public List<MyJoinRequestResponse> getMyJoinRequests(Long userId, JoinRequestStatus status) {
        List<JoinRequest> list;
        if (status == null) {
            list = joinRequestRepository.findByApplicantUserIdOrderByCreatedAtDesc(userId);
        } else {
            list = joinRequestRepository.findByApplicantUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        }

        List<MyJoinRequestResponse> res = new ArrayList<MyJoinRequestResponse>();
        for (JoinRequest jr : list) {
            Team team = teamRepository.findById(jr.getTeamId())
                    .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));
            Slot slot = slotRepository.findById(jr.getSlotId())
                    .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));

            MyJoinRequestResponse item = new MyJoinRequestResponse();
            item.setJoinRequestId(jr.getId());
            item.setTeamId(team.getId());
            item.setTeamName(team.getName());
            item.setSlotId(slot.getId());
            item.setInstrument(slot.getInstrument());
            item.setStatus(jr.getStatus());
            item.setCreatedAt(jr.getCreatedAt().toString());
            res.add(item);
        }
        return res;
    }

    private JoinRequestResponse toJoinRequestResponse(JoinRequest jr) {
        JoinRequestResponse res = new JoinRequestResponse();
        res.setJoinRequestId(jr.getId());
        res.setTeamId(jr.getTeamId());
        res.setSlotId(jr.getSlotId());
        res.setApplicantUserId(jr.getApplicantUserId());
        res.setStatus(jr.getStatus());
        res.setCreatedAt(jr.getCreatedAt() != null ? jr.getCreatedAt().toString() : null);
        return res;
    }
}
