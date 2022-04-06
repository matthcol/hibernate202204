package org.example.movieapi.repository;

import org.example.movieapi.dto.INameCountDurationTotal;
import org.example.movieapi.dto.ITitleDurationAge;
import org.example.movieapi.dto.TitleDurationAge;
import org.example.movieapi.model.Movie;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.stream.Stream;

public interface IMovieRepository extends JpaRepository<Movie, Integer>, JpaSpecificationExecutor<Movie> {

    // SQL generated automatically with vocabulary
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
    Stream<Movie> findByDirectorNameOrderByYearDesc(String directorName);

    Stream<Movie> findByDirectorName(String directorName, Sort sort);

    @Query("select m from Movie m where m.title = :title and year between :year1 and :year2")
    Stream<Movie> findByTitleRangeYear(String title, Integer year1, Integer year2);

    @Query("select m.title as title, m.duration as duration, YEAR(CURRENT_DATE) - m.year as age from Movie m where m.duration >= :ageThreshold")
    Stream<ITitleDurationAge> findTitleDurationAge(Integer ageThreshold);

    // for complex JPQL Queries read doc in JPA not in Hibernate
    // https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a4665

    @Query("select d.name as name, count(m) as movieCount, COALESCE(sum(m.duration), 0) as durationTotal " +
            " from Movie m join m.director d" +
            " group by d.id, d.name " +
            " having count(m) >= :movieCount " +
            " order by movieCount desc")
    Stream<INameCountDurationTotal> findStatMovieByDirector(Long movieCount);

    // Division problem : count or double not exists
    // 1. acteurs ayant joué dans tous les star wars
    // 2. acteurs ayant joué pour tous ces réalisateurs (Eastwood, Tarantino, Scorcese, Spielberg)

}
