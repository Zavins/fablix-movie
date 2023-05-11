package servelets;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.User;
import org.jasypt.util.password.StrongPasswordEncryptor;
import utils.RecaptchaVerifyUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/* Login Servlet
 * Endpoint: POST /api/login
 * Request:
 *      username: String
 *      password: String
 * Response:
 *      (status code):
 *          200 - OK
 *          401 - Credential Incorrect
 *          500 - Internal Server Error
 *      (session):
 *          name: String
 */
@WebServlet(name = "servelets.LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Check if email (username) exists
    private boolean checkUsername(Connection conn, String username) throws SQLException {
        String query = "SELECT EXISTS(SELECT * FROM `customers` WHERE `email` = ?)";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                // Get first row and first column
                rs.next();
                return rs.getBoolean(1);
            }
        }
    }

    // Check if password is correct given the username (email)
    private Integer getUserId(Connection conn, String username, String password) throws SQLException {
        // Check if email (username) exists
        String query = "SELECT `id`, `password` FROM `customers` WHERE `email` = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    // get the encrypted password from the database
                    String encryptedPassword = rs.getString("password");
                    int userId = rs.getInt(1);
                    // use the same encryptor to compare the user input password with encrypted password stored in DB
                    boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                    return success ? userId : null;
                }
                return null;
            }
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            String recaptchaResult = RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            System.out.println(recaptchaResult);
            if (recaptchaResult.equals("success")) {
                if (checkUsername(conn, username)) {
                    Integer userId = getUserId(conn, username, password);
                    if (userId != null) {
                        response.setStatus(200);
                        request.getSession().setAttribute("user", new User(userId, username));
                    } else {
                        response.setStatus(401);
                        responseJsonObject.addProperty("message", "incorrect password");
                    }
                } else {
                    response.setStatus(401);
                    responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
                }
            } else {
                response.setStatus(401);
                responseJsonObject.addProperty("message", "recaptcha verification failed");
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