# Project 5

- # General
    - #### Team: QWQ

    - #### Names: Chengxi Li, ZhiYuan Wang

    - #### Project 5 Video Demo Link:

    - #### Instruction of deployment:
        - TODO: LEO

    - #### Collaborations and Work Distribution:
        - TODO: LEO
        - ##### ZhiYuan Wang:
            - Add connection pooling configuration.
            - Use jmeter to test different test cases.
            - Create [log_processing.py](/logs/log_processing.py) script to calculate the average query time.
            - Add the report the README

- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.

    - #### Explain how Connection Pooling is utilized in the Fabflix code.

    - #### Explain how Connection Pooling works with two backend SQL.


- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.

    - #### How read/write requests were routed to Master/Slave SQL?


- # JMeter TS/TJ Time Logs
    - #### Single-instance cases:
        - Use HTTP, without using Connection Pooling, 10 threads in JMeter ([Log](/logs/http_10_single_no_pooling.txt)).
        - Use HTTP, 1 thread in JMeter ([Log](/logs/http_1_single_pooling.txt)).
        - Use HTTP, 10 threads in JMeter ([Log](/logs/http_10_single_pooling.txt)).
        - Use HTTPS, 10 threads in JMeter ([Log](/logs/https_10_single_pooling.txt)).
    - #### Scaled-version cases:
        - Use HTTP, without using Connection Pooling, 10 threads in
          JMeter ([Master Log](/logs/http_10_scaled_no_pooling_master.txt), [Slave Log](/logs/http_10_scaled_no_pooling_slave.txt)).
        - Use HTTP, 1 thread in
          JMeter ([Master Log](/logs/http_1_scaled_pooling_master.txt), [Slave Log](/logs/http_1_scaled_pooling_slave.txt)).
        - Use HTTP, 10 threads in
          JMeter ([Master Log](/logs/http_10_scaled_pooling_master.txt), [Slave Log](/logs/http_10_scaled_pooling_slave.txt)).

    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
        - Install Python.
        - Run:
      > python log_processing.py log_file [log_file2 ...]

- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**         | **Graph Results Screenshot**                                      | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|-----------------------------------------------|-------------------------------------------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                         | ![HTTP 1 Single With Pooling](/img/http_1_single_pooling.png)     | 988                        | 869.74                              | 869.1                     | ??           |
| Case 2: HTTP/10 threads                       | ![HTTP 10 Single With Pooling](/img/http_10_single_pooling.png)   | 4651                       | 4553.56                             | 4553.13                   | ??           |
| Case 3: HTTPS/10 threads                      | ![HTTPS 10 Single With Pooling](/img/https_10_single_pooling.png) | 4707                       | 4605.54                             | 4605.16                   | ??           |
| Case 4: HTTP/10 threads/No connection pooling | ![HTTP 10 Single No Pooling](/img/http_10_single_no_pooling.png)  | 4691                       | 4518.59                             | 3700.81                   | ??           |

| **Scaled Version Test Plan**                  | **Graph Results Screenshot**                                     | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|-----------------------------------------------|------------------------------------------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                         | ![HTTP 1 Scaled With Pooling](/img/http_1_scaled_pooling.png)    | 979                        | 857.56                              | 856.89                    | ??           |
| Case 2: HTTP/10 threads                       | ![HTTP 10 Scaled With Pooling](/img/http_10_scaled_pooling.png)  | 2864                       | 2762.67                             | 2761.54                   | ??           |
| Case 3: HTTP/10 threads/No connection pooling | ![HTTP 10 Scaled No Pooling](/img/http_10_scaled_no_pooling.png) | 2908                       | 2804.5                              | 2803.44                   | ??           |

# Project 4

## Demo Video

https://drive.google.com/file/d/1_41hdU4QCe2Q8b5xGA3j6kMtOmjk25nc/view?usp=sharing

## Android Repository

https://github.com/UCI-Chenli-teaching/cs-122b-qwq-android

## Contribution

Zhiyuan Wang

- Implements Android app
- Implements full-text search
- Implements autocomplete frontend
- Record the video

Chengxi Li

- Implements fuzzy search
- fixed full-text search
- prepared and tested deployment website

## Fuzzy Search

Fuzzy search is implemented using both SQL LIKE and Levenshtein distance.

The SQL LIKE is used to find the movie title that contains (not just starts with)
the tokens in the keyword.
To avoid too many results and
keep the results meaningful, we only consider the tokens that are at least 5 characters long.

The Levenshtein distance is used to find the movie title that is similar to the keyword.
We check if title and keyword have a distance less than the threshold.
The distance threshold is calculated as length(keyword) / 4.

To combine with full-text search, we union the result. However, the full-text search
result is always ranked higher than the fuzzy search result.

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

## PreparedStatement Usage

- src\servelets\CartCheckoutServlet.java
- src\servelets\LoginServlet.java
- src\servelets\MovieServlet.java
- src\servelets\MoviesServlet.java
- src\servelets\StarServlet.java
- src\utils\Utils.java
- src\servelets\_dashboard\LoginServlet.java
- src\servelets\_dashboard\MetadataServlet.java
- xml-parser\src\main\java\ActorParser.java
- xml-parser\src\main\java\CastParser.java
- xml-parser\src\main\java\MainParser.java

## Parser Optimization

### Summary

Optimizations:

- Batch insert is used when inserting into movies, stars, and stars_in_movies tables.
- Use hash map to cache genre id, movie id, and star id to avoid querying database.
  Also used hash set to check for duplicated records, avoiding querying database.
  These data structures allow O(1) time complexity for checking and inserting.
- Index created on stars and movies table to speed up search

Total speed up 1257 seconds. (Original parsing time = 1278 sseconds; optimized parsing time = 22 seconds)

*Below is detail*

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

The txt files are in the `./xml-parser` folder.

`movie-duplicates.txt` records the movies that are already in the database, which means the title, year, and director
all
match an existing record.

`movie-errors.txt` records the movies that caused an error when parsing mains243.xml, with reason in the file.

`star-duplicates.txt` records the stars that are already in the database, which means the name and birth year all match
an
existing record.

`star-errors.txt` records the stars that caused an error when parsing actors63.xml, with reason in the file.

`movie-not-found.txt` records the movies that are not found in movies63.xml or database when parsing casts124.xml.

`star-not-found.txt` records the stars that are not found in actors63.xml when parsing casts124.xml.

`star-movie-errors.txt` records the stars that caused an error when parsing casts124.xml, with reason in the file.

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
