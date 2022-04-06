package org.example.movieapi.model.query;

import org.example.movieapi.model.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;

// links about Criteria
// - https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#criteria
// - https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a6925
// - gen meta model : https://docs.jboss.org/hibernate/orm/5.6/topical/html_single/metamodelgen/MetamodelGenerator.html
// - https://www.baeldung.com/spring-data-criteria-queries (via Spring)

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("query")
public class QueryCriteriaTest {

    @Autowired
    EntityManager entityManager;

    @Test
    void  testMovieByTitle(){
        var title = "The Man Who Knew Too Much";
        var cb = entityManager.getCriteriaBuilder();
        var criteriaQuery = cb.createQuery(Movie.class);
        var root = criteriaQuery.from(Movie.class);
        criteriaQuery.select(root)
                .where(cb.equal(root.get("title"), title));
        entityManager.createQuery(criteriaQuery)
                .getResultStream()
                .forEach(m -> System.out.println("\t- " + m));
    }

    @Test
    void  testMovieByYearDuration(){
        int yearMin = 1970;
        int yearMax = 1979;
        int durationMin = 120;
        var cb = entityManager.getCriteriaBuilder();
        var criteriaQuery = cb.createQuery(Movie.class);
        var root = criteriaQuery.from(Movie.class);
        criteriaQuery.select(root)
                .where(cb.and(
                        cb.between(root.get("year"), yearMin, yearMax),
                        cb.ge(root.get("duration"), durationMin)));
        entityManager.createQuery(criteriaQuery)
                .getResultStream()
                .forEach(m -> System.out.println("\t- " + m));
    }
}
