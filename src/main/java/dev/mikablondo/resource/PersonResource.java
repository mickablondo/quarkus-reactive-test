package dev.mikablondo.resource;

import dev.mikablondo.model.Person;
import dev.mikablondo.service.PersonService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    @Inject
    PersonService personService;

    @GET
    public Uni<List<Person>> list() {
        return personService.get();
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
}
