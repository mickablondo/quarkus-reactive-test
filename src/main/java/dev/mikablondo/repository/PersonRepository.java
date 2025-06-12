package dev.mikablondo.repository;

import dev.mikablondo.model.Person;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.sqlclient.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

        return Multi.createFrom().emitter(emitter -> {
            classicClient.getConnection().onComplete(connResult -> {
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
            });
        });
    }
}

