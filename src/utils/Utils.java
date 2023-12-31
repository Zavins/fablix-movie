package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
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
        return fullTextQuery.trim() + " >\"" + query + "\"";
    }

    public static String parseFuzzyLikeQuery(String query) {
        String likeQuery = "";
        query = query.trim();
        String[] tokens = query.split(" ");
        for (String token : tokens) {
            if (token.length() >= 5) {
                likeQuery += "%" + token + "%";
            }
        }
        return likeQuery;
    }

    public static int getFuzzyDistanceThreshold(String searchQuery) {
        return searchQuery.length() / 4;
    }

    public static void writeLogFile(String name, String content) {
        try {
//            String homePath = System.getProperty("user.home");
            String homePath = "/home/ubuntu/perf_logs";
            String filePath = homePath + "/" + name;
            System.out.println(filePath);
            FileWriter fileWriter = new FileWriter(filePath, true);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}