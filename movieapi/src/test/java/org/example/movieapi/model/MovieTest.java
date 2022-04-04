package org.example.movieapi.model;

import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import javax.persistence.PersistenceException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // same DB as appli
class MovieTest {

    @Autowired
    TestEntityManager entityManager; // wrapper spring for test purpose
    // EntityManager entityManager; // all hibernate api

    // on this test only (or annotate class for all tests in this class)
    @Rollback(value = false)
    @Test
    void testPersist(){
        var movie = new Movie();
        movie.setTitle("Gemini Man");
        movie.setYear(2019);
        entityManager.persist(movie);
        System.out.println("Id movie: " + movie.getId());
    }

    @ParameterizedTest
    @ValueSource(strings={
            "Z",
            "Gemini Man",
            "Night of the Day of the Dawn of the Son of the Bride of the Return of the Revenge of the Terror of the Attack of the Evil Mutant Hellbound Flesh Eating Crawling Alien Zombified Subhumanoid Living Dead, Part 5"})
    void testPersistTitle(String title){
        var movie = Movie.of(title, 2019);
        entityManager.persist(movie);
        System.out.println("Movie saved: " + movie);
        assertNotNull(movie.getId());
    }

    @ParameterizedTest
    @MethodSource("wrongLengthTitles")
    void testPersistTitleLengthNOK(String title){
        var movie = Movie.of(title, 2019);
        assertThrows(PersistenceException.class, () ->
                entityManager.persist(movie)
        );
    }

    // NB : constraint check by lombock
    @ParameterizedTest
    @NullSource
    void testPersistTitleNullNOK(String title){
        assertThrows(NullPointerException.class, () -> {
                    var movie = Movie.of(title, 2019);
                    entityManager.persist(movie);
                });
    }

    @Rollback(value = false)
    @ParameterizedTest
    @EnumSource(Color.class)
    void testPersistColor(Color color){
        var movie = Movie.of(null, "Sin City", 2005, null, null, color);
        entityManager.persist(movie);
    }

    private static Stream<String> wrongLengthTitles(){
        return Stream.of("", RandomString.make(251));
    }

}