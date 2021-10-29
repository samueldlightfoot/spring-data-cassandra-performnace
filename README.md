# Reproducing cassandra insert performance problem with spring-data

Run docker-compose up (start local cassandra)

Run SpringDataCassandraPerformanceApplication

Change insert mode by switching execution CassandraInsertRunner line 58,59

Insert performance difference in my case is huge:
 - ~15k inserts per second with ReactiveCrudRepository
 - ~40k inserts per second with Plain Cql execution

Tested on Java 8 and 17 
 - https://docs.spring.io/spring-data/cassandra/docs/current/reference/html/#mapping.property-population

