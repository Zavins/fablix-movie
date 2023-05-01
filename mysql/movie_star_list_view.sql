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
