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

import java.util.List;
import java.util.Set;
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
        var movie = Movie.builder()
                .title("Sin City")
                .year(2005)
                .color(color)
                .build();
        entityManager.persist(movie);
    }

    @Rollback(false)
    @Test
    void testPersistWithDirector(){
        var movie = Movie.of("Pulp Fiction", 1994);
        var quentin = People.of("Quentin Tarantino");
        var shyamalan = People.of("M. Night Shyamalan");
        entityManager.persist(movie);
        entityManager.persist(quentin);
        entityManager.persist(shyamalan);
        entityManager.flush(); // 2 x insert
        // association director
        movie.setDirector(shyamalan);
        shyamalan.getDirectedMovies().add(movie);
        entityManager.flush(); // update
        var idMovie = movie.getId();
        entityManager.clear();
        // select movie (+director if fetch eager)
        var movieRead = entityManager.find(Movie.class, idMovie);
        // WARNING : do not put association attributes in toString => fetch
        System.out.println("Movie read:" + movieRead);
        assertNotNull(movieRead.getDirector());
        assertEquals("M. Night Shyamalan", movieRead.getDirector().getName());
        // update director
        movieRead.setDirector(quentin);
        shyamalan.getDirectedMovies().remove(movieRead);
        quentin.getDirectedMovies().add(movieRead);
        entityManager.flush();
        // TODO : read again data
    }

    @Rollback(false)
    @Test
    void testPersistWithActors() {
        var movie = Movie.of("Pulp Fiction", 1994);
        var bruce = People.of("Bruce Willis");
        var people = List.of(
                People.of("John Travolta"),
                People.of("Uma Thurman"),
                People.of("Samuel L. Jackson"));
        entityManager.persist(movie);
        people.forEach(entityManager::persist);
        // people.forEach(p -> entityManager.persist(p));
        // for (var p: people){
        //    entityManager.persist(p)
        // }
        entityManager.persist(bruce);
        entityManager.flush(); // 1 insert into movie, 3+1 into people
        // association actors
        movie.getActors().addAll(people);
        people.forEach(p -> p.getPlayedMovies().add(movie));
        entityManager.flush(); // 3 insert into play
        var idMovie = movie.getId();
        entityManager.clear();
        var movieRead = entityManager.find(Movie.class, idMovie);
        System.out.println(movieRead);
        assertEquals(3, movieRead.getActors().size());
        // modify actor list
        movieRead.getActors().add(bruce);
        movieRead.getActors().add(bruce); // with a Set not added twice
        bruce.getPlayedMovies().add(movieRead);
        entityManager.flush(); // SQL : 1 insert into play
        movieRead.getActors().remove(bruce);
        bruce.getPlayedMovies().remove(movieRead);
        entityManager.flush(); // SQL : 1 delete
    }

    @Rollback(false)
    @Test
    void testPersistGenres(){
        var movie = Movie.of("Reservoir Dogs", 1992);
        entityManager.persist(movie);
        var genres = Set.of("Crime", "Drama", "Thriller");
        movie.getGenres().addAll(genres);
        entityManager.flush();
    }

    private static Stream<String> wrongLengthTitles(){
        return Stream.of("", RandomString.make(251));
    }

}