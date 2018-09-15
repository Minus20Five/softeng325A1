package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.types.Config;
import nz.ac.auckland.concert.service.domain.*;
import nz.ac.auckland.concert.service.domain.mapper.*;
import nz.ac.auckland.concert.service.util.TheatreUtility;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
@Path("/res")
public class ConcertResource {

    //TODO instead of posting to generic user/stuff, make it user/id/stuff
    //TODO refactor out common Response checks by using exceptions
    //TODO add version to USER
    //TODO in finally block, might need to commit the transaction

//    public ConcertResource() {
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        em.close();
//    }

    @GET
    @Path("/concerts")
    public Response retrieveAllConcerts() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
//            em.getTransaction().begin();
            TypedQuery<Concert> query = em.createQuery("SELECT C FROM CONCERTS C", Concert.class);
            List<Concert> retrievedConcerts = query.getResultList();
            if (retrievedConcerts.isEmpty()) {
                return Response.noContent().build();
            }
            Set<ConcertDTO> concertDTOS = new HashSet<>();
            for (Concert concert : retrievedConcerts) {
                concertDTOS.add(ConcertMapper.toDTO(concert));
            }
            GenericEntity<Set<ConcertDTO>> ge = new GenericEntity<Set<ConcertDTO>>(concertDTOS) {
            };
            return Response.ok(ge).build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/performers")
    public Response retrieveAllPerformers() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            TypedQuery<Performer> query = em.createQuery("SELECT P FROM PERFORMERS P", Performer.class);
            List<Performer> retrievedPerformers = query.getResultList();
            if (retrievedPerformers.isEmpty()) {
                return Response.noContent().build();
            }
            Set<PerformerDTO> performerDTOs = new HashSet<>();
            for (Performer performer : retrievedPerformers) {
                performerDTOs.add(PerformerMapper.toDTO(performer));
            }
            GenericEntity<Set<PerformerDTO>> ge = new GenericEntity<Set<PerformerDTO>>(performerDTOs) {
            };
            return Response.ok(ge).build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/user")
    public Response createNewUser(UserDTO userDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            String[] fields = new String[]{
                    userDTO.getFirstname(),
                    userDTO.getLastname(),
                    userDTO.getUsername(),
                    userDTO.getPassword()
            };
            for (String value : fields) {
                if (value == null || value.isEmpty()) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            }
            User checkUser = em.find(User.class, userDTO.getUsername());
            if (checkUser != null) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            String token = UUID.randomUUID().toString();
            User createdUser = new User(userDTO.getUsername(),
                    userDTO.getPassword(),
                    userDTO.getFirstname(),
                    userDTO.getLastname(),
                    token);
            em.getTransaction().begin();
            em.persist(createdUser);
            em.getTransaction().commit();

            return Response.created(URI.create("/user/" + createdUser.getUsername()))
                    .entity(userDTO)
                    .cookie(new NewCookie(Config.CLIENT_COOKIE, token))
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/user/login")
    public Response authenticateUser(UserDTO userDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            if (userDTO.getUsername() == null || userDTO.getPassword() == null ||
                    userDTO.getUsername().isEmpty() || userDTO.getPassword().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            User user = em.find(User.class, userDTO.getUsername());
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            if (!user.getPassword().equals(userDTO.getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            //If a user exists, they must already have a token
            String token = user.getToken();
            return Response.accepted()
                    .entity(UserMapper.toDTO(user))
                    .cookie(new NewCookie(Config.CLIENT_COOKIE, token))
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/user/reserve")
    public Response reserveSeats(ReservationRequestDTO requestDTO, @CookieParam(Config.CLIENT_COOKIE) Cookie token) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        try {
            //Check DTO has all fields set
            Object[] fields = new Object[]{
                    requestDTO.getConcertId(),
                    requestDTO.getDate(),
                    requestDTO.getNumberOfSeats(),
                    requestDTO.getSeatType()
            };
            for (Object value : fields) {
                if (value == null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            }

            //Check authorization token is passed in
            if (token == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //Check token associated with User
            TypedQuery<User> userQuery = em.createQuery("SELECT U FROM USERS U WHERE U.token = :token", User.class)
                    .setParameter("token", token.getValue());
            User user = userQuery.getSingleResult();
            if (user == null) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            //Check concert exists on requested date and time
            TypedQuery<Concert> concertQuery = em.createQuery("SELECT C FROM CONCERTS C JOIN FETCH C.dateTimes D" +
                    " WHERE C.id = :id", Concert.class)
                    .setParameter("id", requestDTO.getConcertId());
            Concert concert = concertQuery.getSingleResult();
            if (concert == null || (!concert.getDateTimes().contains(requestDTO.getDate()))) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            //Get all reserved seats for concert at the specified time
            TypedQuery<Seat> reservedSeatsForConcert = em.createQuery("SELECT S FROM SEATS S JOIN FETCH S.reservation R " +
                    "WHERE S.concert.id = :concertID " +
                    "AND S.dateTime = :dateTime", Seat.class)
                    .setParameter("concertID", requestDTO.getConcertId())
                    .setParameter("dateTime", requestDTO.getDate())
                    .setLockMode(LockModeType.OPTIMISTIC);
            List<Seat> reservedSeats = reservedSeatsForConcert.getResultList();

            //Remove seats from timed-out reservations and cache them (in case they are re-booked)
            HashMap<Seat, Seat> seatsToBeRemoved = new HashMap<>();
            for (Iterator<Seat> iterator = reservedSeats.iterator(); iterator.hasNext(); ) {
                Seat seat = iterator.next();
                Reservation reservation = seat.getReservation();
                if (!reservation.getConfirmed()
                        && reservation.getTimeStamp().isBefore(LocalTime.now()
                        .minusSeconds(Config.SECONDS_TO_EXPIRE))) {
                    iterator.remove();
                    em.remove(reservation);
                    seat.setReservation(null);
                    seatsToBeRemoved.put(seat, seat);
                }
            }

            //Convert to DTO
            Set<SeatDTO> reservedSeatsDTO = new HashSet<>();
            for (Seat seat : reservedSeats) {
                reservedSeatsDTO.add(SeatMapper.toDTO(seat));
            }

            //Get and check if there are available seats
            Set<SeatDTO> availableSeats = TheatreUtility.findAvailableSeats(
                    requestDTO.getNumberOfSeats(),
                    requestDTO.getSeatType(),
                    reservedSeatsDTO
            );
            if (availableSeats.isEmpty()) {
                return Response.status(Response.Status.CONFLICT).build();
            }

            //Make a reservation
            Reservation newReservation = new Reservation(
                    user,
                    requestDTO.getSeatType(),
                    requestDTO.getNumberOfSeats(),
                    concert,
                    requestDTO.getDate(),
                    LocalTime.now()
            );
            em.persist(newReservation);

            for (SeatDTO seatDTO : availableSeats) {
                Seat seatForReservation = SeatMapper.toDomainModel(
                        seatDTO,
                        concert,
                        requestDTO.getDate(),
                        newReservation);
                if (seatsToBeRemoved.containsKey(seatForReservation)) {
                    Seat canonicalSeat = seatsToBeRemoved.get(seatForReservation);
                    canonicalSeat.setReservation(newReservation);
                    newReservation.getSeats().add(canonicalSeat);
                } else {
                    em.persist(seatForReservation);
                    newReservation.getSeats().add(seatForReservation);
                }
            }

            em.getTransaction().commit();

            return Response.created(URI.create("/user/reserve/" + newReservation.getId()))
                    .entity(new ReservationDTO(newReservation.getId(), requestDTO, availableSeats))
                    .build();

        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/user/confirm")
    public Response confirmReservation(ReservationDTO reservationDTO, @CookieParam(Config.CLIENT_COOKIE) Cookie token){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        try {
            //Check authorization token is passed in
            if (token == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //Check token associated with User
            TypedQuery<User> userQuery = em.createQuery("SELECT U FROM USERS U " +
                    " WHERE U.token = :token", User.class)
                    .setParameter("token", token.getValue());
            User user = userQuery.getSingleResult();

            Reservation reservation = em.find(
                    Reservation.class,
                    reservationDTO.getId(),
                    LockModeType.OPTIMISTIC);

            if (!reservation.getUser().equals(user)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            if (!reservation.getConfirmed()
                    && reservation.getTimeStamp().isBefore(LocalTime.now()
                    .minusSeconds(Config.SECONDS_TO_EXPIRE))){
                em.remove(reservation);
                em.getTransaction().commit();
                return Response.status(Response.Status.REQUEST_TIMEOUT).build();
            }

            if (user.getCreditCard() ==  null){
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            reservation.setConfirmed(true);
            em.getTransaction().commit();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }


    @POST
    @Path("/user/creditcard")
    public Response registerCreditCard(CreditCardDTO creditCardDTO, @CookieParam(Config.CLIENT_COOKIE) Cookie token){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        try {
            //Check authorization token is passed in
            if (token == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //Check token associated with User
            TypedQuery<User> userQuery = em.createQuery("SELECT U FROM USERS U WHERE U.token = :token", User.class)
                    .setParameter("token", token.getValue());
            User user = userQuery.getSingleResult();
            if (user == null) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            CreditCard creditCard = CreditCardMapper.toDomainModel(creditCardDTO);
            user.setCreditCard(creditCard);
            em.getTransaction().commit();

            return Response.accepted().build();

        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/user/bookings")
    public Response getBookings(@CookieParam(Config.CLIENT_COOKIE) Cookie token){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        try {
            //Check authorization token is passed in
            if (token == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //Check token associated with User
            TypedQuery<User> userQuery = em.createQuery("SELECT U FROM USERS U WHERE U.token = :token", User.class)
                    .setParameter("token", token.getValue());
            User user = userQuery.getSingleResult();
            if (user == null) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            TypedQuery<Reservation> bookingQuery = em.createQuery("SELECT DISTINCT R FROM RESERVATIONS R " +
                    "JOIN FETCH R.concert C " +
                    "JOIN FETCH R.seats S " +
                    "WHERE R.confirmed = true " +
                    "AND R.user = :user", Reservation.class)
                    .setParameter("user", user)
                    .setLockMode(LockModeType.OPTIMISTIC);
            List<Reservation> confirmedReservations = bookingQuery.getResultList();
            Set<BookingDTO> bookingDTOs = new HashSet<>();
            for (Reservation reservation : confirmedReservations) {
                bookingDTOs.add(BookingMapper.toDTO(reservation));
            }

            GenericEntity<Set<BookingDTO>> ge = new GenericEntity<Set<BookingDTO>>(bookingDTOs) {};

            return Response.ok(ge).build();

        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }





}
