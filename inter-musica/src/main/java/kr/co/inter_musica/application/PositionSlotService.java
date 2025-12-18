package kr.co.inter_musica.application;

import kr.co.inter_musica.domain.enums.Instrument;
import kr.co.inter_musica.domain.enums.Level;
import kr.co.inter_musica.infrastructure.persistence.entity.PositionSlotJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.entity.TeamJpaEntity;
import kr.co.inter_musica.infrastructure.persistence.jpa.PositionSlotJpaRepository;
import kr.co.inter_musica.infrastructure.persistence.jpa.TeamJpaRepository;
import kr.co.inter_musica.domain.exception.ApiException;
import kr.co.inter_musica.domain.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PositionSlotService {

    private final TeamJpaRepository teamJpaRepository;
    private final PositionSlotJpaRepository positionSlotJpaRepository;

    @Autowired
    public PositionSlotService(TeamJpaRepository teamJpaRepository, PositionSlotJpaRepository positionSlotJpaRepository) {
        this.teamJpaRepository = teamJpaRepository;
        this.positionSlotJpaRepository = positionSlotJpaRepository;
    }

    @Transactional
    public Long createPositionSlot(long currentUserId, Long teamId, String instrumentRaw, int capacity, String requiredLevelMinRaw) {
        TeamJpaEntity team = teamJpaRepository.findById(teamId)
                .orElseThrow(() -> new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다."));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.TEAM_FORBIDDEN, "팀장만 포지션 슬롯을 생성할 수 있습니다.");
        }

        String instrument = Instrument.from(instrumentRaw).name();
        String required = Level.from(requiredLevelMinRaw).name();

        PositionSlotJpaEntity slot = new PositionSlotJpaEntity(teamId, instrument, capacity, required);
        positionSlotJpaRepository.save(slot);

        return slot.getId();
    }

    @Transactional(readOnly = true)
    public List<PositionSlotJpaEntity> getPositionSlotList(Long teamId) {
        if (!teamJpaRepository.existsById(teamId)) {
            throw new ApiException(ErrorCode.TEAM_NOT_FOUND, "팀을 찾을 수 없습니다.");
        }

        return positionSlotJpaRepository.findByTeamId(teamId);
    }
}
