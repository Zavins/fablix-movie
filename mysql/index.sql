USE moviedb;

CREATE INDEX stars_name_index ON stars (name);

CREATE INDEX movies_title_director_index ON movies (title, director);