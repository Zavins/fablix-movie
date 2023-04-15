CREATE VIEW `moviedb`.`movie_rating` AS
SELECT `m`.`id`     AS `id`,
       `r`.`rating` AS `rating`
FROM (`moviedb`.`movies` `m`
    JOIN `moviedb`.`ratings` `r`)
WHERE (`m`.`id` = `r`.`movieId`)
ORDER BY `r`.`rating` DESC