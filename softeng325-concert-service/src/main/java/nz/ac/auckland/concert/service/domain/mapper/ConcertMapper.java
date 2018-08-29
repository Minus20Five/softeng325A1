package nz.ac.auckland.concert.service.domain.mapper;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;

import java.util.HashSet;
import java.util.Set;

public class ConcertMapper {

    public static ConcertDTO toDTO(Concert concert) {

        Set<Long> performerIDs = new HashSet<>();
        for (Performer performer : concert.getPerformers()) {
            performerIDs.add(performer.getId());
        }

        return new ConcertDTO(concert.getId(),
                concert.getTitle(),
                concert.getDateTimes(),
                concert.getTarrifs(),
                performerIDs
        );
    }

}
