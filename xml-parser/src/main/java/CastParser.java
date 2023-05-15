import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CastParser {
    private int starInMovieCount;
    private int starNotFoundCount;
    private int movieNotFoundCount;
    private int starMovieErrorCount;
    private Document dom;
    private Connection conn;

    private FileWriter starNotFoundWriter;
    private BufferedWriter starNotFoundBufferedWriter;
    private FileWriter movieNotFoundWriter;
    private BufferedWriter movieNotFoundBufferedWriter;
    private FileWriter starMovieErrorWriter;
    private BufferedWriter starMovieErrorBufferedWriter;
    private PreparedStatement insertStarInMovieStatement;
    // key: movie id, value: actual movie id
    private Map<String, String> movieIdCache;
    // key: star name, value: star id, if not exists "" (empty string)
    private Map<String, String> starIdCache;

    public CastParser(Connection conn) {
        this.conn = conn;
        this.movieNotFoundCount = 0;
        this.starNotFoundCount = 0;
        this.starInMovieCount = 0;
        this.starMovieErrorCount = 0;
        this.movieIdCache = new HashMap<>();
        this.starIdCache = new HashMap<>();
    }

    public void init(String filename) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            // encoding = "ISO-8859-1"
            InputSource inputSource = new InputSource(filename);
            inputSource.setEncoding("ISO-8859-1");
            dom = documentBuilder.parse(inputSource);
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }

        // create buffer writters
        try {
            starNotFoundWriter = new FileWriter("star-not-found.txt");
            starNotFoundBufferedWriter = new BufferedWriter(starNotFoundWriter);
            movieNotFoundWriter = new FileWriter("movie-not-found.txt");
            movieNotFoundBufferedWriter = new BufferedWriter(movieNotFoundWriter);
            starMovieErrorWriter = new FileWriter("star-movie-errors.txt");
            starMovieErrorBufferedWriter = new BufferedWriter(starMovieErrorWriter);
        } catch (IOException error) {
            error.printStackTrace();
        }

        String insertQuery = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
        try {
            insertStarInMovieStatement = conn.prepareStatement(insertQuery);
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    public void clean() {
        try {
            starNotFoundBufferedWriter.close();
            starNotFoundWriter.close();
            movieNotFoundBufferedWriter.close();
            movieNotFoundWriter.close();
            starMovieErrorBufferedWriter.close();
            starMovieErrorWriter.close();
        } catch (IOException error) {
            error.printStackTrace();
        }

        try {
            insertStarInMovieStatement.close();
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    private void reportMovieNotFound(String msg) {
        movieNotFoundCount++;
        try {
            movieNotFoundBufferedWriter.write(msg + "\n");
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    private void reportStarNotFound(String msg) {
        starNotFoundCount++;
        try {
            starNotFoundBufferedWriter.write(msg + "\n");
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    private void reportStarMovieError(String msg) {
        starMovieErrorCount++;
        try {
            starMovieErrorBufferedWriter.write(msg + "\n");
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    private void addMovieStar(String movieId, String starName, String directorName, String movieTitle) throws SQLException {
        // Find star id
        String starId = starIdCache.get(starName);

        if (starId == null) {
            String checkStarQuery = "SELECT * FROM stars WHERE name = ?";
            try (PreparedStatement statement = conn.prepareStatement(checkStarQuery)) {
                statement.setString(1, starName);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    starId = rs.getString("id");
                    starIdCache.put(starName, starId);
                }
            }
            if (starId == null) {
                starIdCache.put(starName, "");
                reportStarNotFound(starName + "(fid " + movieId + ")");
                return;
            }
        } else if (starId.equals("")) {
            reportStarNotFound(starName + "(fid " + movieId + ")");
            return;
        }



        // Find movie id
        String foundMovieid = movieIdCache.get(movieId);

        // Check if movie exists by movieId
        if (foundMovieid == null) {
            String checkMovieIdQuery = "SELECT id FROM movies WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(checkMovieIdQuery)) {
                statement.setString(1, movieId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    foundMovieid = rs.getString("id");
                    movieIdCache.put(movieId, foundMovieid);
                }
            }
        }

        // Assumption: title + director is unique

        // Check if movie exists by title and director name
        if (foundMovieid == null) {
            String checkMovieTitleDirectorQuery = "SELECT id FROM movies WHERE title = ? AND director = ?";
            try (PreparedStatement statement = conn.prepareStatement(checkMovieTitleDirectorQuery)) {
                statement.setString(1, movieTitle);
                statement.setString(2, directorName);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    foundMovieid = rs.getString("id");
                    movieIdCache.put(movieId, foundMovieid);
                }
            }
        }

        if (foundMovieid == null) {
            reportMovieNotFound(movieId + ", " + directorName + ", " + movieTitle);
            return;
        }

        // Insert into stars_in_movies
        try {
            insertStarInMovieStatement.setString(1, starId);
            insertStarInMovieStatement.setString(2, foundMovieid);
            insertStarInMovieStatement.addBatch();
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    public void parse() {
        Element rootElement = dom.getDocumentElement(); // <casts>

        // Traverse directors
        NodeList dirfilmsList = rootElement.getElementsByTagName("dirfilms");
        for (int i = 0; i < dirfilmsList.getLength(); i++) {
            Element dirfilmsElement = (Element) dirfilmsList.item(i);

            // If movie entry exists before main xml was parsed
            String directorName;
            try {
                directorName = Util.getTextValue(dirfilmsElement, "is");
            } catch (RuntimeException error) {
                reportStarMovieError("Invalid <is> director name entry on " + i + "th dirfilms");
                continue;
            }

            // Traverse films by this director
            NodeList filmcList = dirfilmsElement.getElementsByTagName("filmc");
            for (int j = 0; j < filmcList.getLength(); j++) {
                Element filmcElement = (Element) filmcList.item(j);

                // Traverse stars in this movie
                NodeList mList = filmcElement.getElementsByTagName("m");
                for (int k = 0; k < mList.getLength(); k++) {
                    Element mElement = (Element) mList.item(k);

                    String fid = Util.getTextValue(mElement, "f");
                    String stagename;
                    try {
                        stagename = Util.getTextValue(mElement, "a");
                    } catch (NullPointerException error) {
                        reportStarMovieError("Invalid <a> stage name entry on movie " + fid);
                        continue;
                    }
                    // If movie entry exists before main xml was parsed
                    String title = Util.getTextValue(mElement, "t");

                    try {
                        addMovieStar(fid, stagename, directorName, title);
                    } catch (SQLException error) {
                        error.printStackTrace();
                    }
                }
            }
        }

        try {
            conn.setAutoCommit(false);
            starInMovieCount = insertStarInMovieStatement.executeBatch().length;
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }

        System.out.println("Inserted " + starInMovieCount + " star in movie relations");
        System.out.println(starNotFoundCount + " stars not found");
        System.out.println(movieNotFoundCount + " movies not found");
    }
}
