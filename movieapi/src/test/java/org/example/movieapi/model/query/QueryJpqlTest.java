package org.example.movieapi.model.query;

import org.example.movieapi.dto.TitleDurationAge;
import org.example.movieapi.model.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.Arrays;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("query")
public class QueryJpqlTest {

    @Autowired
    EntityManager entityManager; // all Hibernate API

    @Test
    void testMovieByTitle() {
        var movies = entityManager.createQuery("select m from Movie m where m.title = :title", Movie.class)
                .setParameter("title", "The Man Who Knew Too Much")
                .getResultList();
        System.out.println(movies);
    }

    @Test
    void testMoviesByTitleAndYearBetween(){
        entityManager.createQuery(
                "select m from Movie m where m.title = :title and year between :year1 and :year2",
                Movie.class)
            .setParameter("title", "The Man Who Knew Too Much" )
            .setParameter("year1", 1950)
            .setParameter("year2", 1970)
            .getResultStream()
            .forEach(m -> System.out.println("\t- " + m));
    }

    @Test
    void testMovieCount() {
        var movieCount = entityManager.createQuery("select count(m) from Movie m", Long.class)
                .getSingleResult();
        System.out.println("Movie count = " + movieCount);
    }

    @Test
    void testMovieByYearWithDirector() {
        entityManager.createQuery(
                "select m from Movie m  left join fetch m.director where m.year = :year",
                        Movie.class)
                .setParameter("year",2014)
                .getResultStream()
                .forEach(m -> System.out.println("\t- " + m + " by " + m.getDirector()));
    }

    @Test
    void testFilmography() {
        entityManager.createQuery(
                "select m from Movie m join m.director where m.director.name = :name order by year desc",
                        Movie.class)
                .setParameter("name","Clint Eastwood")
                .getResultStream()
                .forEach(m -> System.out.println("\t- " + m));
    }

    @Test
    void testMovieLessThanNYears(){
        int nYears = 10;
        entityManager.createQuery(
                "select m from Movie m where m.year >= YEAR(CURRENT_DATE) - :nYears",
                        Movie.class)
                .setParameter("nYears", nYears)
                .getResultStream()
                .forEach(m -> System.out.println("\t- " + m));
    }

    @Test
    void testMovieTitleAgeToObjectArray(){
        entityManager.createQuery(
                "select m.title as title, YEAR(CURRENT_DATE) - m.year as age from Movie m where m.duration >= 150",
                    Object[].class
                )
                .getResultStream()
                .forEach(r -> System.out.println("\t- " + Arrays.toString(r)));
    }

    @Test
    void testMovieTitleAgeToTuple(){
        entityManager.createQuery(
                        "select m.title as title, m.duration as duration, YEAR(CURRENT_DATE) - m.year as age from Movie m where m.duration >= 150",
                        Tuple.class
                )
                .getResultStream()
                .forEach(r -> System.out.println(
                        "\t- " + r.get("title", String.class)
                        + " (" + r.get("duration", Integer.class)
                        + ") of age " + r.get("age", Integer.class)
                ));
    }

    @Test
    void testMovieTitleAgeToDTO(){
        entityManager.createQuery(
                        "select new org.example.movieapi.dto.TitleDurationAge(m.title, m.duration, YEAR(CURRENT_DATE) - m.year) from Movie m where m.duration >= 150",
                        TitleDurationAge.class
                )
                .getResultStream()
                .forEach(r -> System.out.println(
                        "\t- " + r.getTitle()
                                + " (" + r.getDuration()
                                + ") of age " + r.getAge()
                ));
    }
}

