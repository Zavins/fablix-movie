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

## With index and batch insert

Batch insert is used when inserting into movies, stars, and stars_in_movies tables.

MainParser started
Inserted 12014 movies
Ignored 52 duplicate movies
Skipped 228 movie entries with error
MainParser finished in 63262ms
ActorParser started
Inserted 6209 stars
Ignored 653 duplicate stars
Skipped 1 star entries with error
ActorParser finished in 18514ms
CastParser started
Inserted 33103 star in movie relations
15505 stars not found
252 movies not found
CastParser finished in 107470ms
All parsing finished in 189250ms

# With index, batch insert, and genre id cache

MainParser started
Inserted 12014 movies
Ignored 52 duplicate movies
Skipped 227 movie entries with error
MainParser finished in 63665ms
ActorParser started
Inserted 6209 stars
Ignored 653 duplicate stars
Skipped 1 star entries with error
ActorParser finished in 17760ms
CastParser started
Inserted 33103 star in movie relations
15505 stars not found
252 movies not found
CastParser finished in 102119ms
All parsing finished in 183549ms

# With index, batch insert, genre id cache, movie id cache, and star id cache

MainParser started
Inserted 12014 movies
Ignored 52 duplicate movies
Skipped 227 movie entries with error
MainParser finished in 66282ms
ActorParser started
Inserted 6209 stars
Ignored 653 duplicate stars
Skipped 1 star entries with error
ActorParser finished in 18094ms
CastParser started
Inserted 33103 star in movie relations
15505 stars not found
252 movies not found
CastParser finished in 95706ms
All parsing finished in 180086ms

# With index, batch insert, genre id cache, movie id cache, star id cache, and auto commit off

MainParser started
Inserted 12014 movies
Ignored 52 duplicate movies
Skipped 227 movie entries with error
MainParser finished in 8688ms
ActorParser started
Inserted 6209 stars
Ignored 653 duplicate stars
Skipped 1 star entries with error
ActorParser finished in 2682ms
CastParser started
Inserted 33103 star in movie relations
15505 stars not found
252 movies not found
CastParser finished in 10501ms
All parsing finished in 21873ms