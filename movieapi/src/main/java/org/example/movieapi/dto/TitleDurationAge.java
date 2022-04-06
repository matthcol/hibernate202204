package org.example.movieapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class TitleDurationAge {
    private String title;
    private Integer duration;
    private Integer age;
}
