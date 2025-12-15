package kr.co.inter_musica.team.service;

import jakarta.transaction.Transactional;
import kr.co.inter_musica.exception.AppException;
import kr.co.inter_musica.exception.ErrorCode;
import kr.co.inter_musica.team.domain.Slot;
import kr.co.inter_musica.team.domain.Team;
import kr.co.inter_musica.team.domain.TeamMember;
import kr.co.inter_musica.team.domain.enumm.Level;
import kr.co.inter_musica.team.domain.enumm.TeamMemberStatus;
import kr.co.inter_musica.team.dto.*;
import kr.co.inter_musica.team.repository.SlotRepository;
import kr.co.inter_musica.team.repository.TeamMemberRepository;
import kr.co.inter_musica.team.repository.TeamRepository;
import kr.co.inter_musica.user.domain.Profile;
import kr.co.inter_musica.user.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final SlotRepository slotRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProfileRepository profileRepository;

    public TeamServiceImpl(
            TeamRepository teamRepository,
            SlotRepository slotRepository,
            TeamMemberRepository teamMemberRepository,
            ProfileRepository profileRepository
    ) {
        this.teamRepository = teamRepository;
        this.slotRepository = slotRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public TeamResponse createTeam(Long leaderUserId, TeamCreateRequest request) {
        Team team = new Team();
        team.setLeaderUserId(leaderUserId);
        team.setName(request.getName());
        team.setIntro(request.getIntro());
        team.setGoal(request.getGoal());
        team.setPracticeInfo(request.getPracticeInfo());

        teamRepository.save(team);
        return toTeamResponse(team);
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(Long leaderUserId, Long teamId, TeamUpdateRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.getLeaderUserId().equals(leaderUserId)) {
            throw new AppException(ErrorCode.TEAM_FORBIDDEN);
        }

        team.setName(request.getName());
        team.setIntro(request.getIntro());
        team.setGoal(request.getGoal());
        team.setPracticeInfo(request.getPracticeInfo());

        return toTeamResponse(team);
    }

    @Override
    @Transactional
    public void deleteTeam(Long leaderUserId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.getLeaderUserId().equals(leaderUserId)) {
            throw new AppException(ErrorCode.TEAM_FORBIDDEN);
        }

        if (teamMemberRepository.existsByTeamIdAndStatus(teamId, TeamMemberStatus.ACTIVE)) {
            throw new AppException(ErrorCode.TEAM_CANNOT_DELETE_HAS_ACTIVE_MEMBERS);
        }

        teamRepository.delete(team);
    }

    @Override
    public TeamDetailResponse getTeamDetail(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        List<Slot> slots = slotRepository.findByTeamId(teamId);
        List<SlotSummaryResponse> slotSummaries = new ArrayList<SlotSummaryResponse>();

        for (Slot s : slots) {
            int activeCount = teamMemberRepository.countBySlotIdAndStatus(s.getId(), TeamMemberStatus.ACTIVE);
            SlotSummaryResponse ss = new SlotSummaryResponse();
            ss.setSlotId(s.getId());
            ss.setInstrument(s.getInstrument());
            ss.setCapacity(s.getCapacity());
            ss.setRequiredLevelMin(s.getRequiredLevelMin());
            ss.setActiveMemberCount(activeCount);
            slotSummaries.add(ss);
        }

        TeamDetailResponse res = new TeamDetailResponse();
        res.setTeamId(team.getId());
        res.setLeaderUserId(team.getLeaderUserId());
        res.setName(team.getName());
        res.setIntro(team.getIntro());
        res.setGoal(team.getGoal());
        res.setPracticeInfo(team.getPracticeInfo());
        res.setSlots(slotSummaries);
        return res;
    }

    @Override
    public List<TeamSummaryResponse> searchTeams(String region, String instrument, Level level) {
        // MVP 단순 구현:
        // - region: team.practiceInfo에 포함된 문자열로 contains 검색(구조화 안 되어있으므로)
        // - instrument/level: 팀 슬롯 중 하나라도 조건 만족하면 통과
        List<Team> allTeams = teamRepository.findAll();
        List<TeamSummaryResponse> result = new ArrayList<TeamSummaryResponse>();

        for (Team team : allTeams) {
            if (region != null && !region.isEmpty()) {
                if (team.getPracticeInfo() == null || !team.getPracticeInfo().contains(region)) continue;
            }

            if (instrument != null || level != null) {
                List<Slot> slots = slotRepository.findByTeamId(team.getId());
                boolean ok = false;
                for (Slot s : slots) {
                    boolean instOk = (instrument == null || instrument.isEmpty()) || instrument.equals(s.getInstrument());
                    boolean levelOk = (level == null) || (s.getRequiredLevelMin().ordinal() <= level.ordinal());
                    if (instOk && levelOk) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) continue;
            }

            TeamSummaryResponse ts = new TeamSummaryResponse();
            ts.setTeamId(team.getId());
            ts.setName(team.getName());
            ts.setIntro(team.getIntro());
            ts.setGoal(team.getGoal());
            ts.setPracticeInfo(team.getPracticeInfo());
            result.add(ts);
        }

        return result;
    }

    @Override
    public List<MemberProfileResponse> getTeamMembers(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new AppException(ErrorCode.TEAM_NOT_FOUND);
        }

        List<TeamMember> members =
                teamMemberRepository.findByTeamIdAndStatus(teamId, TeamMemberStatus.ACTIVE);

        List<MemberProfileResponse> res = new ArrayList<MemberProfileResponse>();
        for (TeamMember m : members) {
            Profile p = profileRepository.findById(m.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

            MemberProfileResponse mp = new MemberProfileResponse();
            mp.setUserId(m.getUserId());
            mp.setInstrument(p.getInstrument());
            mp.setLevel(p.getLevel());
            mp.setRegion(p.getRegion());
            res.add(mp);
        }
        return res;
    }

    private TeamResponse toTeamResponse(Team team) {
        TeamResponse res = new TeamResponse();
        res.setTeamId(team.getId());
        res.setLeaderUserId(team.getLeaderUserId());
        res.setName(team.getName());
        res.setIntro(team.getIntro());
        res.setGoal(team.getGoal());
        res.setPracticeInfo(team.getPracticeInfo());
        return res;
    }
}
