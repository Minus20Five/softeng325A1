package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.types.Config;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.User;
import nz.ac.auckland.concert.service.domain.mapper.ConcertMapper;
import nz.ac.auckland.concert.service.domain.mapper.PerformerMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
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


}
