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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;

import static utils.Utils.mapToJson;

// Declaring a WebServlet called servelets.SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "servelets.SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 4L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT * FROM `movie_list` WHERE `id` = ?";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            HashMap<String, Object> movie = new HashMap<>();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("id");
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String director = rs.getString("director");
                float rating = rs.getFloat("rating");
                String genre = rs.getString("genreName");
                String starId = rs.getString("starId");
                String starName = rs.getString("starName");

                movie.put("id", movieId);
                movie.put("title", title);
                movie.put("year", year);
                movie.put("director", director);
                movie.put("rating", rating);

                if (!movie.containsKey("genres")) {
                    movie.put("genres", new HashSet<String>());
                }
                HashSet<String> genres =
                        (HashSet<String>) movie.get("genres");
                genres.add(genre);

                if (!movie.containsKey("stars")) {
                    movie.put("stars", new HashSet<HashMap<String, Object>>());
                }
                HashSet<HashMap<String, Object>> stars =
                        (HashSet<HashMap<String, Object>>) movie.get("stars");
                HashMap<String, Object> star = new HashMap<>();
                star.put("id", starId);
                star.put("name", starName);
                stars.add(star);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(mapToJson(movie));
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
