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