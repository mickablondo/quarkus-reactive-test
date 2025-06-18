package dev.mikablondo.service;

import dev.mikablondo.dto.PersonDto;
import dev.mikablondo.model.Person;
import dev.mikablondo.model.Sport;
import dev.mikablondo.repository.PersonRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hibernate.reactive.mutiny.Mutiny;

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
    public Uni<List<PersonDto>> get() {
        return Person.<Person>findAll().list()
                .map(persons ->
                        persons.stream()
                                .map(p -> new PersonDto(p.id, p.firstname, p.lastname))
                                .toList()
                );
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
        return personRepository.streamAll();
    }

    /**
     * Streams all Person entities along with their associated sports.
     *
     * @return a Multi that emits Person entities with sports
     */
    public Multi<Person> streamWithSports() {
        return personRepository.streamAllWithSports();
    }

    /**
     * Associates a Sport entity with a Person entity.
     *
     * @param personId the ID of the Person entity
     * @param sportId  the ID of the Sport entity
     * @return a Uni that emits the updated Person entity
     */
    @Transactional
    public Uni<Person> associateSportToPerson(Long personId, Long sportId) {
        return Person.<Person>findById(personId)
                .onItem().ifNull().failWith(() -> new RuntimeException("Person not found"))
                .flatMap(person -> Mutiny.fetch(person.getSports()).replaceWith(person)
                        .flatMap(p -> Sport.<Sport>findById(sportId)
                                .onItem().ifNull().failWith(() -> new RuntimeException("Sport not found"))
                                .flatMap(sport -> Mutiny.fetch(sport.getPersons())
                                        .replaceWith(sport)
                                        .map(s -> {
                                            p.getSports().add(s);
                                            s.getPersons().add(p);
                                            return p;
                                        })
                                )
                        )
                )
                .flatMap(person -> person.persist().replaceWith(person));
    }
}
