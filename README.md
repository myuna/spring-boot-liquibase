## Evolve your Spring-Boot application with Liquibase [![Build Status](https://travis-ci.org/myuna/spring-boot-liquibase.svg?branch=master)](https://travis-ci.org/myuna/spring-boot-liquibase)

### How to run?
clone the codebase
```bash
git clone git@github.com:myuna/spring-boot-liquibase.git
```
compile, test, package
```bash
./mvnw clean package
```

run
```bash
./mvnw spring-boot:run
```

Go to http://localhost:8080/h2-console  
  
`JDBC URL`: jdbc:h2:mem:testdb  
`User Name`: sa  
`Password`: Leave it blank

Now you have the access of in-memory `H2` database dashboard.

 