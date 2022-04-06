-- 
-- Sequence for generating people id

DROP SEQUENCE IF EXISTS people_seq;
CREATE SEQUENCE people_seq start with 1 increment by 50 cache 1000;
SELECT SETVAL(people_seq, 1, 0);

--
-- Table structure for table people
--

DROP TABLE IF EXISTS people;
CREATE TABLE people (
  id integer NOT NULL,
  name varchar(100) NOT NULL,
  birthdate date NULL,
  CONSTRAINT pk_people PRIMARY KEY (`id`)
);

--
-- Table structure for table `movie`
--

DROP TABLE IF EXISTS movie;
CREATE TABLE movie (
  id integer NOT NULL AUTO_INCREMENT,
  title varchar(250) NOT NULL,
  year integer NOT NULL,
  color varchar(15) DEFAULT NULL,
  duration integer DEFAULT NULL,
  fk_director_id integer NULL,
  CONSTRAINT pk_movie PRIMARY KEY (id),
  CONSTRAINT fk_director_id FOREIGN KEY (fk_director_id) REFERENCES people (id)
);

--
-- Table structure for table genre
--

DROP TABLE IF EXISTS genre;
CREATE TABLE genre (
  fk_movie_id integer NOT NULL,
  genre varchar(15) NOT NULL,
  CONSTRAINT fk_genre_movie_id FOREIGN KEY (fk_movie_id) REFERENCES movie (id)
);

--
-- Table structure for table `play`
--

DROP TABLE IF EXISTS play;
CREATE TABLE play (
  fk_movie_id int(11) NOT NULL,
  fk_actor_id int(11) NOT NULL,
  CONSTRAINT pk_play PRIMARY KEY (fk_movie_id,fk_actor_id),
  CONSTRAINT fk_actor_id FOREIGN KEY (fk_actor_id) REFERENCES people (id),
  CONSTRAINT fk_movie_id FOREIGN KEY (fk_movie_id) REFERENCES movie (id)
);
