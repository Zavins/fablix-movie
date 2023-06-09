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
 * Endpoint: POST /_dashboard/api/star/add
 * Request:
 *      name: String
 *      year: Int
 * Response:
 *      (status code):
 *          200 - OK
 *          401 - Incorrect
 *          500 - Internal Server Error
 *      (data): {status: message}
 */
@WebServlet(name = "servelets._dashboard.AddStarServlet", urlPatterns = "/_dashboard/api/star/add")
public class AddStarServlet extends HttpServlet {
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
        String name = request.getParameter("name");
        String year = request.getParameter("year");
        String result = "";
        try (Connection conn = dataSource.getConnection()) {
            String procedure = "CALL add_star(?, ?, ?, ?)";
            CallableStatement statement = conn.prepareCall(procedure);
            statement.setString(1, name);
            if (year.equals("")) {
                statement.setNull(2, Types.NULL);
            } else {
                statement.setInt(2, Integer.parseInt(year));
            }
            statement.registerOutParameter(3, Types.VARCHAR);
            statement.registerOutParameter(4, Types.VARCHAR);
            statement.execute();

            response.setStatus(200);
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("message", statement.getString(4));
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