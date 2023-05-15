$("#add-genre-form").submit(function (e) {
    e.preventDefault();
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/genre/add",
        data: {
            name: $("#genre").val(),
        },
        success: (message) => {
            let info = message["message"]
            $("#add-genre-feedback").html(info)
            $("#add-genre-form").trigger("reset");
        },
        error: (message) => {
            let error = JSON.parse(message.responseText)["message"]
            $("#add-genre-feedback").html(error)
        }
    });
});


// Load common header and footer
$("#header").load("header.html");