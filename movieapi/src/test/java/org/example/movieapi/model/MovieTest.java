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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
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

    @Autowired
    EntityManager trueEntityManager; // all hibernate api

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
        entityManager.clear();
        var movieRead = entityManager.find(Movie.class, movie.getId());
        assertEquals(title, movieRead.getTitle());
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
        movie.setDirector(shyamalan);
        entityManager.flush(); // update
        var idMovie = movie.getId();
        var idQuentin = quentin.getId();
        entityManager.clear();
        // select movie (+director if fetch eager)
        var movieRead = entityManager.find(Movie.class, idMovie);
        // WARNING : do not put association attributes in toString => fetch
        System.out.println("Movie read:" + movieRead);
        assertNotNull(movieRead.getDirector());
        assertEquals("M. Night Shyamalan", movieRead.getDirector().getName());
        // update director
        var quentinRead = entityManager.find(People.class, idQuentin);
        movieRead.setDirector(quentinRead);
        entityManager.flush();
        // TODO : read again data
    }

    @Rollback(false)
    @Test
    void testPersistBothMovieDirector(){
        var movie = Movie.of("Pulp Fiction", 1994);
        var quentin = People.of("Quentin Tarantino");
        movie.setDirector(quentin);
        // entityManager.persist(quentin);  // persist both by programming it ;)
        entityManager.persist(movie);
        entityManager.flush();  // persist both if cascade persist else fail
        var idMovie = movie.getId();
        entityManager.clear();
        // select movie (+director if fetch eager)
        var movieRead = entityManager.find(Movie.class, idMovie);
        // WARNING : do not put association attributes in toString => fetch
        System.out.println("Movie read:" + movieRead);
        assertNotNull(movieRead.getDirector());
        assertEquals("Quentin Tarantino", movieRead.getDirector().getName());
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
        movie.getActors().addAll(people);
        entityManager.flush(); // 3 insert into play
        var idMovie = movie.getId();
        var idBruce = bruce.getId();
        entityManager.clear();
        var movieRead = entityManager.find(Movie.class, idMovie);
        var bruceRead = entityManager.find(People.class, idBruce);
        System.out.println(movieRead);
        assertEquals(3, movieRead.getActors().size());
        // modify actor list
        movieRead.getActors().add(bruce);
        movieRead.getActors().add(bruce); // with a Set not added twice
        entityManager.flush(); // SQL : 1 insert into play
        movieRead.getActors().remove(bruce);
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

    @Rollback(false)
    @Test
    void testRemoveWithDirector(){
        var movie = Movie.of("Pulp Fiction", 1994);
        var quentin = People.of("Quentin Tarantino");
        entityManager.persist(movie);
        entityManager.persist(quentin);
        entityManager.flush();
        movie.setDirector(quentin);
        entityManager.flush();
        var idMovie = movie.getId();
        entityManager.clear();
        var movieRead = entityManager.find(Movie.class, idMovie);
        entityManager.remove(movieRead);
        entityManager.flush(); // remove only movie if not cascade remove else remove both
    }

    @Rollback(false)
    @Test
    void testRemoveWithDirector2(){
        var movie = Movie.of("Pulp Fiction", 1994);
        var movie2 = Movie.of("Reservoir Dogs", 1992);
        var quentin = People.of("Quentin Tarantino");
        entityManager.persist(movie);
        entityManager.persist(movie2);
        entityManager.persist(quentin);
        entityManager.flush();
        movie.setDirector(quentin);
        movie2.setDirector(quentin);
        entityManager.flush();
        var idMovie = movie.getId();
        entityManager.clear();
        var movieRead = entityManager.find(Movie.class, idMovie);
        entityManager.remove(movieRead);
        entityManager.flush(); // remove only movie if not cascade remove else fail trying removing director too
    }

    @Test
    void testDetachMerge(){
        var movie = Movie.of("Pulp Fiction", 1994);
        entityManager.persist(movie);
        entityManager.flush();;
        entityManager.detach(movie);
        // update entity in detach mode
        movie.setDuration(154);
        // reintegrate entity in hibernate cache by merging it with the version from db
        entityManager.merge(movie);
        entityManager.flush();;
        entityManager.clear();;
        var movieRead = entityManager.find(Movie.class, movie.getId());
        assertEquals(154, movieRead.getDuration());
    }

    @Test
    void testDetachMerge2(){
        var movie = Movie.of("Pulp Fiction", 1994);
        entityManager.persist(movie);
        entityManager.flush();;
        entityManager.detach(movie);
        // update entity in detach mode
        movie.setDuration(154);
        // modify the one in the db
        var movieRead = entityManager.find(Movie.class, movie.getId());
        var quentin = People.of("Quentin Tarantino");
        entityManager.persist(quentin);
        movieRead.setDirector(quentin);
        entityManager.flush();
        // reintegrate entity in hibernate cache by merging it with the version from db
        entityManager.merge(movie);
        entityManager.flush();;
        entityManager.clear();;
        var movieRead2 = entityManager.find(Movie.class, movie.getId());
        assertEquals(154, movieRead2.getDuration());
        assertNull(movieRead2.getDirector());
        // assertEquals("Quentin Tarantino", movieRead2.getDirector().getName());
    }

    private static Stream<String> wrongLengthTitles(){
        return Stream.of("", RandomString.make(251));
    }

}