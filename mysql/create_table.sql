DROP DATABASE IF EXISTS moviedb;
CREATE DATABASE moviedb;

USE moviedb;

CREATE TABLE movies(
    `id` VARCHAR(10) NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `year` INTEGER NOT NULL NOT NULL,
    `director` VARCHAR(100),
    PRIMARY KEY (`id`)
);

CREATE TABLE stars (
    `id` VARCHAR(10) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `birthYear` INTEGER,
    PRIMARY KEY (`id`)
);

CREATE TABLE stars_in_movies(
    `starId` VARCHAR(10) NOT NULL,
    `movieId` VARCHAR(10) NOT NULL,
    FOREIGN KEY (`starId`) REFERENCES stars(`id`),
    FOREIGN KEY (`movieId`) REFERENCES movies(`id`)
);

CREATE TABLE genres (
    `id` INTEGER AUTO_INCREMENT NOT NULL,
    `name` VARCHAR(32) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE genres_in_movies(
    `genreId` INTEGER NOT NULL,
    `movieId` VARCHAR(10) NOT NULL,
    FOREIGN KEY (`genreId`) REFERENCES genres(`id`),
    FOREIGN KEY (`movieId`) REFERENCES movies(`id`)
);

CREATE TABLE creditcards(
    `id` VARCHAR(20) NOT NULL,
    `firstName` VARCHAR(50) NOT NULL,
    `lastName` VARCHAR(50) NOT NULL,
    `expiration` DATE,
    PRIMARY KEY (`id`)
);


CREATE TABLE customers(
    `id` INTEGER AUTO_INCREMENT NOT NULL,
    `firstName` VARCHAR(50) NOT NULL,
    `lastName` VARCHAR(50) NOT NULL,
    `ccId` VARCHAR(20) NOT NULL,
    `address` VARCHAR(200) NOT NULL,
    `email` VARCHAR(50) NOT NULL,
    `password` VARCHAR(20) NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`ccId`) REFERENCES creditcards(`id`)
);

CREATE TABLE sales (
    `id` INTEGER AUTO_INCREMENT NOT NULL,
    `customerId` INTEGER NOT NULL,
    `movieId` VARCHAR(10) NOT NULL,
    `saleDate` DATE NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`customerId`) REFERENCES customers(`id`),
    FOREIGN KEY (`movieId`) REFERENCES movies(`id`)
);

CREATE TABLE ratings (
    `movieId` VARCHAR(10) NOT NULL,
    `rating` FLOAT NOT NULL,
    `numVotes` INTEGER NOT NULL,
    FOREIGN KEY (`movieId`) REFERENCES movies(`id`)
);

CREATE TABLE employees (
    `email` varchar(50) primary key,
    `password` varchar(100) not null,
    `fullname` varchar(100)
);

INSERT INTO employees VALUES ('classta@email.edu', 'NCnY9S/opn+Lngue89H/QKLnKqm2AA1yAM326+j2uWJsGMITszCJwPFnXGOZcm1f', 'TA CS122B');


-- Create Views --
-- CREATE movie_list view --
CREATE VIEW `moviedb`.`movie_list` AS
SELECT `m`.`id`       AS `id`,
       `m`.`title`    AS `title`,
       `m`.`year`     AS `year`,
       `m`.`director` AS `director`,
       `g`.`name`     AS `genreName`,
       `s`.`name`     AS `starName`,
       `s`.`id`       AS `starId`,
       `r`.`rating`   AS `rating`
FROM (((((`moviedb`.`movies` `m`
    LEFT JOIN `moviedb`.`genres_in_movies` `gm` ON ((`m`.`id` = `gm`.`movieId`)))
    LEFT JOIN `moviedb`.`genres` `g` ON ((`gm`.`genreId` = `g`.`id`)))
    LEFT JOIN `moviedb`.`ratings` `r` ON ((`m`.`id` = `r`.`movieId`)))
    LEFT JOIN `moviedb`.`stars_in_movies` `sm` ON ((`m`.`id` = `sm`.`movieId`)))
    LEFT JOIN `moviedb`.`stars` `s` ON ((`sm`.`starId` = `s`.`id`)))
ORDER BY `r`.`rating` DESC;

-- CREATE movie_rating --
CREATE VIEW `moviedb`.`movie_rating` AS
SELECT `m`.`id`     AS `id`,
       `r`.`rating` AS `rating`
FROM (`moviedb`.`movies` `m`
    JOIN `moviedb`.`ratings` `r`)
WHERE (`m`.`id` = `r`.`movieId`)
ORDER BY `r`.`rating` DESC;

-- CREATE star_list view --
CREATE VIEW `moviedb`.`star_list` AS
SELECT `s`.`id`        AS `id`,
       `s`.`name`      AS `name`,
       `s`.`birthYear` AS `birthYear`,
       `m`.`id`        AS `movieId`,
       `m`.`title`     AS `title`,
       `m`.`year`      AS `year`,
       `m`.`director`  AS `director`
FROM ((`moviedb`.`stars` `s`
    JOIN `moviedb`.`movies` `m`)
    JOIN `moviedb`.`stars_in_movies` `sm`)
WHERE ((`m`.`id` = `sm`.`movieId`)
    AND (`s`.`id` = `sm`.`starId`))
ORDER BY `s`.`id`;

-- Create movie_genre_list_view
CREATE VIEW `moviedb`.`movie_genre_list_view` AS
SELECT gm.`movieId`,
       GROUP_CONCAT(
               g.`id`, '|', g.`name`
               ORDER BY g.`name` ASC
               SEPARATOR ';'
           ) AS `genreList`
FROM `moviedb`.`genres_in_movies` gm
         JOIN `moviedb`.`genres` g ON gm.`genreId` = g.`id`
GROUP BY gm.`movieId`;

-- Create movie_star_list_view
CREATE VIEW `moviedb`.`movie_star_list_view` AS
SELECT sm.`movieId`,
       GROUP_CONCAT(
               s.`id`, '|', s.`name`
               ORDER BY mp.`moviesPlayed` DESC, s.`name`
               SEPARATOR ';'
           ) AS `starList`
FROM `moviedb`.`stars_in_movies` sm
         JOIN `moviedb`.`stars` s ON sm.`starId` = s.`id`
         JOIN (SELECT sm2.`starId`, COUNT(*) AS `moviesPlayed` FROM `stars_in_movies` sm2 GROUP BY sm2.`starId`) mp
              ON sm.`starId` = mp.`starId`
GROUP BY sm.`movieId`;

