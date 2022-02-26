package com.piddubnyi.test.springdatacassandraperformance.persistence.runner;

import com.piddubnyi.test.springdatacassandraperformance.persistence.model.SnapshotRecord;
import com.piddubnyi.test.springdatacassandraperformance.persistence.repository.SnapshotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.data.cassandra.core.cql.ReactiveCqlOperations;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class CassandraInsertRunner implements SmartLifecycle {

    private static final AtomicLong success = new AtomicLong();
    private static final AtomicLong fail = new AtomicLong();

    static {
        final ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
        service.scheduleAtFixedRate(() -> System.out.println("Saved: " + success.getAndSet(0)), 0, 1, TimeUnit.SECONDS);
        service.scheduleAtFixedRate(() -> System.err.println("Failed: " + fail.getAndSet(0)), 0, 1, TimeUnit.SECONDS);
    }

    private final ReactiveCqlOperations cqlOps;
    private final SnapshotRepository repository;
    private Disposable subscription;

    @Autowired
    public CassandraInsertRunner(SnapshotRepository repository, ReactiveCassandraTemplate cassandraTemplate) {
        this.repository = repository;
        this.cqlOps = cassandraTemplate.getReactiveCqlOperations();
        cassandraTemplate.setUsePreparedStatements(true);
        log.info("Use prep st = true");
    }

    @Override
    public void start() {
        Flux<SnapshotRecord> data = Flux.generate(Object::new, (state, sink) -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            sink.next(
                    new SnapshotRecord(
                            random.nextLong(),
                            (short) random.nextInt(),
                            Clock.systemUTC().instant(),
                            random.nextDouble()
                    )
            );
            return state;
        });
        subscription = data
                .flatMap((SnapshotRecord record) -> repository.saveViaCql(cqlOps, record), 512, 2048)
                //.flatMap(repository::save, 512, 2048)
                .doOnNext(d -> success.incrementAndGet())
                .onErrorContinue((throwable, object) -> fail.incrementAndGet())
                .subscribe();
    }

    @Override
    public void stop() {
        subscription.dispose();
    }

    @Override
    public boolean isRunning() {
        return subscription != null && !subscription.isDisposed();
    }
}
