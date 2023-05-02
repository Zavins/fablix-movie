const addCartHandler = () => {
    $(".cart").on("click", (e) => {
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
}

function handleMovieListResult(result) {
    // Populate the star table
    // Find the empty table body by id "star_table_body"

    let html = ""
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < result.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + `<a href="single-movie.html?id=${result[i]['id']}">${result[i]['title']}</a>` + "</td>"; //Title
        rowHTML += "<td>" + result[i]["year"] + "</td>"; //Year
        rowHTML += "<td>" + result[i]["director"] + "</td>"; //Director
        rowHTML += `<td>${result[i]["genres"].map(
            (genre) => `<a href="movie-list.html?genre=${genre['id']}">${genre['name']}</a>`
        ).join(", ")}</td>`; //Genres
        rowHTML += `<td>${result[i]["stars"].map(
            (star) => `<a href="single-star.html?id=${star['id']}">${star['name']}</a>`
        ).join(", ")}</td>`; //Stars
        rowHTML += "<td>" + result[i]["rating"] + "</td>"; //Rating
        rowHTML += "<td>" +
            `<div class='d-flex justify-content-end align-items-center gap-2'>
            $10.00
            <button class='btn btn-success cart' value='${result[i]['id']}'>
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-cart-plus" viewBox="0 0 16 16">
                  <path d="M9 5.5a.5.5 0 0 0-1 0V7H6.5a.5.5 0 0 0 0 1H8v1.5a.5.5 0 0 0 1 0V8h1.5a.5.5 0 0 0 0-1H9V5.5z"/>
                  <path d="M.5 1a.5.5 0 0 0 0 1h1.11l.401 1.607 1.498 7.985A.5.5 0 0 0 4 12h1a2 2 0 1 0 0 4 2 2 0 0 0 0-4h7a2 2 0 1 0 0 4 2 2 0 0 0 0-4h1a.5.5 0 0 0 .491-.408l1.5-8A.5.5 0 0 0 14.5 3H2.89l-.405-1.621A.5.5 0 0 0 2 1H.5zm3.915 10L3.102 4h10.796l-1.313 7h-8.17zM6 14a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm7 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
                </svg>
            </button>
            </div>` +
            "</td>"; //Price
        rowHTML += "</tr>";
        html += rowHTML
        // Append the row created to the table body, which will refresh the page
    }
    $("#movie_list_table_body").html(html);
    addCartHandler()
}

const fetchResult = () => {
    $("#movie_list_table_body").html("")
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies",
        data: params,
        success: (resultData, textStatus, xhr) => {
            handleMovieListResult(resultData["result"])
            setPagination(resultData["numPages"])
        },
        complete: (e, status) => {
            if (status === "parsererror" && !location.href.endsWith("login.html")) {
                location.reload();
            }
        },
    });
}

const fetchPreviousResult = () => {
    $("#movie_list_table_body").html("")
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies",
        data: {"usePrevious": 1},
        success: (resultData, textStatus, xhr) => {
            handleMovieListResult(resultData["result"])
            setPagination(resultData["numPages"])
            uesPreviousParams(resultData)
        },
        complete: (e, status) => {
            if (status === "parsererror" && !location.href.endsWith("login.html")) {
                location.reload();
            }
        },
    });
}

const setPagination = (pageCount) => {
    let pageNumbers = [...Array(pageCount).keys()]
    let currentPage = parseInt(params["page"])
    if (currentPage <= 5) {
        pageNumbers = pageNumbers.slice(0, Math.min(pageCount, 10))
    } else if (currentPage >= pageCount - 5) {
        pageNumbers = pageNumbers.slice(Math.max(pageCount - 10, 0), pageCount)
    } else {
        pageNumbers = pageNumbers.slice(currentPage - 5, currentPage + 5)
    }

    let disablePrev = ""
    let disableNext = ""
    if (currentPage <= 1) {
        disablePrev = "disabled text-secondary"
    }
    if (currentPage >= pageCount) {
        disableNext = "disabled text-secondary"
    }
    $("#pagination").html(
        `<li class="page-item"><a class="page-link bg-dark border-secondary ${disablePrev}" href="#" >Previous</a></li>
        ${
            pageNumbers.map((page) => {
                let active = ""
                if (page + 1 == currentPage) active = "active"
                return "<li class='" + `page-item ${active}` + "'>" +
                    "<a class='page-link bg-dark border-secondary' href='#'>" + (page + 1).toString() + "</a>" +
                    "</li>"
            }).join(' ')
        }
        <li class="page-item"><a class="page-link bg-dark border-secondary ${disableNext}" href="#">Next</a></li>`
    )
    $(".page-link").on("click", (e) => {
        let page = e.currentTarget.innerHTML
        if (page === "Previous") {
            page = currentPage - 1
        } else if (page === "Next") {
            page = currentPage + 1
        }
        updatePageNum(page)
        setPagination(pageCount)
    })
}


const updateSortAndCount = () => {
    let sort = $("#sort").val()
    let order = $("#order").val()
    let count = $("#count").val()
    params["count"] = count
    params["sortBy"] = sort[0] + order[0] + sort[1] + order[1]
    params["page"] = 1
    history.replaceState({}, null, "/movie-list.html?" + new URLSearchParams(params).toString());
    fetchResult()
}

const fillInSortAndCount = (sortBy, count) => {
    $("#sort").val(sortBy[0] + sortBy[2])
    $("#order").val(sortBy[1] + sortBy[3])
    $("#count").val(count)
}

const updateSearchString = () => {
    let title = $("#title").val()
    let director = $("#director").val()
    let starName = $("#star").val()
    let year = $("#year").val()
    params["title"] = title ? `%${title}%` : '%'
    params["director"] = director && advanceSearch ? `%${director}%` : ''
    params["starName"] = starName && advanceSearch ? `%${starName}%` : ''
    params["year"] = year && advanceSearch ? year : ''
    params["page"] = 1
    params["advanced"] = advanceSearch
    history.replaceState({}, null, "/movie-list.html?" + new URLSearchParams(params).toString());
    fetchResult()
}

const updatePageNum = (page) => {
    params["page"] = page
    history.replaceState({}, null, "/movie-list.html?" + new URLSearchParams(params).toString());
    fetchResult()
}


const prepareParams = (params) => {
    return {
        usePrevious: params.get("usePrevious") ?? "0",
        count: params.get("count") ?? 25,
        title: params.get("title") ?? "%",
        year: params.get("year") ?? "",
        director: params.get("director") ?? "",
        starName: params.get("starName") ?? "",
        genre: params.get("genre") ?? "",
        page: params.get("page") ?? 1,
        sortBy: params.get("sortBy") ?? "rdta",
        advanced: params.get("advanced") ?? ""
    }
}


const uesPreviousParams = (resultData) => {
    console.log(resultData)
    let paramMap = new Map();
    paramMap.set("count", resultData["count"] ?? "")
    paramMap.set("director", resultData["director"] ?? "")
    paramMap.set("title", resultData["title"] ?? "")
    paramMap.set("starName", resultData["starName"] ?? "")
    paramMap.set("year", resultData["year"] ?? "")
    paramMap.set("sortBy", resultData["sortBy"] ?? "")
    paramMap.set("page", resultData["page"] ?? "")
    paramMap.set("genreId", resultData["genreId"] ?? "")
    paramMap.set("advanced", resultData["advanced"] ?? "")
    paramMap.set("usePrevious", 0)
    params = prepareParams(paramMap)
    history.replaceState({}, null, "/movie-list.html?" + new URLSearchParams(params).toString());
    fillInSearchParams()
    fillInSortAndCount(resultData["sortBy"], resultData["count"])
}


const initMovieList = () => {
    let searchParams = new URLSearchParams(window.location.search)
    if (searchParams.get("usePrevious") === "1") {
        fetchPreviousResult()
    } else {
        let sortBy = searchParams.get("sortBy") ?? "rdta"
        let count = searchParams.get("count") ?? "25"
        fillInSortAndCount(sortBy, count)
    }
    params = prepareParams(searchParams)
    fetchResult()
}

$("#search-form").on("submit", function (e) {
    e.preventDefault()
    updateSearchString()
})


// Load common header and footer
$("#header").load("header.html");
$("#search-form").load("search.html");

$("#order, #sort, #count").on("change", (e) => {
    updateSortAndCount()
})

$("#advance-search-button").on("click", (e) => {
    console.log("advance search")
    e.preventDefault()
    if (e.target.innerHTML === "Advance search") {
        showAdvanceSearch()
        e.target.innerHTML = "Hide advance Search"
    } else {
        showAdvanceSearch()
        e.target.innerHTML = "Advance search"
    }
})


var params = {}
$(document).ready(function () {
    initMovieList()
});

