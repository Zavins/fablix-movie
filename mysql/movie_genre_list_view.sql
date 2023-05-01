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