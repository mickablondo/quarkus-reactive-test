package dev.mikablondo.repository;

import dev.mikablondo.model.Person;
import dev.mikablondo.model.Sport;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.sqlclient.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class PersonRepository {

    @Inject
    Pool client;

    /**
     * Streams all Person entities from the database using a Vert.x SQL Client Pool.
     *
     * @return a Multi that emits Person entities
     */
    public Multi<Person> streamAll() {
        // Convert the Mutiny Pool to a classic Vert.x SQL Client Pool
        io.vertx.sqlclient.Pool classicClient = client.getDelegate();

        return Multi.createFrom().emitter(emitter -> classicClient.getConnection().onComplete(connResult -> {
            if (connResult.failed()) {
                emitter.fail(connResult.cause());
                return;
            }
            SqlConnection conn = connResult.result();

            // Prepare the SQL query to select all persons
            conn.prepare("SELECT id, firstname, lastname FROM Person").onComplete(pqResult -> {
                if (pqResult.failed()) {
                    emitter.fail(pqResult.cause());
                    conn.close();
                    return;
                }
                PreparedStatement preparedQuery = pqResult.result();

                // Create a stream to handle the results (batch size is set to 50)
                RowStream<Row> stream = preparedQuery.createStream(50, Tuple.tuple());

                stream.handler(row -> {
                    Person person = Person.builder()
                            .id(row.getLong("id"))
                            .firstname(row.getString("firstname"))
                            .lastname(row.getString("lastname"))
                            .build();

                    // Emit the person object to the Multi stream
                    emitter.emit(person);
                });
                stream.endHandler(v -> {
                    emitter.complete();
                    conn.close();
                });
                stream.exceptionHandler(err -> {
                    emitter.fail(err);
                    conn.close();
                });
            });
        }));
    }

    /**
     * Streams all Person entities along with their associated sports from the database.
     *
     * @return a Multi that emits Person entities with sports
     */
    public Multi<Person> streamAllWithSports() {
        io.vertx.sqlclient.Pool classicClient = client.getDelegate();

        String sql = """
                    SELECT
                        p.id AS person_id,
                        p.firstname,
                        p.lastname,
                        s.id AS sport_id,
                        s.name AS sport_name,
                        s.rules AS sport_rules
                    FROM Person p
                    LEFT JOIN person_sport ps ON p.id = ps.person_id
                    LEFT JOIN Sport s ON ps.sport_id = s.id
                    ORDER BY p.id
                """;

        return Multi.createFrom().emitter(emitter ->
                classicClient.getConnection().onComplete(connResult -> {
                    if (connResult.failed()) {
                        emitter.fail(connResult.cause());
                        return;
                    }

                    SqlConnection conn = connResult.result();

                    conn.prepare(sql).onComplete(pqResult -> {
                        if (pqResult.failed()) {
                            emitter.fail(pqResult.cause());
                            conn.close();
                            return;
                        }

                        PreparedStatement preparedQuery = pqResult.result();
                        RowStream<Row> stream = preparedQuery.createStream(100, Tuple.tuple());

                        final Map<Long, Person> personMap = new LinkedHashMap<>();

                        stream.handler(row -> {
                            Long personId = row.getLong("person_id");

                            Person person = personMap.computeIfAbsent(personId, id -> Person.builder()
                                    .id(id)
                                    .firstname(row.getString("firstname"))
                                    .lastname(row.getString("lastname"))
                                    .sports(new HashSet<>())
                                    .build());

                            Long sportId = row.getLong("sport_id");
                            if (sportId != null) {
                                person.getSports().add(
                                        Sport.builder()
                                                .id(sportId)
                                                .name(row.getString("sport_name"))
                                                .rules(row.getString("sport_rules"))
                                                .build()
                                );
                            }
                        });

                        stream.endHandler(v -> {
                            personMap.values().forEach(emitter::emit);
                            emitter.complete();
                            conn.close();
                        });

                        stream.exceptionHandler(err -> {
                            emitter.fail(err);
                            conn.close();
                        });
                    });
                })
        );
    }


}

