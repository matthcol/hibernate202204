package org.example.movieapi.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "movie")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
public class Movie {


    // 2 prefered strategies : Sequence, Identity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull // lombock
    @Column(nullable = false, length = 250)
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
}
