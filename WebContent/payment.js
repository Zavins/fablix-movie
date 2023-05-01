const getPrice = () => {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/cart",
        success: (resultData) => {
            console.log(resultData)
            $("#total").html(`$${resultData["total"].toFixed(2)}`)
        }
    });
}

$("#payment-form").submit(function (e) {
    e.preventDefault();
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart/checkout",
        data: {
            firstName: $("#firstname").val(),
            lastName: $("#lastname").val(),
            creditCard: $("#card-number").val(),
            exp: $("#exp").val(),
        },
        success: (resultData) => {
            $("#content").load("payment-confirmation.html", () => {
                resultData["sales"].forEach(sale => {
                    $("#confirmation-table-body").append(`
                        <tr>
                            <td>${sale["id"]}</td>
                            <td>${sale["movieTitle"]}</td>
                            <td>${sale["quantity"]}</td>
                            <td>$10.00</td>
                            <td>$${sale["subtotal"].toFixed(2)}</td>
                        </tr>
                    `)
                });
            });
        },
        error: (message) => {
            let error = JSON.parse(message.responseText)["message"]
            $("#content").load("payment-failed.html", () => {
                $("#error-message").text(error);
                $("#back-to-payment").on("click", e => location.reload());
            });
        }
    });
});



// Load common header and footer
$("#header").load("header.html");
// $("#footer").load("footer.html");

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
$(document).ready(function () {
    getPrice()
});
