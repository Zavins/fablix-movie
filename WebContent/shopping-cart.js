function handleCartResult(result) {

    let html = ""
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < result.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + `<a href="single-movie.html?id=${result[i]['movieId']}">${result[i]['title']}</a>` + "</td>"; //Title
        rowHTML += "<td>" +
            `<div class="d-flex justify-content-center align-items-center">
             <label for="quantity"></label>
             <input type="number" min="1" step="1" 
             class="col-3 text-center rounded qty" placeholder="1"  id='${result[i]['movieId']}' value='${result[i]["quantity"]}'>
              </div>`
            + "</td>";
        rowHTML += "<td>" + `$${parseFloat(result[i]["price"]).toFixed(2)}` + "</td>";
        rowHTML += "<td>" +
            `<div class='d-flex justify-content-end align-items-center gap-3'>
            $${parseFloat(result[i]["total"]).toFixed(2)}
            <button class='btn btn-danger btn-sm trash' value='${result[i]['movieId']}'>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
              <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5Zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5Zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6Z"/>
              <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1ZM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118ZM2.5 3h11V2h-11v1Z"/>
            </svg>
            </button>
            </div>` +
            "</td>"; //Price
        rowHTML += "</tr>";
        html += rowHTML
        // Append the row created to the table body, which will refresh the page
    }
    $("#cart_table_body").html(html);
    $(".qty").on("change", (e) => {
        modifyQuantity(e.currentTarget["id"], e.currentTarget["value"])
    });
    $(".trash").on("click", (e) => {
        removeMovie(e.currentTarget["value"])
    });
}

const removeMovie = (id) => {
    jQuery.ajax({
        dataType: "json",
        method: "DELETE",
        url: "api/cart?movieId=" + id,
        success: (resultData) => {
            $("#cart-header-button").find('span').html(resultData["count"])
            updateCart()
        },
        complete: (e, status) => {
            if (status.toString().endsWith("error") && !location.href.endsWith("login.html")) {
                location.reload();
            }
        },
    });
}

const modifyQuantity = (id, qty) => {
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data: {
            movieId: id,
            quantity: qty
        },
        success: (resultData) => {
            $("#cart-header-button").find('span').html(resultData["count"])
            $("#cart-header-button").addClass("cart-add-item")
            setTimeout(function () {
                $("#cart-header-button").removeClass("cart-add-item");
            }, 200);
            updateCart()
        },
        complete: (e, status) => {
            if (status === "parsererror" && !location.href.endsWith("login.html")) {
                location.reload();
            }
        },
    });
}

const updateCart = () => {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/cart",
        success: (resultData) => {
            handleCartResult(resultData["result"])
            $("#total").html(`$${resultData["total"].toFixed(2)}`)
            $("#cart-header-button").find('span').html(resultData["count"])
            hasItem = resultData["count"] !== 0;
        }
    });
}

var hasItem = false;

// Redirect only if shopping cart not empty
$("#btn-payment").on("click", e => {
    if (hasItem) location.href = "payment.html";
})

// Load common header and footer
$("#header").load("header.html");


$(document).ready(function () {
    updateCart()
});

$("#empty-cart").on("click", () => {
    console.log("3245678")
    jQuery.ajax({
        dataType: "json",
        method: "DELETE",
        url: "api/cart",
        success: (resultData) => {
            $("#cart-header-button").find('span').html(resultData["count"])
            updateCart()
        },
        complete: (e, status) => {
            if (status.toString().endsWith("error") && !location.href.endsWith("login.html")) {
                location.reload();
            }
        },
    });
})
