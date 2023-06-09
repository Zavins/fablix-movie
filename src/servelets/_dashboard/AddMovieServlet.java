package servelets._dashboard;

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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

/* Login Servlet
 * Endpoint: POST /_dashboard/api/movie/add
 * Request:
 *      title: String
 *      director: String
 *      year: Int
 *      starName: String
 *      birthYear? Int
 *      genre: String
 * Response:
 *      (status code):
 *          200 - OK
 *          401 - Incorrect
 *          500 - Internal Server Error
 *      (data): {status: message}
 */
@WebServlet(name = "servelets._dashboard.AddMovieServlet", urlPatterns = "/_dashboard/api/movie/add")
public class AddMovieServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_rw");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        String director = request.getParameter("director");
        String year = request.getParameter("year");
        String starName = request.getParameter("starName");
        String birthYear = request.getParameter("birthYear");
        String genre = request.getParameter("genre");
        String result = "";
        try (Connection conn = dataSource.getConnection()) {
            String procedure = "CALL add_movie(?, ?, ?, ?, ?, ?, ?)";
            CallableStatement statement = conn.prepareCall(procedure);
            statement.setString(1, title);
            statement.setString(2, director);
            statement.setInt(3, Integer.parseInt(year));
            statement.setString(4, starName);
            if (birthYear.equals("")) {
                statement.setNull(5, java.sql.Types.NULL);
            } else {
                statement.setInt(5, Integer.parseInt(birthYear));
            }
            statement.setString(6, genre);
            statement.registerOutParameter(7, Types.VARCHAR);
            statement.execute();

            response.setStatus(200);
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("message", statement.getString(7));
            result = responseJsonObject.toString();
        } catch (Exception e) {
            response.setStatus(500);
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("message", e.getMessage());
            e.printStackTrace();
            result = responseJsonObject.toString();
        } finally {
            response.setContentType("application/json");
            response.getWriter().write(result);
        }
    }
}