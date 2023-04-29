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
ORDER BY `r`.`rating` DESC