package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.types.Config;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.User;
import nz.ac.auckland.concert.service.domain.mapper.ConcertMapper;
import nz.ac.auckland.concert.service.domain.mapper.PerformerMapper;
import nz.ac.auckland.concert.service.domain.mapper.UserMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
@Path("/res")
public class ConcertResource {

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
    public Response authenticateUser(UserDTO userDTO){
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
    public Response reserveSeats(ReservationRequestDTO requestDTO, @CookieParam(Config.CLIENT_COOKIE) Cookie token){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            for (Field f : requestDTO.getClass().getDeclaredFields()){
                if (f.get(requestDTO) == null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            }
            em.getTransaction().begin();
            if (token == null){
               return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            TypedQuery<User> userQuery = em.createQuery("SELECT U FROM USERS U WHERE U.token = :token", User.class)
                   .setParameter("token", token.getValue());
            User user = userQuery.getSingleResult();
            if (user == null){
               return Response.status(Response.Status.FORBIDDEN).build();
            }
            TypedQuery<Concert> concertQuery = em.createQuery("SELECT C FROM CONCERTS C WHERE C.id = :id", Concert.class)
                    .setParameter("id", requestDTO.getConcertId());
            Concert concert = concertQuery.getSingleResult();
            if (concert == null){
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            if (!concert.getDateTimes().contains(requestDTO.getDate())){
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return null;


        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }


    }





}
