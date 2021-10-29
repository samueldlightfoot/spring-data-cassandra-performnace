package com.piddubnyi.test.springdatacassandraperformance.persistence.repository;

import com.piddubnyi.test.springdatacassandraperformance.persistence.model.SnapshotRecord;
import org.springframework.data.cassandra.core.cql.ReactiveCqlOperations;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SnapshotRepository extends ReactiveCrudRepository<SnapshotRecord, Long> {

    default Mono<Boolean> saveViaCql(ReactiveCqlOperations cqlOps, SnapshotRecord record) {
        return cqlOps.execute(
                "INSERT INTO snapshot (id, market,slot,value) VALUES (?,?,?,?) USING TIMESTAMP ?;",
                ps -> {
                    return ps.bind(
                            record.getId(),
                            record.getMarket(),
                            record.getSlot(),
                            record.getValue(),
                            record.getSlot().toEpochMilli() * 1000
                    );
                }
        );
    }
}
