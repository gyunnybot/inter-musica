package kr.co.inter_musica.team.service;

import jakarta.transaction.Transactional;
import kr.co.inter_musica.exception.AppException;
import kr.co.inter_musica.exception.ErrorCode;
import kr.co.inter_musica.team.domain.JoinRequest;
import kr.co.inter_musica.team.domain.Slot;
import kr.co.inter_musica.team.domain.Team;
import kr.co.inter_musica.team.domain.enumm.JoinRequestStatus;
import kr.co.inter_musica.team.domain.enumm.TeamMemberStatus;
import kr.co.inter_musica.team.dto.*;
import kr.co.inter_musica.team.repository.JoinRequestRepository;
import kr.co.inter_musica.team.repository.SlotRepository;
import kr.co.inter_musica.team.repository.TeamMemberRepository;
import kr.co.inter_musica.team.repository.TeamRepository;
import kr.co.inter_musica.user.domain.Profile;
import kr.co.inter_musica.user.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SlotServiceImpl implements SlotService {

    private final TeamRepository teamRepository;
    private final SlotRepository slotRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProfileRepository profileRepository;

    public SlotServiceImpl(
            TeamRepository teamRepository,
            SlotRepository slotRepository,
            JoinRequestRepository joinRequestRepository,
            TeamMemberRepository teamMemberRepository,
            ProfileRepository profileRepository
    ) {
        this.teamRepository = teamRepository;
        this.slotRepository = slotRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public SlotResponse createSlot(Long leaderUserId, Long teamId, SlotCreateRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.getLeaderUserId().equals(leaderUserId)) {
            throw new AppException(ErrorCode.TEAM_FORBIDDEN);
        }

        Slot slot = new Slot();
        slot.setTeamId(teamId);
        slot.setInstrument(request.getInstrument());
        slot.setCapacity(request.getCapacity());
        slot.setRequiredLevelMin(request.getRequiredLevelMin());
        slotRepository.save(slot);

        return toSlotResponse(slot);
    }

    @Override
    @Transactional
    public SlotResponse updateSlot(Long leaderUserId, Long teamId, Long slotId, SlotUpdateRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));
        if (!team.getLeaderUserId().equals(leaderUserId)) throw new AppException(ErrorCode.TEAM_FORBIDDEN);

        Slot slot = slotRepository.findByIdAndTeamId(slotId, teamId)
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));

        if (request.getCapacity() != null) {
            int activeCount = teamMemberRepository.countBySlotIdAndStatus(slotId, TeamMemberStatus.ACTIVE);
            if (request.getCapacity().intValue() < activeCount) {
                throw new AppException(ErrorCode.SLOT_CAPACITY_TOO_SMALL_FOR_ACTIVE_MEMBERS);
            }
            slot.setCapacity(request.getCapacity());
        }
        if (request.getRequiredLevelMin() != null) {
            slot.setRequiredLevelMin(request.getRequiredLevelMin());
        }

        return toSlotResponse(slot);
    }

    @Override
    @Transactional
    public void deleteSlot(Long leaderUserId, Long teamId, Long slotId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));
        if (!team.getLeaderUserId().equals(leaderUserId)) throw new AppException(ErrorCode.TEAM_FORBIDDEN);

        Slot slot = slotRepository.findByIdAndTeamId(slotId, teamId)
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));

        boolean hasApplied = !joinRequestRepository.findBySlotIdAndStatus(slotId, JoinRequestStatus.APPLIED).isEmpty();
        int activeCount = teamMemberRepository.countBySlotIdAndStatus(slotId, TeamMemberStatus.ACTIVE);

        if (hasApplied || activeCount > 0) {
            throw new AppException(ErrorCode.SLOT_CANNOT_DELETE_HAS_PENDING_OR_ACTIVE);
        }

        slotRepository.delete(slot);
    }

    @Override
    public List<JoinRequestWithProfileResponse> getSlotJoinRequests(Long leaderUserId, Long teamId, Long slotId, JoinRequestStatus status) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));
        if (!team.getLeaderUserId().equals(leaderUserId)) throw new AppException(ErrorCode.TEAM_FORBIDDEN);

        Slot slot = slotRepository.findByIdAndTeamId(slotId, teamId)
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));

        JoinRequestStatus queryStatus = (status == null) ? JoinRequestStatus.APPLIED : status;
        List<JoinRequest> reqs = joinRequestRepository.findBySlotIdAndStatus(slot.getId(), queryStatus);

        List<JoinRequestWithProfileResponse> res = new ArrayList<JoinRequestWithProfileResponse>();
        for (JoinRequest jr : reqs) {
            Profile p = profileRepository.findById(jr.getApplicantUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

            MemberProfileResponse mp = new MemberProfileResponse();
            mp.setUserId(jr.getApplicantUserId());
            mp.setInstrument(p.getInstrument());
            mp.setLevel(p.getLevel());
            mp.setRegion(p.getRegion());

            JoinRequestWithProfileResponse item = new JoinRequestWithProfileResponse();
            item.setJoinRequestId(jr.getId());
            item.setApplicantUserId(jr.getApplicantUserId());
            item.setApplicantProfile(mp);
            item.setStatus(jr.getStatus());
            item.setCreatedAt(jr.getCreatedAt().toString());
            res.add(item);
        }
        return res;
    }

    private SlotResponse toSlotResponse(Slot slot) {
        SlotResponse res = new SlotResponse();
        res.setSlotId(slot.getId());
        res.setTeamId(slot.getTeamId());
        res.setInstrument(slot.getInstrument());
        res.setCapacity(slot.getCapacity());
        res.setRequiredLevelMin(slot.getRequiredLevelMin());
        return res;
    }
}
