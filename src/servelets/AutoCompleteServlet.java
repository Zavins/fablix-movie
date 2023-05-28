package servelets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Utils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "servelets.AutoCompleteServlet", urlPatterns = "/api/autocomplete")
public class AutoCompleteServlet extends HttpServlet {
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

        String searchQuery = request.getParameter("query");

        if (searchQuery != null) {
            //If title is empty, it should not parse to fulltextquery.
            String fullTextQuery = Utils.parseFullTextQuery(searchQuery);
            String likeQuery = Utils.parseFuzzyLikeQuery(searchQuery);
            int distance = Utils.getFuzzyDistanceThreshold(searchQuery);

            try (Connection conn = dataSource.getConnection()) {
                String query =
                        "(" +
                        "SELECT m.`id`, m.`title`, m.`year`, m.`director`\n" +
                        "FROM `moviedb`.`movies` m\n" +
                        "WHERE MATCH(m.`title`) AGAINST(? IN BOOLEAN MODE)\n" +
                        "UNION\n" +
                        "SELECT m.`id`, m.`title`, m.`year`, m.`director`\n" +
                        "FROM `moviedb`.`movies` m\n" +
                        "WHERE m.`title` LIKE ?\n" +
                        "OR edth(LOWER(m.`title`), LOWER(?), ?)\n" +
                        ") " +
                        "LIMIT 10"; //Hardcode limit 10 :)
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, fullTextQuery);
                    statement.setString(2, likeQuery);
                    statement.setString(3, searchQuery);
                    statement.setInt(4, distance);
                    System.out.println(statement);
                    try (ResultSet rs = statement.executeQuery()) {
                        JsonArray result = new JsonArray();
                        while (rs.next()) {
                            String rowId = rs.getString("id");
                            String rowTitle = rs.getString("title");
                            int rowYear = rs.getInt("year");
                            String rowDirector = rs.getString("director");

                            JsonObject rowJsonObject = new JsonObject();

                            rowJsonObject.addProperty("id", rowId);
                            rowJsonObject.addProperty("title", rowTitle);
                            rowJsonObject.addProperty("year", rowYear);
                            rowJsonObject.addProperty("director", rowDirector);

                            result.add(rowJsonObject);
                        }
                        responseJsonObject.add("result", result);
                    }
                    response.setStatus(200);
                }
            } catch (SQLException e) {
                response.setStatus(500);
                responseJsonObject.addProperty("message", e.getMessage());
                e.printStackTrace();
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }
}
