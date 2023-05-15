## MainParser

movie-duplicates.txt records the movies that are already in the database, which means the title, year, and director all match an existing record.

movie-errors.txt records the movies that caused an error when parsing mains243.xml, with reason in the file.

## ActorParser

star-duplicates.txt records the stars that are already in the database, which means the name and birth year all match an existing record.

star-errors.txt records the stars that caused an error when parsing actors63.xml, with reason in the file.

## CastParser

movie-not-found.txt records the movies that are not found in movies63.xml or database when parsing casts124.xml.

star-not-found.txt records the stars that are not found in actors63.xml when parsing casts124.xml.

star-movie-errors.txt records the stars that caused an error when parsing casts124.xml, with reason in the file.

## Without optimization

MainParser started
Inserted 11954 movies
Ignored 115 duplicate movies
MainParser finished in 195451ms
ActorParser started
ActorParser finished in 131116ms
CastParser started
Inserted 33082 star in movie relations
15505 stars not found
273 movies not found
CastParser finished in 951803ms
All parsing finished in 1278375ms

## With index

CREATE INDEX stars_name_index ON stars (name);
CREATE INDEX movies_title_director_index ON movies (title, director);

MainParser started
Inserted 11954 movies
Ignored 115 duplicate movies
Skipped 225 movie entries with error
MainParser finished in 65175ms
ActorParser started
Inserted 6855 stars
Ignored 663 duplicate stars
Skipped 1 star entries with error
ActorParser finished in 20068ms
CastParser started
Inserted 33082 star in movie relations
15505 stars not found
273 movies not found
CastParser finished in 114325ms
All parsing finished in 199572ms
