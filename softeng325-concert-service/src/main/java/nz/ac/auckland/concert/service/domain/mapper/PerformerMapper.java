package nz.ac.auckland.concert.service.domain.mapper;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;

import java.util.HashSet;
import java.util.Set;

public class PerformerMapper {

    public static PerformerDTO toDTO(Performer performer) {
        Set<Long> concertIDs = new HashSet<>();
        for (Concert concert : performer.getConcerts()) {
            concertIDs.add(concert.getId());
        }

        return new PerformerDTO(performer.getId(),
                performer.getPerformerName(),
                performer.getImageName(),
                performer.getGenre(),
                concertIDs);
    }
}
