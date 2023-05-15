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

    public CastParser(Connection conn) {
        this.conn = conn;
        this.movieNotFoundCount = 0;
        this.starNotFoundCount = 0;
        this.starInMovieCount = 0;
        this.starMovieErrorCount = 0;

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
        String foundMovieid = null;
        String starId = null;

        // Find star id
        String checkStarQuery = "SELECT * FROM stars WHERE name = ?";
        try (PreparedStatement statement = conn.prepareStatement(checkStarQuery)) {
            statement.setString(1, starName);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                starId = rs.getString("id");
            }
        }

        if (starId == null) {
            reportStarNotFound(starName + "(fid " + movieId + ")");
            return;
        }

        // Find movie id

        // Check if movie exists by movieId
        String checkMovieIdQuery = "SELECT id FROM movies WHERE id = ?";
        try (PreparedStatement statement = conn.prepareStatement(checkMovieIdQuery)) {
            statement.setString(1, movieId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                foundMovieid = rs.getString("id");
            }
        }

        // Assumption: title + director is unique

        if (foundMovieid == null) {
            // Check if movie exists by title and director name
            String checkMovieTitleDirectorQuery = "SELECT id FROM movies WHERE title = ? AND director = ?";
            try (PreparedStatement statement = conn.prepareStatement(checkMovieTitleDirectorQuery)) {
                statement.setString(1, movieTitle);
                statement.setString(2, directorName);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    foundMovieid = rs.getString("id");
                }
            }
        }

        if (foundMovieid == null) {
            reportMovieNotFound(movieId + ", " + directorName + ", " + movieTitle);
            return;
        }

        // Insert into stars_in_movies
        String insertQuery = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(insertQuery)) {
            statement.setString(1, starId);
            statement.setString(2, foundMovieid);
            statement.executeUpdate();
            starInMovieCount++;
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
        System.out.println("Inserted " + starInMovieCount + " star in movie relations");
        System.out.println(starNotFoundCount + " stars not found");
        System.out.println(movieNotFoundCount + " movies not found");
    }
}
