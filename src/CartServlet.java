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
        System.out.println(movieId);

        String query =
                "SELECT m.`title`\n" +
                "FROM `moviedb`.`movies` m\n" +
                "WHERE m.`id` = ?";

        String result;

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, movieId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                result = rs.getString("title");
            }
        }
        return result;
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
