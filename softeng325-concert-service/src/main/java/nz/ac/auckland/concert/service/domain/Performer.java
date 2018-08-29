package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.Genre;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "PERFORMERS")
public class Performer {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    private String imageName;

    private String performerName;

    @ManyToMany(mappedBy = "performers")
    private Set<Concert> concerts = new HashSet<>();

    public Long getId() {
        return id;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getPerformerName() {
        return performerName;
    }

    public void setPerformerName(String performerName) {
        this.performerName = performerName;
    }

    public Set<Concert> getConcerts() {
        return concerts;
    }

    public void setConcerts(Set<Concert> concerts) {
        this.concerts = concerts;
    }


    public Performer(){}

    public Performer(Genre genre, String imageName, String performerName) {
        this.genre = genre;
        this.imageName = imageName;
        this.performerName = performerName;
    }
}
