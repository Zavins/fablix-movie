package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Utils {
    public static String mapToJson(Object data) {
        // Create a Gson object and convert the Map object to JSON
        Gson gson = new GsonBuilder().create();
        return gson.toJson(data);
    }

    public static String getMovieTitle(Connection conn, String movieId) throws SQLException {
        String query =
                "SELECT m.`title`\n" +
                        "FROM `moviedb`.`movies` m\n" +
                        "WHERE m.`id` = ?";

        System.out.println(movieId);

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, movieId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getString("title");
            }
        }
    }

    public static String parseFullTextQuery(String query) {
        String fullTextQuery = "";
        query = query.trim();
        String[] tokens = query.split(" ");
        for (String token : tokens) {
            if (!token.isEmpty()) {
                fullTextQuery += "+" + token + "* ";
            }
        }
        return fullTextQuery.trim() + " >(\"" + query + "\")";
    }
}