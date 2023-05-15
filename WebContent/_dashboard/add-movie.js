$("#add-movie-form").submit(function (e) {
    e.preventDefault();
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/movie/add",
        data: {
            title: $("#title").val(),
            director: $("#director").val(),
            year: $("#year").val(),
            starName: $("#star-name").val(),
            birthYear: $("#birth-year").val(),
            genre: $("#genre").val()
        },
        success: (message) => {
            let info = message["message"]
            $("#add-movie-feedback").html(info)
            $("#add-movie-form").trigger("reset");
        },
        error: (message) => {
            let error = JSON.parse(message.responseText)["message"]
            $("#add-movie-feedback").html(error)
        }
    });
});


// Load common header and footer
$("#header").load("header.html");