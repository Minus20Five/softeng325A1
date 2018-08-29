package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity(name = "CREDIT_CARDS")
public class CreditCard {

    @Enumerated(EnumType.STRING)
    private CreditCardDTO.Type type;

    private String name;
    @Id
    private String number;


    private LocalDate expiryDate;

    public CreditCard(CreditCardDTO.Type type, String name, String number, LocalDate expiryDate) {
        this.type = type;
        this.name = name;
        this.number = number;
        this.expiryDate = expiryDate;
    }

    public CreditCard() {
    }

    public CreditCardDTO.Type getType() {
        return type;
    }

    public void setType(CreditCardDTO.Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }





}
