package com.piddubnyi.test.springdatacassandraperformance.persistence;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.ReactiveSession;
import org.springframework.data.cassandra.ReactiveSessionFactory;
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.AsyncCassandraOperations;
import org.springframework.data.cassandra.core.AsyncCassandraTemplate;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.cql.AsyncCqlOperations;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DropKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.session.DefaultBridgedReactiveSession;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableReactiveCassandraRepositories
public class CassandraConfig extends AbstractReactiveCassandraConfiguration {

    private final String keyspace = "test_keyspace";

    @Override
    protected String getKeyspaceName() {
        return keyspace;
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        return Arrays.asList(CreateKeyspaceSpecification.createKeyspace(keyspace).ifNotExists().withSimpleReplication());
    }

    @Override
    protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
        return Arrays.asList(DropKeyspaceSpecification.dropKeyspace(keyspace).ifExists());
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

//    @Bean
//    @Primary
//    @Override
//    public ReactiveCassandraTemplate reactiveCassandraTemplate() {
//        AsyncCassandraOperations asyncCassandraOperations = new AsyncCassandraTemplate(getRequiredSession());
//        return new ReactiveCassandraTemplate(asyncCassandraOperations,
//                beanFactory.getBean(CassandraConverter.class));
//    }

    @Override
    public ReactiveSession reactiveCassandraSession() {
        return new CustomSession(getRequiredSession());
    }

    static class CustomSession extends DefaultBridgedReactiveSession {

        public CustomSession(CqlSession session) {
            super(session);
        }

        @Override
        public Mono<PreparedStatement> prepare(SimpleStatement statement) {
            return super.prepare(SimpleStatement.newInstance(statement.getQuery()));
        }
    }

}
