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

    public Multi<Person> streamAll() {
        io.vertx.sqlclient.Pool classicClient = client.getDelegate();

        return Multi.createFrom().emitter(emitter -> {
            classicClient.getConnection().onComplete(connResult -> {
                if (connResult.failed()) {
                    emitter.fail(connResult.cause());
                    return;
                }
                SqlConnection conn = connResult.result();

                conn.prepare("SELECT id, firstname, lastname FROM Person").onComplete(pqResult -> {
                    if (pqResult.failed()) {
                        emitter.fail(pqResult.cause());
                        conn.close();
                        return;
                    }
                    PreparedStatement preparedQuery = pqResult.result();

                    RowStream<Row> stream = preparedQuery.createStream(50, Tuple.tuple());

                    stream.handler(row -> {
                        Person person = Person.builder()
                                .id(row.getLong("id"))
                                .firstname(row.getString("firstname"))
                                .lastname(row.getString("lastname"))
                                .build();

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

