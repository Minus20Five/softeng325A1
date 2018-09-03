package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "RESERVATIONS")
public class Reservation {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.PERSIST,
            orphanRemoval = true
    )
    private Set<Seat> seats = new HashSet<Seat>();

    public Reservation(User user, PriceBand priceBand, int numberOfSeats, Concert concert, LocalDateTime dateTime, LocalTime timeStamp) {
        this.user = user;
        this.priceBand = priceBand;
        this.numberOfSeats = numberOfSeats;
        this.concert = concert;
        this.dateTime = dateTime;
        this.timeStamp = timeStamp;
    }

    public Reservation() {

    }

    @Enumerated(EnumType.STRING)
    private PriceBand priceBand;

    private int numberOfSeats;

    @ManyToOne
    private Concert concert;

    private LocalDateTime dateTime;

    private Boolean confirmed = false;

    private LocalTime timeStamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }

    public PriceBand getPriceBand() {
        return priceBand;
    }

    public void setPriceBand(PriceBand priceBand) {
        this.priceBand = priceBand;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
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

    public void setDateTime(LocalDateTime date) {
        this.dateTime = date;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }


    public LocalTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalTime timeStamp) {
        this.timeStamp = timeStamp;
    }

}
