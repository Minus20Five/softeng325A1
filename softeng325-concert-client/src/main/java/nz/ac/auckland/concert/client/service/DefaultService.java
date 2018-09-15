package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.common.types.Config;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultService implements ConcertService {

    private static String WEB_SERVICE_URI = "http://localhost:10000/services/res";
    private Cookie token;

    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response = null;
        try {
            Builder builder = client.target(WEB_SERVICE_URI + "/concerts").request(MediaType.APPLICATION_XML);
            response = builder.get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(new GenericType<Set<ConcertDTO>>() {
                });
            } else if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                return new HashSet<ConcertDTO>();
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            if (response != null) {
                response.close();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response = null;
        try {
            Builder builder = client.target(WEB_SERVICE_URI + "/performers").request(MediaType.APPLICATION_XML);
            response = builder.get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(new GenericType<Set<PerformerDTO>>() {
                });
            } else if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                return new HashSet<PerformerDTO>();
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            if (response != null) {
                response.close();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response = null;
        try {
            Builder builder = client.target(WEB_SERVICE_URI + "/user")
                    .request(MediaType.APPLICATION_XML);
            response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));
            switch (response.getStatus()) {
                case 201:   //CREATED
                    processCookieFromResponse(response);
                    return response.readEntity(UserDTO.class);
                case 400:   //BAD REQUEST
                    throw new ServiceException(Messages.CREATE_USER_WITH_MISSING_FIELDS);
                case 409:   //CONFLICT
                    throw new ServiceException(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME);
                default:
                    throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            if (response != null) {
                response.close();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response = null;
        try {
            Builder builder = client.target(WEB_SERVICE_URI + "/user/login")
                    .request(MediaType.APPLICATION_XML);
            response = builder.post(Entity.entity(user, MediaType.APPLICATION_XML));
            switch (response.getStatus()) {
                case 202:   //ACCEPTED
                    processCookieFromResponse(response);
                    return response.readEntity(UserDTO.class);
                case 400:   //BAD REQUEST
                    throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS);
                case 404:   //NOT FOUND
                    throw new ServiceException(Messages.AUTHENTICATE_NON_EXISTENT_USER);
                case 401:   //UNAUTHORIZED
                    throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD);
                default:
                    throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            if (response != null) {
                response.close();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
        return null;
    }

    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response = null;
        try {
            Builder builder = client.target(WEB_SERVICE_URI + "/user/reserve")
                    .request(MediaType.APPLICATION_XML);
            if (token != null){
                builder.cookie(token);
            }
            response = builder.post(Entity.entity(reservationRequest, MediaType.APPLICATION_XML));
            switch (response.getStatus()) {
                case 400:   //BAD REQUEST
                    throw new ServiceException(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS);
                case 401:   //UNAUTHORIZED
                    throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
                case 403:   //FORBIDDEN
                    throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
                case 404:   //NOT FOUND
                    throw new ServiceException(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE);
                case 409:   //CONFLICT
                    throw new ServiceException(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION);
                case 201:   //CREATED
                    return response.readEntity(ReservationDTO.class);
                default:
                    throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            if (response != null) {
                response.close();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response = null;
        try {
            Builder builder = client.target(WEB_SERVICE_URI + "/user/confirm")
                    .request(MediaType.APPLICATION_XML);
            if (token != null){
                builder.cookie(token);
            }
            response = builder.post(Entity.entity(reservation, MediaType.APPLICATION_XML));
            switch (response.getStatus()) {
                case 401:   //UNAUTHORIZED
                    throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
                case 403:   //FORBIDDEN
                    throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
                case 408:   //REQUEST_TIMEOUT
                    throw new ServiceException(Messages.EXPIRED_RESERVATION);
                case 404:   //NOT_FOUND
                    throw new ServiceException(Messages.CREDIT_CARD_NOT_REGISTERED);
                case 200:   //OK
                    return;
                default:
                    throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            if (response != null) {
                response.close();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response = null;
        try {
            Builder builder = client.target(WEB_SERVICE_URI + "/user/creditcard")
                    .request(MediaType.APPLICATION_XML);
            if (token != null){
                builder.cookie(token);
            }
            response = builder.post(Entity.entity(creditCard, MediaType.APPLICATION_XML));
            switch (response.getStatus()) {
                case 401:   //UNAUTHORIZED
                    throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
                case 403:   //FORBIDDEN
                    throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
                case 202:   //ACCEPTED
                    return;
                default:
                    throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            if (response != null) {
                response.close();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response = null;
        try {
            Builder builder = client.target(WEB_SERVICE_URI + "/user/bookings")
                    .request(MediaType.APPLICATION_XML);
            if (token != null){
                builder.cookie(token);
            }
            response = builder.get();
            switch (response.getStatus()) {
                case 401:   //UNAUTHORIZED
                    throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
                case 403:   //FORBIDDEN
                    throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
                case 200:   //OK
                    return response.readEntity(new GenericType<Set<BookingDTO>>() {
                    });
                default:
                    throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            if (response != null) {
                response.close();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    private void processCookieFromResponse(Response response) {
        Map<String, NewCookie> cookies = response.getCookies();

        if (cookies.containsKey(Config.CLIENT_COOKIE)) {
            token = cookies.get(Config.CLIENT_COOKIE);
        }
    }
}
