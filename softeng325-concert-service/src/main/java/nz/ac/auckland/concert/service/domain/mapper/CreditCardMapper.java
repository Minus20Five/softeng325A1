package nz.ac.auckland.concert.service.domain.mapper;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.service.domain.CreditCard;

public class CreditCardMapper {
    public static CreditCard toDomainModel(CreditCardDTO creditCardDTO){
        return new CreditCard(
                creditCardDTO.getType(),
                creditCardDTO.getName(),
                creditCardDTO.getNumber(),
                creditCardDTO.getExpiryDate()
        );
    }

}
