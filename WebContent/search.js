const showAdvanceSearch = () => {
    if (advanceSearch === false) {
        $("#advance-search").transition({y: '0px'})
        $("#utils-section").transition({y: '0px'})
        $("#search-button").transition({y: '60px'})
        advanceSearch = true
    } else {
        $("#utils-section").transition({y: '-60px'})
        $("#advance-search").transition({y: '-60px'})
        $("#search-button").transition({y: '0px'})
        advanceSearch = false
    }
}

var advanceSearch = false

const fillInSearchParams = () => {
    let searchParams = new URLSearchParams(window.location.search)
    $("#title").attr('value', (searchParams.get("title") ?? "").replaceAll("%", ""))
    $("#director").attr('value', (searchParams.get("director") ?? "").replaceAll("%", ""))
    $("#star").attr('value', (searchParams.get("starName") ?? "").replaceAll("%", ""))
    $("#year").attr('value', searchParams.get("year"))
    if (searchParams.get("advanced") === "true") {
        var advanceSearch = false
        showAdvanceSearch()
    }
}

$(document).ready(function () {
    setTimeout(function () {
        $('#title').autocomplete({
            minChars: 3,
            lookupLimit: 10,
            deferRequestBy: 300,
            lookup: function (query, done) {
                console.log("Autocomplete query is initiated.")
                let suggestions = sessionStorage.getItem(query)
                // if found in session storage
                if (suggestions != null) {
                    console.log("using cached results");
                    suggestions = JSON.parse(suggestions)
                    console.log(suggestions)
                    done({suggestions: suggestions})
                    return;
                }
                //if not found, call the api
                suggestions = []
                jQuery.ajax({
                    dataType: "json", // Setting return data type
                    method: "GET", // Setting request method
                    data: {"query": query},
                    url: "api/autocomplete", // Setting request url, which is mapped by StarsServlet in Stars.java
                    success: (resultData) => {
                        resultData["result"].forEach((item) => {
                            suggestions.push({value: `${item["title"]} (${item["year"]})`, data: item["id"]})
                        })
                        console.log("Send AJAX request to backend for the result")
                        console.log(suggestions)
                        sessionStorage.setItem(query, JSON.stringify(suggestions))
                        done({suggestions: suggestions});
                    },
                    complete: (e, status) => {
                        if (status === "parsererror" && !location.href.endsWith("login.html")) {
                            location.reload();
                        }
                    },
                });
            },
            onSelect: function (item) {
                location.href = `single-movie.html?id=${item["data"]}`
            }
        });
        fillInSearchParams()
    }, 500)
});