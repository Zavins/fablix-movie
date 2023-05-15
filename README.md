# Project 3

## Demo Video

https://drive.google.com/file/d/11aAU9FFsVEtUQRUG8nFTa6gMmNOyAcZF/view?usp=sharing

## Contribution

Zhiyuan Wang

- Implements employee dashboard
    - HTML, CSS, JS, for most pages
- Implements dashboard backend servlets
- Added Recaptcha
- Added Password encryption
- Fixed the prepare statement

Chengxi Li

- Registered the domain
- Changed the protocol to HTTPS
- Fixed the prepare statement
- Importing large XML data files into the Fabflix database
- Test the website and record the video

## Parser Optimization

### Without optimization

All parsing finished in 1278375ms

### With index

**Optimization 1: Index created on stars and movies table to speed up search**

> CREATE INDEX stars_name_index ON stars (name);  
> CREATE INDEX movies_title_director_index ON movies (title, director);

All parsing finished in 199572ms.

It is 1078 seconds faster than without index.

### With index and batch insert

**Optimization 2: Batch insert is used when inserting into movies, stars, and stars_in_movies tables.**

All parsing finished in 189250ms

It is 10 seconds faster than only with optimization 1.

### With index, batch insert, genre id cache, movie id cache, and star id cache

**Optimization 3: Use hash map to cache genre id, movie id, and star id to avoid querying database.
Also used hash set to check for duplicated records, avoiding querying database.
These data structures allow O(1) time complexity for checking and inserting.**

All parsing finished in 180086ms

It is 9 seconds faster than only with optimization 1 and 2.

### With index, batch insert, genre id cache, movie id cache, star id cache, and auto commit off

Optimization 4: auto commit off. (not counted)

All parsing finished in 21873ms

## Inconsistent Data

The txt files are in the ./xml-parser folder.

movie-duplicates.txt records the movies that are already in the database, which means the title, year, and director all
match an existing record.

movie-errors.txt records the movies that caused an error when parsing mains243.xml, with reason in the file.

star-duplicates.txt records the stars that are already in the database, which means the name and birth year all match an
existing record.

star-errors.txt records the stars that caused an error when parsing actors63.xml, with reason in the file.

movie-not-found.txt records the movies that are not found in movies63.xml or database when parsing casts124.xml.

star-not-found.txt records the stars that are not found in actors63.xml when parsing casts124.xml.

star-movie-errors.txt records the stars that caused an error when parsing casts124.xml, with reason in the file.

# Project 2

## Demo Video

https://drive.google.com/file/d/1Y1F7v6tdIzsz06B0dwH91mLa2tuaW_eb/view?usp=sharing

## Contribution

Zhiyuan Wang

- Implement the front-end of the pages
    - HTML, CSS, JS, for most pages
- Improve some back-end code
- Test the website
- Record the video

Chengxi Li

- Implement the back-end servlets
- Improve some front-end code
- Implement the checkout page
- Test the website

## Substring Matching

We used the `LIKE` operator in SQL to implement substring matching.
For example, if user inputs "term", we will match "%term%" with `LIKE`
operator against the corresponding attribute. To implement this, we
append `%` to the beginning and end of the input string at the front-end.
When the servlet receives the request, it inserts the input string
with `%` at both ends to the query.
Moreover, we escape `%` and `_` in the input string to avoid misinterpretation.

# Project 1

## Demo Video

https://youtu.be/POGcus8Z_00

## Contribution

Zhiyuan Wang

- Install and test the production environment on AWS machine
- Create MySQL database (tables and views)
- Create Movie List, Single Movie, Single Star pages
- Implement the framework and logic of the pages (js and servlet)
- Record the video

Chengxi Li

- Install and test the production environment on AWS machine
- Create MySQL database (tables)
- Beautify the pages using HTML and CSS (Bootstrap)
- Refactor some js code (jQuery)