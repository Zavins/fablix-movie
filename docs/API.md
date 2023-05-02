### POST /api/login

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

### GET /api/movies

**Request**
- usePrevious: String
  
  Use the search parameter in the previous search. "0" or "1".

- count?: int

  Number of records per page. (10, 25, 50, 100)

- title?: String

  Can use wild card (% and _)

- year?: int

- director?: String

  Can use wild card

- starName?: String

  Can use wild card

- genre?: int

  Genre id

- page?: int

  Pagination index, start with 1. Default = 1

- genreCount?: int

  (UNUSED) Number of genres returned. Default = 3

- starCount?: int

  (UNUSED) Number of stars returned. Default = 3

- sortBy?: String

  Array of attributes and order, which can be "title" or "rating".\
  Separate by spaces. Example: "title DESC, rating ASC".

Note: 
must have al least one of (title, year, starName, director, genre).
If no parameter is provided, it will use the previous query saved
in the session.

**Response**
- (status code)
  - 200 - OK
  - 400 - Bad request

- numPages: int

    Total number of pages
- count: int
- title: String
- year: int
- director: String
- starName: String
- page: int
- sortBy: String
- result: List[Object]
  - id: String
  - title: String
  - year: int
  - director: String
  - rating: float
  - genres: List
    - id: int
    - name: String

    Sorted by alphabetical order.
  - stars: List
    - id: String
    - name: String

    Sorted by number of movies played, desc.\
    Use alphabetical order to break ties.

### GET /api/movie

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
- genres: List[Object]
  - id: String
  - name: String

  Sorted by alphabetical order.
- stars: List[Object]
  - id: String
  - name: String

  Sorted by number of movies played, desc.\
  Use alphabetical order to break ties.

### GET /api/star

**Request**
- id: String

**Response**
- id: String
- name: String
- birthYear: int
- movies: List[Object]
  - id: String
  - title: String
  - year: int
  - director: String

  sorted by year, desc.\
  Use alphabetical order to break ties. 

### GET /api/genres

**Request**

None

**Response**
- result: List[Object]
  - id: int
  - name: String

  List of genres.

### GET /api/cart

**Request**

None

**Response**

- result: List[Object]
  - movieId: String
  - movieTitle: String
  - quantity: int
  - price: float
- total: float

### POST /api/cart

Increase or decrease the quantity of a cart item.

Used Put not Patch because HttpServlet does not support Patch.

**Request**

- movieId: String
- change: int

  Change in quantity. Can be +1 or -1.

**Response**

- (status code)
  - 200 - OK
  - ...

### DELETE /api/cart

Delete a movie from shopping cart.

**Request**

- movieId: String

**Response**

- (status code)
  - 200 - OK
  - ...

### POST /api/cart/checkout

Place order.

**Request**

- creditCard: String
- firstName: String
- lastName: String
- exp: String

  Should be 'yyyy/mm/dd'

**Response**

- (status code)
  - 200 - OK
  - 402 - Payment Required (incorrect credit card)
- sales: List[Object]
  - id: int
  - movieTitle: String
  - quantity: int
  - subtotal: float
- total: float