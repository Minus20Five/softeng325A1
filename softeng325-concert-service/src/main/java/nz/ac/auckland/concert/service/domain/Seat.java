package nz.ac.auckland.concert.service.domain;


import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.awt.print.PrinterException;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity(name = "SEATS")
@IdClass(SeatKey.class)
public class Seat {

    @Version
    private int version;

    @Id
    @Convert(converter = SeatNumberConverter.class)
    private SeatNumber seatNumber;

    @Id
    @Enumerated(EnumType.STRING)
    private SeatRow seatRow;

    @Id
    @ManyToOne
    private Concert concert;

    @Id
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    private PriceBand priceBand;

    @ManyToOne
    @JoinColumn
    private Reservation reservation;

    public PriceBand getPriceBand() {
        return priceBand;
    }

    public void setPriceBand(PriceBand priceBand) {
        this.priceBand = priceBand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Seat seat = (Seat) o;

        return new EqualsBuilder()
                .append(seatNumber, seat.seatNumber)
                .append(seatRow, seat.seatRow)
                .append(concert, seat.concert)
                .append(dateTime, seat.dateTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(seatNumber)
                .append(seatRow)
                .append(concert)
                .append(dateTime)
                .toHashCode();
    }

    public Seat() {

    }

    public Seat(SeatNumber seatNumber, SeatRow seatRow, Concert concert, LocalDateTime dateTime, Reservation reservation, PriceBand priceBand) {
        this.seatNumber = seatNumber;
        this.seatRow = seatRow;
        this.concert = concert;
        this.dateTime = dateTime;
        this.reservation = reservation;
        this.priceBand = priceBand;
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime localDateTime) {
        this.dateTime = localDateTime;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
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

}


