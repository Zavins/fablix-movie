import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private String getMovieTitle(Connection conn, String movieId) throws SQLException{
        String query =
                "SELECT m.`title`\n" +
                "FROM `moviedb`.`movies` m\n" +
                "WHERE m.`id` = ?";

        System.out.println(movieId);

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, movieId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getString("title");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        JsonObject responseJsonObject = new JsonObject();

        Cart cart = (Cart) request.getSession().getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            request.getSession().setAttribute("cart", cart);
        }

        try (Connection conn = dataSource.getConnection()) {
            JsonArray result = new JsonArray();
            float total = 0;
            for (Map.Entry<String, Integer> item : cart.getItems().entrySet()) {
                JsonObject itemJsonObject = new JsonObject();
                itemJsonObject.addProperty("movieId", item.getKey());
                itemJsonObject.addProperty("movieTitle", getMovieTitle(conn, item.getKey()));
                itemJsonObject.addProperty("quantity", item.getValue());
                itemJsonObject.addProperty("price", 10);
                total += item.getValue() * 10;
                result.add(itemJsonObject);
            }
            responseJsonObject.addProperty("total", total);
            responseJsonObject.add("result", result);
            response.setStatus(200);
        }  catch (Exception e) {
            response.setStatus(400);
            responseJsonObject.addProperty("message", e.getMessage());
            e.printStackTrace();

            response.setContentType("application/json");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        JsonObject responseJsonObject = new JsonObject();

        String changeStr = request.getParameter("change");
        int change = Integer.parseInt(changeStr);
        String movieId = request.getParameter("movieId");

        Cart cart = (Cart) request.getSession().getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            request.getSession().setAttribute("cart", cart);
        }

        if (change == 1) {
            cart.addOne(movieId);
            response.setStatus(200);
        } else if (change == -1) {
            cart.removeOne(movieId);
            response.setStatus(200);
        } else {
            response.setStatus(400);
        }

        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        JsonObject responseJsonObject = new JsonObject();

        String movieId = request.getParameter("movieId");

        Cart cart = (Cart) request.getSession().getAttribute("cart");

        cart.delete(movieId);
        response.setStatus(200);

        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
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

    //    /**
//     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
//     */
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//        response.setContentType("application/json");
//
//        String id;
//        JsonObject responseJsonObject = new JsonObject();
//
//        // Get request parameters
//        try {
//            id = request.getParameter("id");
//        } catch (Exception e) {
//            response.setStatus(400);
//            responseJsonObject.addProperty("message", e.getMessage());
//            e.printStackTrace();
//
//            response.setContentType("application/json");
//            response.getWriter().write(responseJsonObject.toString());
//            return;
//        }
//
//        try (Connection conn = dataSource.getConnection()) {
//            String query =
//                    "SELECT s.`id`, s.`name`, s.`birthYear`\n" +
//                            "FROM `moviedb`.`stars` s\n" +
//                            "WHERE s.`id` = ?";
//
//            try (PreparedStatement statement = conn.prepareStatement(query)) {
//                statement.setString(1, id);
//                try (ResultSet rs = statement.executeQuery()) {
//                    rs.next();
//                    String rowId = rs.getString("id");
//                    String rowName = rs.getString("name");
//                    int rowBirthYear = rs.getInt("birthYear");
//
//                    responseJsonObject.addProperty("id", rowId);
//                    responseJsonObject.addProperty("name", rowName);
//                    responseJsonObject.addProperty("birthYear", rowBirthYear);
//
//                    responseJsonObject.add("movies", getMovies(conn, id));
//                }
//                response.setStatus(200);
//            }
//        } catch (Exception e) {
//            response.setStatus(500);
//            responseJsonObject.addProperty("message", e.getMessage());
//            e.printStackTrace();
//        }
//
//        response.setContentType("application/json");
//        response.getWriter().write(responseJsonObject.toString());
//    }
}
