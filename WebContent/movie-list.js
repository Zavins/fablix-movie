/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieListResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#movie_list_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + `<a href="single-movie.html?id=${resultData[i]['id']}">${resultData[i]['title']}</a>` + "</td>"; //Title
        rowHTML += "<td>" + resultData[i]["year"] + "</td>"; //Year
        rowHTML += "<td>" + resultData[i]["director"] + "</td>"; //Director
        rowHTML += "<td>" + resultData[i]["genres"] + "</td>"; //Genres
        rowHTML += `<td>${resultData[i]["stars"].map(
            (star) => `<a href="single-star.html?id=${star['id']}">${star['name']}</a>`
        ).join(", ")}</td>`; //Stars
        rowHTML += "<td>" + resultData[i]["rating"] + "</td>"; //Rating
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movie-list",
    success: (resultData) => handleMovieListResult(resultData)
});