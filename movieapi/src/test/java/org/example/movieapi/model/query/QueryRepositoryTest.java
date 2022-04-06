package org.example.movieapi.model.query;

import org.example.movieapi.model.Movie;
import org.example.movieapi.repository.IMovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("query")
public class QueryRepositoryTest {

    @Autowired
    IMovieRepository movieRepository;

    @ParameterizedTest
    @ValueSource(strings={"Clint Eastwood", "Quentin Tarantino"})
    void testFilmographyDirector(String name){
        movieRepository.findByDirectorNameOrderByYearDesc(name)
                .forEach(m -> System.out.println("\t- " + m));
    }

    @ParameterizedTest
    @MethodSource("sortMovieProvider")
    void testFilmographyDirectorSort(Sort sort){
        String name = "Clint Eastwood";
        movieRepository.findByDirectorName(name, sort)
                .forEach(m -> System.out.println("\t- " + m));
    }

    @Test
    void testFindByTitleRangeYear(){
        movieRepository.findByTitleRangeYear("The Man Who Knew Too Much", 1900, 1940)
                .forEach(m -> System.out.println("\t- " + m));
    }

    @Test
    void testFindTitleDurationAge(){
        movieRepository.findTitleDurationAge(5)
                .forEach(r -> System.out.println(
                        "\t- " + r.getTitle()
                                + " (" + r.getDuration()
                                + ") of age " + r.getAge()));
    }

    @Test
    void testFindStatMovieByDirector(){
        long movieCountThreshold = 10;
        movieRepository.findStatMovieByDirector(movieCountThreshold)
                .forEach(r -> System.out.println(
                        "\t- " + r.getName()
                        + " : movie count = " + r.getMovieCount()
                        + " , duration total (min) = " + r.getDurationTotal()));
    }

    // queries by Example (Spring Data)
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#query-by-example

    @Test
    void testFindAllMovieWithExample(){
        var movieProbe = new Movie();
        movieProbe.setYear(2014);
        var movieExample = Example.of(movieProbe);
        movieRepository.findAll(movieExample)
                .forEach(m -> System.out.println("\t- " + m));
    }

    @Test
    void testFindAllMovieWithExample2(){
        var movieProbe = new Movie();
        movieProbe.setTitle("Star");
        var movieExample = Example.of(movieProbe,
                ExampleMatcher.matching()
                        .withMatcher("title", match -> match.contains())
                        .withIgnorePaths("year")
        );
        movieRepository.findAll(movieExample)
                .forEach(m -> System.out.println("\t- " + m));
    }

    private static Stream<Sort> sortMovieProvider(){
        return Stream.of(
            Sort.by("year", "title"),
            Sort.by(Sort.Order.desc("year"), Sort.Order.asc("title"))
        );
    }
}
