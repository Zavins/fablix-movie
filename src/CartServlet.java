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

import static utils.Utils.getMovieTitle;

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

}
