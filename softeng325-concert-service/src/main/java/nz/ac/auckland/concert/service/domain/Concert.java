package nz.ac.auckland.concert.service.domain;

import com.sun.javafx.beans.IDProperty;
import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity(name = "CONCERTS")
public class Concert {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @ElementCollection
    @CollectionTable(
            name = "CONCERT_DATES",
            joinColumns = @JoinColumn(name = "CONCERT_ID")
    )
    @Column(name = "DATETIME")
    private Set<LocalDateTime> dateTimes = new HashSet<>();


    //TODO Make this set have at least one performer
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "CONCERT_PERFORMER",
            joinColumns = @JoinColumn(name = "CONCERT_ID"),
            inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID"))
    private Set<Performer> performers = new HashSet<>();


    @ElementCollection
    @CollectionTable(
            name = "CONCERT_TARIFS",
            joinColumns = @JoinColumn(name = "CONCERT_ID")
    )
    @MapKeyColumn(name = "PRICEBAND")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "COST")
    private Map<PriceBand, BigDecimal> tarrifs = new HashMap<>();

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<LocalDateTime> getDateTimes() {
        return dateTimes;
    }

    public void setDateTimes(Set<LocalDateTime> dateTimes) {
        this.dateTimes = dateTimes;
    }

    public Set<Performer> getPerformers() {
        return performers;
    }

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
    }

    public Map<PriceBand, BigDecimal> getTarrifs() {
        return tarrifs;
    }

    public void setTarrifs(Map<PriceBand, BigDecimal> tarrifs) {
        this.tarrifs = tarrifs;
    }

    public Concert(){}




}
