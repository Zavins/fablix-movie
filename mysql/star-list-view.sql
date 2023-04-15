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
ORDER BY `s`.`id`