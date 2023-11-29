DROP TABLE moviereviews;
DROP SEQUENCE movie_id_seq;
DROP TRIGGER trigger_movie_id;

CREATE TABLE moviereviews (
    movie_id INTEGER FOREIGN KEY REFERENCES movies(movie_id) ON DELETE CASCADE,
    review VARCHAR(65535)
);

INSERT INTO moviereviews (movie_id, review) VALUES(4, 'best movie world');