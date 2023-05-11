package servelets;

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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "servelets.GenresServlet", urlPatterns = "/api/genres")
public class GenresServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            String query =
                    "SELECT g.`id`, g.`name` FROM `moviedb`.`genres` g ORDER BY g.`name`";

            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery(query)) {
                    JsonArray result = new JsonArray();
                    while (rs.next()) {
                        JsonObject rowJsonObject = new JsonObject();
                        String rowId = rs.getString("id");
                        String rowName = rs.getString("name");
                        rowJsonObject.addProperty("id", rowId);
                        rowJsonObject.addProperty("name", rowName);
                        result.add(rowJsonObject);
                    }
                    responseJsonObject.add("result", result);
                }
                response.setStatus(200);
            }
        } catch (Exception e) {
            response.setStatus(500);
            responseJsonObject.addProperty("message", e.getMessage());
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }
}
