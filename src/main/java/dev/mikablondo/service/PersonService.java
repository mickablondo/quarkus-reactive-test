package dev.mikablondo.service;

import dev.mikablondo.model.Person;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PersonService {
    /**
     * Retrieves all Person entities from the database.
     *
     * @return a Uni that emits a list of Person entities
     */
    public Uni<List<Person>> get() {
        return Person.listAll();
    }

    /**
     * Creates a new Person entity in the database.
     *
     * @param person the Person entity to be created
     * @return a Uni that emits the created Person entity
     */
    @Transactional
    public Uni<Person> create(Person person) {
        return person.persist().replaceWith(person);
    }
}
