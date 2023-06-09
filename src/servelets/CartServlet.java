package servelets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Cart;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

import static utils.Utils.getMovieTitle;

@WebServlet(name = "servelets.CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_ro");
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
                itemJsonObject.addProperty("title", getMovieTitle(conn, item.getKey()));
                itemJsonObject.addProperty("quantity", item.getValue());
                itemJsonObject.addProperty("price", 10);
                itemJsonObject.addProperty("total", 10 * item.getValue());
                total += item.getValue() * 10;
                result.add(itemJsonObject);
            }
            responseJsonObject.addProperty("total", total);
            responseJsonObject.add("result", result);
            responseJsonObject.addProperty("count", cart.count());
            response.setStatus(200);
        } catch (Exception e) {
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
        String quantityStr = request.getParameter("quantity");
        int change = 0;
        int quantity = 0;
        if (changeStr != null) change = Integer.parseInt(changeStr);
        if (quantityStr != null) quantity = Integer.parseInt(quantityStr);
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
            if (quantity > 0) {
                cart.setQuantity(movieId, quantity);
                response.setStatus(200);
            } else {
                response.setStatus(400);
            }
        }
        responseJsonObject.addProperty("count", cart.count());
        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        JsonObject responseJsonObject = new JsonObject();

        String movieId = request.getParameter("movieId");

        Cart cart = (Cart) request.getSession().getAttribute("cart");

        if (movieId != null) {
            cart.delete(movieId);
        } else {
            cart.deleteAll();
        }


        responseJsonObject.addProperty("count", cart.count());
        response.setStatus(200);

        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }
}
