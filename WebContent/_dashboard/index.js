const renderMetadata = (metadata) => {
    let metadataHtml = "";
    for (const table in metadata) {
        metadataHtml += `<table class='table table-striped table-dark table-hover caption-top border border-secondary mt-2'>
        <caption class="h2 text-center text-info">${table}</caption>
        <thead>
            <tr>
            <th>Attribute</th>
            <th>Type</th>
            </tr>
        </thead>
        <tbody>
        ${metadata[table].map((item) => (
            "<tr>" +
            "<td>" + item['attribute'] + "</td>" +
            "<td>" + item['type'] + "</td>" +
            "</tr>"
        )).join('')}
        </tbody>
        </table>`
    }
    $("#metadata").html(metadataHtml);
}


const getMetadata = () => {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/metadata", // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (metadata) => {
            renderMetadata(metadata)
        } // Setting callback function to handle data returned successfully by the StarsServlet
    });

}


$("#header").load("header.html");
getMetadata()

