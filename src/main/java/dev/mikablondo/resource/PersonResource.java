package dev.mikablondo.resource;

import dev.mikablondo.dto.PersonDto;
import dev.mikablondo.model.Person;
import dev.mikablondo.service.PersonService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    @Inject
    PersonService personService;

    @GET
    public Uni<List<PersonDto>> list() {
        return personService.get();
    }

    @GET
    @Produces("application/x-ndjson")
    @Path("/stream")
    public Multi<Person> stream(@QueryParam("include") String include) {
        if ("sports".equalsIgnoreCase(include)) {
            return personService.streamWithSports();
        } else {
            return personService.stream();
        }
    }

    @GET
    @Path("/count")
    public Uni<Long> count() {
        return personService.countPersons();
    }

    @POST
    public Uni<Person> create(Person person) {
        return personService.create(person);
    }

    @POST
    @Path("/{personId}/sports/{sportId}")
    public Uni<Response> addSportToPerson(@PathParam("personId") Long personId,
                                          @PathParam("sportId") Long sportId) {
        return personService.associateSportToPerson(personId, sportId)
                .onItem().transform(person -> Response.ok().build())
                .onFailure().recoverWithItem((Throwable throwable) ->
                        Response.status(Response.Status.NOT_FOUND)
                                .entity(Map.of("error", throwable.getMessage()))
                                .build());
    }
}
