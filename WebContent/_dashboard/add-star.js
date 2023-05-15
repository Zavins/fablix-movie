$("#add-star-form").submit(function (e) {
    e.preventDefault();
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/star/add",
        data: {
            name: $("#name").val(),
            year: $("#year").val(),
        },
        success: (message) => {
            let info = message["message"]
            $("#add-star-feedback").html(info)
            $("#add-star-form").trigger("reset");
        },
        error: (message) => {
            let error = JSON.parse(message.responseText)["message"]
            $("#add-star-feedback").html(error)
        }
    });
});


// Load common header and footer
$("#header").load("header.html");