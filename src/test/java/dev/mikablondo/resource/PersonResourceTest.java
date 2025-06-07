package dev.mikablondo.resource;

import dev.mikablondo.model.Person;
import dev.mikablondo.service.PersonService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class PersonResourceTest {

    @InjectMock
    PersonService personService;

    @Test
    void testGetPersonEndpoint() {
        when(personService.get()).thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .when().get("/persons")
                .then()
                .statusCode(200)
                .body(is("[]"));
    }

    @Test
    void testCountEndpoint() {
        when(personService.countPersons()).thenReturn(Uni.createFrom().item(5L));

        given()
                .when().get("/persons/count")
                .then()
                .statusCode(200)
                .body(is("5"));
    }

    @Test
    void testCreatePersonEndpoint() {
        Person mockPerson = Person.builder().firstname("Micka").lastname("Blondo").build();

        when(personService.create(any(Person.class))).thenReturn(Uni.createFrom().item(mockPerson));

        given()
                .body(mockPerson)
                .contentType("application/json")
                .when().post("/persons")
                .then()
                .statusCode(200)
                .body("firstname", is("Micka"));
    }
}