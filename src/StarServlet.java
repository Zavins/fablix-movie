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

@WebServlet(name = "StarServlet", urlPatterns = "/api/star")
public class StarServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private JsonArray getMovies(Connection conn, String starId) throws SQLException {
        @SuppressWarnings("SqlAggregates") String query =
                "SELECT m.`id`, m.`title`, m.`year`, m.`director`\n" +
                "FROM `moviedb`.`stars_in_movies` sm\n" +
                "JOIN `moviedb`.`movies` m ON m.`id` = sm.`movieId`\n" +
                "WHERE sm.`starId` = ?\n" +
                "GROUP BY m.`id`\n" +
                "ORDER BY m.`year` DESC, m.`title`";

        JsonArray result = new JsonArray();

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, starId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    JsonObject rowJsonObject = new JsonObject();
                    rowJsonObject.addProperty("id", rs.getString("id"));
                    rowJsonObject.addProperty("title", rs.getString("title"));
                    rowJsonObject.addProperty("year", rs.getString("year"));
                    rowJsonObject.addProperty("director", rs.getString("director"));
                    result.add(rowJsonObject);
                }
            }
        }
        return result;
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        String id;
        JsonObject responseJsonObject = new JsonObject();

        // Get request parameters
        try {
            id = request.getParameter("id");
        } catch (Exception e) {
            response.setStatus(400);
            responseJsonObject.addProperty("message", e.getMessage());
            e.printStackTrace();

            response.setContentType("application/json");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query =
                    "SELECT s.`id`, s.`name`, s.`birthYear`\n" +
                    "FROM `moviedb`.`stars` s\n" +
                    "WHERE s.`id` = ?";

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    String rowId = rs.getString("id");
                    String rowName = rs.getString("name");
                    int rowBirthYear = rs.getInt("birthYear");

                    responseJsonObject.addProperty("id", rowId);
                    responseJsonObject.addProperty("name", rowName);
                    responseJsonObject.addProperty("birthYear", rowBirthYear);

                    responseJsonObject.add("movies", getMovies(conn, id));
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
