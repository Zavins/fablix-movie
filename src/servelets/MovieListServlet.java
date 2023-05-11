package servelets;

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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static utils.Utils.mapToJson;


// Declaring a WebServlet called servelets.StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "servelets.MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;

    // Create a dataSource which registered in web.
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

        response.setContentType("application/json"); // Response mime type

        String countStr = request.getParameter("count");
        int count;
        try {
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            count = 20;
        }
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "SELECT `ml`.`id`, `title`, `year`, `director`, " +
                    "`genreName`, `starName`, `starId`, `rating`" +
                    "FROM `movie_list` as `ml` " +
                    "JOIN (" +
                    "SELECT `id` " +
                    "FROM `movie_rating` " +
                    String.format("LIMIT %d ", count) +
                    ") as `mr` ON `ml`.`id` = `mr`.`id`";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            HashMap<String, Object> movie = null;
            ArrayList<Object> movieList = new ArrayList<>();

            String prevId = "";

            // Iterate through each row of rs
            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String director = rs.getString("director");
                String genre = rs.getString("genreName");
                String starName = rs.getString("starName");
                String starId = rs.getString("starId");
                float rating = rs.getFloat("rating");

                if (!id.equals(prevId) || movie == null) {
                    if (movie != null) movieList.add(movie);

                    movie = new HashMap<>();
                    movie.put("id", id);
                    movie.put("title", title);
                    movie.put("year", year);
                    movie.put("director", director);
                    movie.put("rating", rating);
                    movie.put("genres", new HashSet<String>());
                    movie.put("stars", new HashSet<HashMap<String, String>>());
                }
                HashSet<String> genres = (HashSet<String>) movie.get("genres");
                HashSet<HashMap<String, String>> stars = (HashSet<HashMap<String, String>>) movie.get("stars");
                HashMap<String, String> star = new HashMap<>();
                star.put("id", starId);
                star.put("name", starName);
                if (genres.size() < 3) genres.add(genre);
                if (stars.size() < 3) stars.add(star);
                prevId = id;
            }
            rs.close();
            statement.close();

            movieList.add(movie);
            // Write JSON string to output
            out.write(mapToJson(movieList));
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
