DROP TABLE movies;
DROP SEQUENCE movie_id_seq;
DROP TRIGGER trigger_movie_id;

CREATE TABLE movies (
    movie_id INTEGER PRIMARY KEY,
    title VARCHAR(30),
    year NUMBER(4),
    rating REAL
    UNIQUE (title, year)
);

CREATE SEQUENCE movie_id_seq;

CREATE TRIGGER trigger_movie_id
    BEFORE INSERT ON movies
    FOR EACH ROW
BEGIN
    SELECT movie_id_seq.nextval
        INTO :new.movie_id
        FROM dual;
END;

INSERT INTO movies (title,year,rating) VALUES('L.A. Confidential',1997,10.0);

INSERT INTO movies (title,year,rating) VALUES('A Perfect Murder',1998,6.1);

INSERT INTO movies (title,year,rating) VALUES('Jerry Maguire',1996,8.3);