package nz.ac.auckland.concert.service.domain.mapper;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.Reservation;
import nz.ac.auckland.concert.service.domain.Seat;

import java.util.HashSet;
import java.util.Set;

public class BookingMapper {
    public static BookingDTO toDTO(Reservation reservation){
        Set<SeatDTO> seatDTOS = new HashSet<>();
        for (Seat seat : reservation.getSeats()) {
            seatDTOS.add(SeatMapper.toDTO(seat));
        }
        return new BookingDTO(
                reservation.getConcert().getId(),
                reservation.getConcert().getTitle(),
                reservation.getDateTime(),
                seatDTOS,
                reservation.getPriceBand()
        );
    }
}
