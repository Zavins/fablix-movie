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
import java.net.URLDecoder;
import java.sql.*;
import java.util.Map;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
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

        final Map<String, String> ORDER_BY_MAP = Map.of(
                "tara", "title ASC, rating ASC",
                "tard", "title ASC, rating DESC",
                "tdra", "title DESC, rating ASC",
                "tdrd", "title DESC, rating DESC",
                "rata", "rating ASC, title ASC",
                "ratd", "rating ASC, title DESC",
                "rdta", "rating DESC, title ASC",
                "rdtd", "rating DESC, title DESC",
                "default", "rating DESC"
        );

        final Map<String, String> REVERSE_ORDER_BY_MAP = Map.of(
                "title ASC, rating ASC", "tara",
                "title ASC, rating DESC", "tard",
                "title DESC, rating ASC", "tdra",
                "title DESC, rating DESC", "tdrd",
                "rating ASC, title ASC", "rata",
                "rating ASC, title DESC", "ratd",
                "rating DESC, title ASC", "rdta",
                "rating DESC, title DESC", "rdtd",
                "rating DESC", "default"
        );

        int count, page, genreCount = 3, starCount = 3;
        Integer year, genreId;
        String title, director, starName, sortBy;
        JsonObject responseJsonObject = new JsonObject();

        // Get request parameters
        try {
            String usePrevious = request.getParameter("usePrevious");
            if (usePrevious.equals("1")) {
                Search search = (Search) request.getSession().getAttribute("search");
                count = search.getCount();
                page = search.getPage();
                year = search.getYear();
                title = search.getTitle();
                starName = search.getStarName();
                director = search.getDirector();
                genreId = search.getGenreId();
                sortBy = search.getSortBy();
                responseJsonObject.addProperty("count", count);
                responseJsonObject.addProperty("page", page);
                responseJsonObject.addProperty("year", year);
                responseJsonObject.addProperty("title", title);
                responseJsonObject.addProperty("starName", starName);
                responseJsonObject.addProperty("director", director);
                responseJsonObject.addProperty("genreId", genreId);
                responseJsonObject.addProperty("sortBy", REVERSE_ORDER_BY_MAP.get((sortBy)));
            } else {
                String countStr = request.getParameter("count");
                count = Integer.parseInt(countStr);
                String yearStr = request.getParameter("year");
                year = yearStr.isEmpty() ? null : Integer.parseInt(yearStr);
                String titleOrNull = request.getParameter("title");
                title = titleOrNull.isEmpty() ? null : URLDecoder.decode(titleOrNull, "UTF-8");
                String directorOrNull = request.getParameter("director");
                director = directorOrNull.isEmpty() ? null : URLDecoder.decode(directorOrNull, "UTF-8");
                String starOrNull = request.getParameter("starName");
                starName = starOrNull.isEmpty() ? null : URLDecoder.decode(starOrNull, "UTF-8");
                String genreStr = request.getParameter("genre");
                genreId = genreStr == null ? null : Integer.parseInt(genreStr);
                String pageStr = request.getParameter("page");
                page = pageStr == null ? 1 : Integer.parseInt(pageStr);

                String sortByStr = request.getParameter("sortBy");
                if (sortByStr == null) {
                    sortByStr = "default";
                }
                sortBy = ORDER_BY_MAP.get(sortByStr);
                if (sortBy == null) {
                    throw new RuntimeException("sortBy value not supported");
                }

                request.getSession().setAttribute("search", new Search(
                        count, page, year, title, starName, director, genreId, sortBy
                ));
            }
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
                            "WHERE " +
                            "(? OR m.`id` IN (SELECT gm.`movieId` FROM `moviedb`.`genres_in_movies` gm WHERE gm.`genreId` = ?))" +
                            "AND (? OR m.`id` IN (SELECT sm.`movieId` FROM `moviedb`.`stars_in_movies` sm JOIN `moviedb`.`stars` s ON s.`id` = sm.`starId` WHERE s.`name` = ?)) " +
                            "AND (? OR m.`title` LIKE ?) " +
                            "AND (? OR m.`director` LIKE ?) " +
                            "AND (? OR m.`year` = ?) " +
                            "ORDER BY " + sortBy + " " +
                            "LIMIT ?, ?";

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setBoolean(1, genreId == null);
                statement.setInt(2, genreId == null ? 0 : genreId);
                statement.setBoolean(3, starName == null);
                statement.setString(4, starName == null ? "" : starName);
                statement.setBoolean(5, title == null);
                statement.setString(6, title == null ? "" : title);
                statement.setBoolean(7, director == null);
                statement.setString(8, director == null ? "" : title);
                statement.setBoolean(9, year == null);
                statement.setInt(10, year == null ? 0 : year);
                statement.setInt(11, (page - 1) * count); // offset
                statement.setInt(12, count); // limit
                try (ResultSet rs = statement.executeQuery()) {
                    JsonArray result = new JsonArray();
                    while (rs.next()) {
                        String rowId = rs.getString("id");
                        String rowTitle = rs.getString("title");
                        int rowYear = rs.getInt("year");
                        String rowDirector = rs.getString("director");
                        float rowRating = rs.getFloat("rating");
                        String rowGenreList = rs.getString("genreList");
                        String rowStarList = rs.getString("starList");

                        JsonObject rowJsonObject = new JsonObject();

                        rowJsonObject.addProperty("id", rowId);
                        rowJsonObject.addProperty("title", rowTitle);
                        rowJsonObject.addProperty("year", rowYear);
                        rowJsonObject.addProperty("director", rowDirector);
                        rowJsonObject.addProperty("rating", rowRating);

                        JsonArray genreList = new JsonArray();
                        for (String genre : rowGenreList.split(";")) {
                            JsonObject genreObj = new JsonObject();
                            int genreObjId = Integer.parseInt(genre.split("\\|")[0]);
                            String genreObjName = genre.split("\\|")[1];
                            genreObj.addProperty("id", genreObjId);
                            genreObj.addProperty("name", genreObjName);
                            genreList.add(genreObj);
                        }
                        // TODO: Limit to three
                        rowJsonObject.add("genres", genreList);

                        // TODO: Limit to three
                        JsonArray starList = new JsonArray();
                        for (String star : rowStarList.split(";")) {
                            JsonObject starObj = new JsonObject();
                            String starObjId = star.split("\\|")[0];
                            String starObjName = star.split("\\|")[1];
                            starObj.addProperty("id", starObjId);
                            starObj.addProperty("name", starObjName);
                            starList.add(starObj);
                        }
                        rowJsonObject.add("stars", starList);

                        result.add(rowJsonObject);
                    }
                    responseJsonObject.add("result", result);
                }
                response.setStatus(200);
            }

            // Add numPages

            String countQuery = "SELECT COUNT(*) " +
                    "FROM `moviedb`.`movies` m " +
                    "JOIN `moviedb`.`ratings` r ON m.`id` = r.`movieId` " +
                    "WHERE " +
                    "(? OR m.`id` IN (SELECT gm.`movieId` FROM `moviedb`.`genres_in_movies` gm WHERE gm.`genreId` = ?))" +
                    "AND (? OR m.`id` IN (SELECT sm.`movieId` FROM `moviedb`.`stars_in_movies` sm JOIN `moviedb`.`stars` s ON s.`id` = sm.`starId` WHERE s.`name` = ?)) " +
                    "AND (? OR m.`title` LIKE ?) " +
                    "AND (? OR m.`director` LIKE ?) " +
                    "AND (? OR m.`year` = ?) ";

            try (PreparedStatement statement = conn.prepareStatement(countQuery)) {
                statement.setBoolean(1, genreId == null);
                statement.setInt(2, genreId == null ? 0 : genreId);
                statement.setBoolean(3, starName == null);
                statement.setString(4, starName == null ? "" : starName);
                statement.setBoolean(5, title == null);
                statement.setString(6, title == null ? "" : title);
                statement.setBoolean(7, director == null);
                statement.setString(8, director == null ? "" : title);
                statement.setBoolean(9, year == null);
                statement.setInt(10, year == null ? 0 : year);
                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    responseJsonObject.addProperty("numPages", rs.getInt(1) / count + 1);
                }
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
