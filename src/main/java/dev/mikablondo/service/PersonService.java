package dev.mikablondo.service;

import dev.mikablondo.model.Person;
import dev.mikablondo.repository.PersonRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PersonService {

    @Inject
    PersonRepository personRepository;

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

    /**
     * Counts the total number of Person entities in the database.
     *
     * @return a Uni that emits the count of Person entities
     */
    public Uni<Long> countPersons() {
        return Person.count();
    }

    /**
     * Streams all Person entities from the database.
     *
     * @return a Multi that emits Person entities
     */
    public Multi<Person> stream() {
        return personRepository.
    }
}
