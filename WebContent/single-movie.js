/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    // populate the movie info h3
    $("#movie-info-title").text(resultData["title"]);
    $("#movie-info-director").text(resultData["director"]);
    $("#movie-info-year").text(resultData["year"]);
    $("#movie-info-genres").html(
        resultData["genres"].map(
            (genre) => `<a href="index.html?genre=${genre['id']}">${genre['name']}</a>`
        ).join(", ")
    );
    $("#movie-info-rating").text(resultData["rating"]);
    $("#movie-info-stars").html(
        resultData["stars"].map(
            (star) => `<a href="single-star.html?id=${star['id']}">${star['name']}</a>`
        ).join(", ")
    );
    $("#add-to-cart").attr("value", resultData["id"])
}

// Load common header and footer
$("#header").load("header.html");

$("#add-to-cart").on("click", (e) => {
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data: {
            movieId: e.currentTarget['value'],
            change: 1
        },
        success: (resultData) => {
            $("#cart-header-button").find('span').html(resultData["count"])
            $("#cart-header-button").addClass("cart-add-item")
            setTimeout(function () {
                $("#cart-header-button").removeClass("cart-add-item");
            }, 200);
        },
        complete: (e, status) => {
            if (status === "parsererror" && !location.href.endsWith("login.html")) {
                location.reload();
            }
        },
    });
})
// $("#footer").load("footer.html");

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/movie?id=" + movieId,
    success: (resultData) => handleResult(resultData)
});