package servelets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Cart;
import models.User;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

import static utils.Utils.getMovieTitle;

@WebServlet(name = "servelets.CartCheckoutServlet", urlPatterns = "/api/cart/checkout")
public class CartCheckoutServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_rw");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    private boolean verifyCreditCard(Connection conn, String creditCard, String firstName, String lastName, String exp) throws SQLException {
        String query =
                "SELECT EXISTS(SELECT * FROM `moviedb`.`creditcards` c\n" +
                        "WHERE `id` = ? AND `firstName` = ? AND `lastName` = ? AND `expiration` = ?)";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, creditCard);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, exp);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        }
    }

    private int insertSale(Connection conn, int customerId, String movieId, int quantity) throws SQLException {
        String insertQuery =
                "INSERT INTO `moviedb`.`sales` (customerId, movieId, saleDate, quantity) VALUES\n" +
                        "(?, ?, CURDATE(), ?)";

        try (PreparedStatement statement = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, customerId);
            statement.setString(2, movieId);
            statement.setInt(3, quantity);

            statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        JsonObject responseJsonObject = new JsonObject();

        String creditCard = request.getParameter("creditCard");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String exp = request.getParameter("exp");

        try (Connection conn = dataSource.getConnection()) {
            if (verifyCreditCard(conn, creditCard, firstName, lastName, exp)) {
                JsonArray sales = new JsonArray();
                Cart cart = (Cart) request.getSession().getAttribute("cart");
                float total = 0;
                for (Map.Entry<String, Integer> item : cart.getItems().entrySet()) {
                    JsonObject saleJsonObject = new JsonObject();

                    User user = (User) request.getSession().getAttribute("user");
                    int customerId = user.getId();
                    String movieId = item.getKey();
                    String movieTitle = getMovieTitle(conn, movieId);
                    int quantity = item.getValue();
                    int saleId = insertSale(conn, customerId, movieId, quantity);
                    float price = 10;

                    saleJsonObject.addProperty("id", saleId);
                    saleJsonObject.addProperty("movieTitle", movieTitle);
                    saleJsonObject.addProperty("quantity", quantity);
                    saleJsonObject.addProperty("subtotal", price * quantity);

                    total += price * quantity;
                    sales.add(saleJsonObject);
                }
                responseJsonObject.add("sales", sales);
                responseJsonObject.addProperty("total", total);
                cart.deleteAll();
                response.setStatus(200);
            } else {
                response.setStatus(402);
                responseJsonObject.addProperty("message", "Incorrect credit card information.");
            }
        } catch (Exception e) {
            response.setStatus(400);
            responseJsonObject.addProperty("message", e.getMessage());
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }
}
