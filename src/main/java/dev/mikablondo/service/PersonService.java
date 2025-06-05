package dev.mikablondo.service;

import dev.mikablondo.model.Person;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PersonService {
    public Uni<List<Person>> get() {
        return Person.listAll();
    }
}
