package org.example.movieapi.model;

import lombok.*;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "people")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(staticName = "of")
@Builder
@ToString
public class People {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "people_seq")
    @SequenceGenerator(name = "people_seq", sequenceName = "people_seq")
    private Integer id;

    @NonNull
    @Column(nullable = false)
    private String name;

    private LocalDate birthdate;
    // @Temporal(TemporalType.DATE)
    // @Column(name="birth_date")
    // private Date birthdate;

    @Formula("YEAR(birthdate) ")  // name of column, function in DB !
    @Setter(AccessLevel.NONE)
    private Integer birthYear;

    @OneToMany(mappedBy = "director")
    @Builder.Default
    private Set<Movie> directedMovies = new HashSet<>();

    @ManyToMany(mappedBy = "actors")
    @Builder.Default
    private Set<Movie> playedMovies = new HashSet<>();
}
