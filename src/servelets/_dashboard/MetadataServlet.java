package servelets._dashboard;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static utils.Utils.mapToJson;

@WebServlet(name = "servelets._dashboard.MetadataServelet", urlPatterns = "/_dashboard/api/metadata")
public class MetadataServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_ro");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    //{attribute: [{name: name, type: type}, {name: name, type: type}...]}
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String result = "";
        Cart cart = (Cart) request.getSession().getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            request.getSession().setAttribute("cart", cart);
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT `c`.`TABLE_NAME`, `c`.`COLUMN_NAME`, `c`.`DATA_TYPE` , `c`.`CHARACTER_MAXIMUM_LENGTH` " +
                    "FROM `information_schema`.`columns` as `c`, `information_schema`.`tables` as `t` " +
                    "WHERE `t`.`TABLE_SCHEMA` = 'moviedb' AND `c`.`TABLE_NAME` = `t`.`TABLE_NAME`AND `t`.`TABLE_TYPE` = 'BASE TABLE';";

            Map<String, ArrayList<Object>> metadata = new TreeMap<String, ArrayList<Object>>();

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                ResultSet rs = statement.executeQuery(query);
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String attribute = rs.getString("COLUMN_NAME");
                    String type = rs.getString("DATA_TYPE");
                    int maxLength = rs.getInt("CHARACTER_MAXIMUM_LENGTH");
                    type = type.equals("varchar") ? String.format("varchar(%d)", maxLength) : type;


                    ArrayList<Object> attributeList = metadata.getOrDefault(tableName, null);
                    if (attributeList == null) {
                        attributeList = new ArrayList<Object>();
                        metadata.put(tableName, attributeList);
                    }
                    Map<String, String> entry = new TreeMap<String, String>();
                    entry.put("attribute", attribute);
                    entry.put("type", type.toUpperCase());
                    attributeList.add(entry);
                }
                rs.close();
                statement.close();
                result = mapToJson(metadata);
            }

            response.setStatus(200);
        } catch (Exception e) {
            response.setStatus(400);
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("message", e.getMessage());
            result = mapToJson(responseJsonObject);
            e.printStackTrace();
        } finally {
            response.setContentType("application/json");
            response.getWriter().write(result);
        }
    }
}
