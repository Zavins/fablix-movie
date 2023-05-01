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

@WebServlet(name = "MovieServlet", urlPatterns = "/api/movie")
public class MovieServlet extends HttpServlet {
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
                    "SELECT m.`id`, m.`title`, m.`year`, m.`director`, r.`rating`, mglv.`genreList`, mslv.`starList` " +
                            "FROM `moviedb`.`movies` m " +
                            "JOIN `moviedb`.`ratings` r ON m.`id` = r.`movieId` " +
                            "JOIN `moviedb`.`movie_star_list_view` mslv ON m.`id` = mslv.`movieId` " +
                            "JOIN `moviedb`.`movie_genre_list_view` mglv ON m.`id` = mglv.`movieId` " +
                            "WHERE m.`id` = ?";

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    String rowId = rs.getString("id");
                    String rowTitle = rs.getString("title");
                    int rowYear = rs.getInt("year");
                    String rowDirector = rs.getString("director");
                    float rowRating = rs.getFloat("rating");
                    String rowGenreList = rs.getString("genreList");
                    String rowStarList = rs.getString("starList");

                    responseJsonObject.addProperty("id", rowId);
                    responseJsonObject.addProperty("title", rowTitle);
                    responseJsonObject.addProperty("year", rowYear);
                    responseJsonObject.addProperty("director", rowDirector);
                    responseJsonObject.addProperty("rating", rowRating);

                    JsonArray genreList = new JsonArray();
                    for (String genre : rowGenreList.split(";")) {
                        JsonObject genreObj = new JsonObject();
                        int genreObjId = Integer.parseInt(genre.split("\\|")[0]);
                        String genreObjName = genre.split("\\|")[1];
                        genreObj.addProperty("id", genreObjId);
                        genreObj.addProperty("name", genreObjName);
                        genreList.add(genreObj);
                    }
                    responseJsonObject.add("genres", genreList);

                    JsonArray starList = new JsonArray();
                    for (String star : rowStarList.split(";")) {
                        JsonObject starObj = new JsonObject();
                        String starObjId = star.split("\\|")[0];
                        String starObjName = star.split("\\|")[1];
                        starObj.addProperty("id", starObjId);
                        starObj.addProperty("name", starObjName);
                        starList.add(starObj);
                    }
                    responseJsonObject.add("stars", starList);
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
