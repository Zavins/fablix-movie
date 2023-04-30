### Endpoint: POST /api/login

Request:
- username: String
- password: String

Response:
- (status code):
  - 200 - OK
  - 401 - Credential Incorrect
  - 500 - Internal Server Error
- (session):
  - User: User Class
    - name: String

### POST /api/movies

**Request**
- count?: int

    Number of records per page. (10, 25, 50, 100)
- title?: String

    Can use wild card (% and _)
- year?: int
- director?: String

    Can use wild card
- starName?: String

    Can use wild card
- page?: int

    Pagination index, start with 1. Default = 1
- genreCount?: int

    Number of genres returned. Default = 3
- starCount?: int

    Number of stars returned. Default = 3

Note: must have al least one of (title, year, starName, director).
If no parameter is provided, it will use the previous query saved
in the session.

**Response**
- numPages: int

    Total number of pages
- result: List
  - id: String
  - title: String
  - year: int
  - director: String
  - rating: float
  - genres: List

    Sorted by alphabetical order.
  - stars: List

    Sorted by number of movies played, desc.\
    Use alphabetical order to break ties.

### POST /api/movie

**Request**
- id: String
- genreCount?: int

  (UNUSED) Number of genres returned
- starCount?: int

  (UNUSED) Number of stars returned

**Response**
- id: String
- title: String
- year: int
- director: String
- rating: float
- genres: List

  Sorted by alphabetical order.
- stars: List

  Sorted by number of movies played, desc.\
  Use alphabetical order to break ties.

### POST /api/star

**Request**
- id: String

**Response**
- id: String
- name: String
- birthYear: int
- movies: List
  - id: String
  - title: String

  sorted by year, desc.\
  Use alphabetical order to break ties. 
