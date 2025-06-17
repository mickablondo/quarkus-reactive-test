package dev.mikablondo.service;

import dev.mikablondo.model.Sport;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SportService {

    @Transactional
    public Uni<Sport> create(Sport sport) {
        return sport.persist().replaceWith(sport);
    }
}
