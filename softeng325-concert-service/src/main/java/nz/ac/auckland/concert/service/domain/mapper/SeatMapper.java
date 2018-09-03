package nz.ac.auckland.concert.service.domain.mapper;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Reservation;
import nz.ac.auckland.concert.service.domain.Seat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatMapper {

    public static SeatDTO toDTO (Seat seat){
        return new SeatDTO(
                seat.getSeatRow(),
                seat.getSeatNumber()
        );
    }


    public static Seat toDomainModel(SeatDTO seatDTO, Concert concert, LocalDateTime dateTime, Reservation reservation){
        return new Seat(seatDTO.getNumber(), seatDTO.getRow(), concert, dateTime, reservation);
    }
}
