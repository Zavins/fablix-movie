const LETTERS = [
    'A', 'B', 'C', 'D', 'E', 'F', 'G',
    'H', 'I', 'J', 'K', 'L', 'M', 'N',
    'O', 'P', 'Q', 'R', 'S', 'T', 'U',
    'V', 'W', 'X', 'Y', 'Z', '*',
]

const NUMBERS = [
    '1', '2', '3',
    '4', '5', '6',
    '7', '8', '9',
    '0',
]

const clearBrowseSection = () => {
    $("#browse-letters").html("")
    $("#browse-numbers").html("")
    $("#browse-genres").html("")
}
const browseByGenres = () => {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/genres", // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (genres) => {
            $("#browse-genres").html(
                genres["result"].map((genre) => {
                    let button = `<a role="button" class='btn btn-secondary' href='/movie-list.html?genre=${genre["id"]}'>
                    ${genre["name"]}
                    </a>`
                    return "<div class='col cell-8-1 animate-cell'>" + button + "</div>"
                })
            )
        } // Setting callback function to handle data returned successfully by the StarsServlet
    });

}

const browseBySubstrings = () => {

    $("#browse-letters").html(
        LETTERS.map((text) => {
            let params = {title: text === "*" ? "%" : `${text}%`}
            let button = `<a role='button' href='/movie-list.html?${new URLSearchParams(params).toString()}'
            class='btn btn-secondary' aria-label='${text}'>${text}</a>`

            if (text === " ") button = ""
            return "<div class='col cell-7-1 animate-cell'>" + button + "</div>"
        })
    )

    $("#browse-numbers").html(
        NUMBERS.map((text) => {
            let params = {title: `${text}%`}
            let button = `<a role='button' href='/movie-list.html?${new URLSearchParams(params).toString()}'
            class='btn btn-secondary' aria-label='${text}'>${text}</a>`

            if (text === " ") button = ""
            return "<div class='col cell-3-1 animate-cell'>" + button + "</div>"
        })
    )
}


$("#header").load("header.html");
$("#search-form").load("search.html");

$("#browse-button").on("click", (e) => {
    e.preventDefault()
    clearBrowseSection()
    if (e.target.innerHTML === "Browse by substrings") {
        browseBySubstrings()
        e.target.innerHTML = "Browse by genres"
    } else {
        browseByGenres()
        e.target.innerHTML = "Browse by substrings"
    }
})

$("#advance-search-button").on("click", (e) => {
    e.preventDefault()
    if (e.target.innerHTML === "Advance search") {
        showAdvanceSearch()
        e.target.innerHTML = "Hide advance Search"
    } else {
        showAdvanceSearch()
        e.target.innerHTML = "Advance search"
    }
})

$("#search-form").on("submit", function (e) {
    e.preventDefault()
    let title = $("#title").val()
    let director = $("#director").val()
    let starName = $("#star").val()
    let year = $("#year").val()

    let params = {
        count: 25,
        title: title ? `%${title}%` : '',
        director: director && advanceSearch ? `%${director}%` : '',
        starName: director && advanceSearch ? `%${starName}%` : '',
        year: year && advanceSearch ? year : '',
        advanced: advanceSearch,
    }

    location.href = "movie-list.html?" + new URLSearchParams(params).toString();
})


//if advance search, search by title, else search everything else
browseByGenres()

