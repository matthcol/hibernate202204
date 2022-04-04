package org.example.movieapi.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Movie {
    @Id
    private int id;
    private String title;
    private int year;
}
