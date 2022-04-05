package org.example.movieapi.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PeopleTest {
    @Autowired
    TestEntityManager entityManager;

    @Test
    void testPersist(){
        var people = People.of("Will Smith");
        entityManager.persist(people);
        // no insert at this point, only nextval on sequence
        entityManager.flush(); // synchronize with DB : SQL insert
    }

    @Rollback(value = false) // to see data in DB
    @Test
    void testPersistBirthdate(){
        var modernBirthdate = LocalDate.of(1968,9,25);
        var people = People.builder()
                .name("Will Smith")
                .birthdate(modernBirthdate)
                //.birthdate(new Date(modernBirthdate.toEpochDay()*24*3600*1000))
                .build();
        entityManager.persist(people);
        entityManager.flush(); // SQL insert
        var idPeople = people.getId();
        entityManager.clear(); // clear hibernate cache
        var peopleRead = entityManager.find(People.class, idPeople);
        System.out.println("People read from db: " + peopleRead);
        assertEquals(1968, peopleRead.getBirthYear());
    }

}