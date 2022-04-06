package org.example.movieapi.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "movie")
@Getter
@Setter
@ToString(exclude = {"director", "actors"})
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
@Builder
public class Movie {


    // 2 prefered strategies : Sequence, Identity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull // lombock
    @Column(nullable = false, length = 250)
    @Convert(converter = EmptyStringConvertor.class)
    private String title;

    @NonNull
    @Column(nullable = false)
    private int year;

    /**
     * duration in minutes
     */
    @Column(nullable = true) // default
    private Integer duration;

    // NB : by default all is persistent
    @Transient
    private String synopsis;

    @Enumerated(EnumType.STRING) // ORDINAL (default)
    @Column(nullable = true)
    private Color color;

    @ElementCollection
    @CollectionTable(name = "genre",
            joinColumns = @JoinColumn(name = "fk_movie_id",
                    foreignKey = @ForeignKey(name = "fk_genre_movie_id")))
    @Column(name = "genre")
    @Builder.Default
    private Set<String> genres = new HashSet<>();

    @ManyToOne(
            fetch = FetchType.LAZY,  // EAGER by default
            cascade = { CascadeType.PERSIST } // REMOVE, MERGE, REFRESH, ALL
    )
    @JoinColumn(nullable = true, name = "fk_director_id",
            foreignKey = @ForeignKey(name="fk_director_id"))
    private People director;

    // @Transient // when not mapped
    @ManyToMany // fetch lazy by default
    @JoinTable(
            name="play",
            joinColumns = @JoinColumn(name="fk_movie_id", foreignKey = @ForeignKey(name="fk_movie_id")),
            inverseJoinColumns = @JoinColumn(name="fk_actor_id", foreignKey = @ForeignKey(name="fk_actor_id"))
    )
    @Builder.Default
    private Set<People> actors = new HashSet<>();
}
