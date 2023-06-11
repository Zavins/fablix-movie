# Project 5

- # General
    - #### Team: QWQ

    - #### Names: Chengxi Li, ZhiYuan Wang

    - #### Project 5 Video Demo Link:

    - #### Instruction of deployment:
        ##### For single instance version
        - Use `single-instace-with-pool` branch
        - Create database
            ```
            sudo mysql < s23-122b-qwq/mysql/create_table.sql
            sudo mysql < s23-122b-qwq/mysql/index.sql
            sudo mysql < movie-data.sql
            sudo mysql < s23-122b-qwq/mysql/stored-procedure.sql
            sudo mysql < s23-122b-qwq/mysql/alter_sales_table_add_quantity.sql
            sudo mysql < s23-122b-qwq/mysql/alter_movie_table_add_fulltext.sql
            ```
        - Update password
            ```
            cd ~/s23-122b-qwq/encryption
            mvn compile
            mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="UpdateSecurePassword"
            ```
        - Parse XML
            ```
            cd xml-parser
            cp ~/stanford-movies/*.xml .
            mvn compile
            mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="Main"
            ```
        - Deploy
            ```
            mvn package
            sudo cp ./target/*.war /var/lib/tomcat10/webapps/
            ```
        ##### For scaled version (master and slave)
        - Use `main` branch
        - Setup MySQL master slave duplication
            ```
            master-mysql> show master status;
            slave-mysql> stop slave;
            slave-mysql> CHANGE MASTER TO MASTER_HOST='<master ip>', MASTER_USER='repl', MASTER_PASSWORD='slave66Pass$word', MASTER_LOG_FILE='mysql-bin.000001', MASTER_LOG_POS=337;
            slave-mysql> start slave;
            slave-mysql> show slave status;
            ```
        - On master, create database
        - On master, update password
        - On master, parse XML
        - On master and slave, deploy war file
        - On load balancer, setup proxy and sticky session
            In apache2 config file
            ```
            Header add Set-Cookie "ROUTEID=.%{BALANCER_WORKER_ROUTE}e; path=/" env=BALANCER_ROUTE_CHANGED
            <Proxy "balancer://Session_balancer">
                BalancerMember "http://<master ip>:8080/cs122b-project" route=1
                BalancerMember "http://<slave ip>:8080/cs122b-project" route=2
            ProxySet stickysession=ROUTEID
            </Proxy> 
            ```
            and
            ```
            ProxyPass /cs122b-project balancer://Session_balancer
            ProxyPassReverse /cs122b-project balancer://Session_balancer
            ```
            and restart
            ```sudo service apache2 restart```
            

    - #### Collaborations and Work Distribution:
        - ##### Chengxi Li:
            - Setup MySQL master slave duplication (task 2)
            - Setup load balancer (task 3)
        - ##### ZhiYuan Wang:
            - Add connection pooling configuration.
            - Use jmeter to test different test cases.
            - Create [log_processing.py](/logs/log_processing.py) script to calculate the average query time.
            - Add the report the README

- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
        `WebContent/META-INF/context.xml` is the configuration file for connection pooling,
        where the following config are added/modified for connection pooling.
        ```   factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
              maxTotal="100" maxIdle="30" maxWaitMillis="10000"
              testOnBorrow="true" validationQuery="SELECT 1"
              url="jdbc:mysql://mp.fablix.tech:3306/moviedb?autoReconnect=true&amp;allowPublicKeyRetrieval=true&amp;useSSL=false&amp;cachePrepStmts=true"/>
        ```
        
        Specifically, we added `testOnBorrow="true" validationQuery="SELECT 1"` to avoid SQL connection error,
        and `cachePrepStmts=true` to utilize prepared statement cache.
        
        The prepared statements are used in
        - `AutoCompleteServlet` (in `src/servelets`)
        - `CartCheckoutServlet`
        - `LoginServlet`
        - `MovieServlet`
        - `MoviesServlet`
        - `StarServlet`
        - `LoginServlet` (in `src/servelets/_dashboard`)
        - `MetadataServlet`
        - `Utils` (in `src/utils`)

    - #### Explain how Connection Pooling is utilized in the Fabflix code.
        In servlets where SQL connection is needed, we first get a dataSource from the context.
        ```dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_ro")```.
        Then, we get a connection from the pool by `dataSource.getConnection()`.
        
        The server has some connections in the pool. 
        When a servlet needs a connection, it takes one from the pool.
        When the servlet closes the connection, the connection is returned to the pool for future reuse.

    - #### Explain how Connection Pooling works with two backend SQL.
        In the scaled version, when we have two datasources, 
        one for the read/write master database and the other for the read only database (will be routed to master or slave).
        We added the config for connection pooling to each datasource.
        In this way, each datasource/database has its own pool of connections.

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
        Datasource config: `WebContent/META-INF/context.xml` and `WebContent/WEB-INF/web.xml`
        
        Servlets that use read-only datasource (routed to master or slave database):
        - `src/servelets/AutoCompleteServlet.java`
        - `src/servelets/CartServlet.java`
        - `src/servelets/GenresServlet.java`
        - `src/servelets/LoginServlet.java`
        - `src/servelets/MovieServlet.java`
        - `src/servelets/MoviesServlet.java`
        - `src/servelets/StarServlet.java`
        - `src/servelets/StarsServlet.java`
        - `src/servelets/_dashboard/LoginServlet.java`
        - `src/servelets/_dashboard/MetadataServlet.java`
        
        Servlets that use read/write datasource (routed to master)
        - `src/servelets/CartCheckoutServlet.java`
        - `src/servelets/_dashboard/AddGenreServlet.java`
        - `src/servelets/_dashboard/AddMovieServlet.java`
        - `src/servelets/_dashboard/AddStarServlet.java`

    - #### How read/write requests were routed to Master/Slave SQL?
        We created two datasources, 
        `moviedb_rw` for the read/write database (routed to master) 
        and `moviedb_ro` for the read only database (routed to master or slave).
        The read/write datasource `moviedb_rw` is connected to the master database.
        The read-only datasource `moviedb_ro` is connected to both master and slave databases,
        where requests are distributed evenly using the load balancing feature provided by MySQL Connector/J
        ([doc](https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-usagenotes-j2ee-concepts-managing-load-balanced-connections.html)).
        
        The load balance is configured like `url="jdbc:mysql:loadbalance://<master ip>:3306,<slave ip>:3306/moviedb?<...>"`.
        By default, it uses a round-robin load balancing strategy.
        
        Codes sending write SQL like `insert` will use the read/write datasource `moviedb_rw`, 
        whereas codes sending `select` SQL will use the read-only datasource `moviedb_ro`. Files are listed above.

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
