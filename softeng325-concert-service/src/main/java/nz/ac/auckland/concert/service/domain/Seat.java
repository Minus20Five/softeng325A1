package nz.ac.auckland.concert.service.domain;


import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.time.LocalTime;


@Entity(name = "SEATS")
public class Seat {

    @Id
    @GeneratedValue
    private Long Id;

    @Version
    private int version;

    @ManyToOne
    private Reservation reservations;

    private SeatNumber seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatRow seatRow;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalTime timeStamp;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Reservation getReservations() {
        return reservations;
    }

    public void setReservations(Reservation reservations) {
        this.reservations = reservations;
    }

    public SeatNumber getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(SeatNumber seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatRow getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(SeatRow seatRow) {
        this.seatRow = seatRow;
    }

    private enum Status{BOOKED, RESERVED, FREE}
}
