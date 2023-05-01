/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

$("#header").load("header.html");

// Example starter JavaScript for disabling form submissions if there are invalid fields
(function () {
    'use strict'

    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    let form = document.querySelectorAll('.needs-validation')[0]
    // Loop over them and prevent submission
    form.addEventListener('submit', function (event) {
        $("#login-error-feedback").html("")
        if (!form.checkValidity()) {
            event.preventDefault()
            event.stopPropagation()
        } else {
            event.preventDefault()
            let email = $('#email').val()
            let password = $('#password').val()
            jQuery.ajax({
                dataType: "json",
                method: "POST",
                data: {username: email, password: password},
                url: "api/login",
                success: (resultData) => location.href = "/index.html",
                error: (message) => {
                    let error = JSON.parse(message.responseText)["message"]
                    $("#login-error-feedback").html(error)
                }
            });
        }
        form.classList.add('was-validated')
    })
})()


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// // Makes the HTTP GET request and registers on success callback function handleStarResult
// jQuery.ajax({
//     dataType: "json", // Setting return data type
//     method: "GET", // Setting request method
//     url: "api/stars", // Setting request url, which is mapped by StarsServlet in Stars.java
//     success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
// });