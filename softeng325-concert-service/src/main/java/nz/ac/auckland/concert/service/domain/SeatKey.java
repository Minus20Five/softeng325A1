package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Convert;
import java.io.Serializable;
import java.time.LocalDateTime;


public class SeatKey implements Serializable {

    SeatNumber seatNumber;
    SeatRow seatRow;
    Concert concert;
    LocalDateTime dateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SeatKey seatKey = (SeatKey) o;

        return new EqualsBuilder()
                .append(seatNumber, seatKey.seatNumber)
                .append(seatRow, seatKey.seatRow)
                .append(concert, seatKey.concert)
                .append(dateTime, seatKey.dateTime)
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
}
