package dev.mikablondo.resource;

import dev.mikablondo.model.Sport;
import dev.mikablondo.service.SportService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/sports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SportResource {

    @Inject
    SportService sportService;

    @POST
    public Uni<Sport> create(Sport sport) {
        return sportService.create(sport);
    }
}
