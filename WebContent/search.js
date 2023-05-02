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
        fillInSearchParams()
    }, 500)
});