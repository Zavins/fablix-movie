Use `moviedb`;
CREATE TABLE `my_stopwords`
(
    value VARCHAR(30)
) ENGINE = INNODB;
SET GLOBAL innodb_ft_server_stopword_table = 'moviedb/my_stopwords';
ALTER TABLE `moviedb`.`movies`
    ADD FULLTEXT INDEX `title` (`title`);