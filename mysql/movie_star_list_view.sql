CREATE VIEW `moviedb`.`movie_star_list_view` AS
SELECT sm.`movieId`,
       GROUP_CONCAT(
               s.`id`, '|', s.`name`
               ORDER BY s.`name` ASC
               SEPARATOR ';'
           ) AS `starList`
FROM `moviedb`.`stars_in_movies` sm
         JOIN `moviedb`.`stars` s ON sm.`starId` = s.`id`
GROUP BY sm.`movieId`;