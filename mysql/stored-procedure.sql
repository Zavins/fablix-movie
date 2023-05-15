use moviedb;
DROP PROCEDURE IF EXISTS `moviedb`.`add_star`;
DROP PROCEDURE IF EXISTS `moviedb`.`add_genre`;
DROP PROCEDURE IF EXISTS `moviedb`.`add_movie`;

-- ---------------------add star store procedure
DELIMITER //
CREATE PROCEDURE add_star(
    IN name VARCHAR(100),
    IN year INT,
    OUT new_star_id VARCHAR(10),
    OUT status VARCHAR(256)
)
EXEC:
BEGIN
    DECLARE star_id INT;
    SELECT SUBSTRING(MAX(s.id), 3, 7)
    INTO star_id
    FROM stars as s
    LIMIT 1;

    SET new_star_id = CONCAT('nm', LPAD(star_id + 1, 7, '0'));
    INSERT INTO stars (`id`, `name`, `birthYear`)
    VALUES (new_star_id, name, year);
    SET status = CONCAT('Star added ID:', new_star_id);
END
//DELIMITER ;

-- ---------------------add genre store procedure
DELIMITER //
CREATE PROCEDURE add_genre(
    IN name VARCHAR(100),
    OUT new_genre_id int,
    OUT status VARCHAR(256)
)
EXEC:
BEGIN
    DECLARE genre_exist INT;
    DECLARE genre_id INT;
    SELECT MAX(id)
    INTO genre_id
    FROM genres
    LIMIT 1;

    SELECT COUNT(*), g.id
    INTO genre_exist, genre_id
    FROM genres as g
    WHERE g.name = name
    LIMIT 1;

    IF genre_exist > 0 THEN
        SET status = CONCAT('Genre already exists. ID:', genre_id);
    ELSE
        SELECT MAX(g.id) INTO genre_id FROM genres as g LIMIT 1;
        SET new_genre_id = genre_id + 1;
        INSERT INTO genres (`id`, `name`)
        VALUES (new_genre_id, name);
        SET status = CONCAT('Genre added ID:', new_genre_id);
    END IF;
END
//DELIMITER ;

-- ---------------------add movie store procedure
DELIMITER //
CREATE PROCEDURE add_movie(
    IN title VARCHAR(100),
    IN director VARCHAR(100),
    IN year INT,
    IN star_name VARCHAR(100),
    IN birth_year INT,
    IN genre VARCHAR(100),
    OUT status VARCHAR(256)
)
EXEC:
BEGIN
    DECLARE max_movie_id INT;
    DECLARE movie_id VARCHAR(10);
    DECLARE movie_exist INT;
    DECLARE star_id VARCHAR(10);
    DECLARE star_exist INT;
    DECLARE genre_id INT;
    DECLARE genre_exist INT;

    START TRANSACTION;
    -- --------Check if the movie already exists
    SELECT COUNT(*), m.id
    INTO movie_exist, movie_id
    FROM movies as m
    WHERE m.title = title
      AND m.year = year
      AND m.director = director;

    IF movie_exist > 0 THEN
        SET status = CONCAT('Movie already exists. ID:', movie_id);
        LEAVE EXEC;
    ELSE
        SELECT SUBSTRING(MAX(m.id), 3, 7) INTO max_movie_id FROM movies as m LIMIT 1;
        SET movie_id = CONCAT('tt', LPAD(max_movie_id + 1, 7, '0'));
    END IF;

    -- --------Check if the star already exists
    IF birth_year IS NULL THEN
        SELECT COUNT(*), s.id
        INTO star_exist, star_id
        FROM stars as s
        WHERE s.name = star_name
        LIMIT 1;
    ELSE
        SELECT COUNT(*), s.id
        INTO star_exist, star_id
        FROM stars as s
        WHERE s.name = star_name
          AND s.birthYear = birth_year
        LIMIT 1;
    END IF;

    IF star_exist = 0 THEN -- if star not exists
        CALL add_star(star_name, birth_year, @star_id, @status);
        set star_id = @star_id;
    END IF;

    -- --------Check if the genre already exists
    SELECT COUNT(*), g.id
    INTO genre_exist, genre_id
    FROM genres as g
    WHERE g.name = genre
    LIMIT 1;

    IF genre_exist = 0 THEN -- if star not exists
        CALL add_genre(genre, @genre_id, @status);
        set genre_id = @genre_id;
    END IF;

    -- --------Add movie to the movie table,
    INSERT INTO movies (`id`, `title`, `director`, `year`)
    VALUES (movie_id, title, director, year);

    -- --------Add movie to stars_in_movies table,
    INSERT INTO stars_in_movies (`movieId`, `starId`)
    VALUES (movie_id, star_id);

    -- --------Add movie to genres_in_movies table,
    INSERT INTO genres_in_movies (`movieId`, `genreId`)
    VALUES (movie_id, genre_id);
    COMMIT;
    SET status = CONCAT('Movie added. ID:', movie_id);
END
//DELIMITER ;