import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;
import java.util.logging.Logger;

class MainParser {
    private Document dom;
    private Connection conn;

    private FileWriter movieErrorWriter;
    private BufferedWriter movieErrorBufferedWriter;
    private FileWriter movieDuplicateWriter;
    private BufferedWriter movieDuplicateBufferedWriter;
    private int movieErrorCount;
    private int movieDuplicateCount;
    private PreparedStatement insertMovieStatement;
    private PreparedStatement insertMovieGenreStatement;
    private Set<String> insertedMovieIds;
    private Map<String, Integer> genreCache;

    public MainParser(Connection conn) {
        this.conn = conn;
        this.movieErrorCount = 0;
        this.movieDuplicateCount = 0;
        this.insertedMovieIds = new HashSet<>();
        this.genreCache = new HashMap<>();
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
            movieErrorWriter = new FileWriter("movie-errors.txt");
            movieErrorBufferedWriter = new BufferedWriter(movieErrorWriter);
            movieDuplicateWriter = new FileWriter("movie-duplicates.txt");
            movieDuplicateBufferedWriter = new BufferedWriter(movieDuplicateWriter);
        } catch (IOException error) {
            error.printStackTrace();
        }

        String insertMovieQuery = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
        String insertMovieGenreQuery = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?);";
        try {
            insertMovieStatement = conn.prepareStatement(insertMovieQuery);
            insertMovieGenreStatement = conn.prepareStatement(insertMovieGenreQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clean() {
        try {
            movieErrorBufferedWriter.close();
            movieErrorWriter.close();
            movieDuplicateBufferedWriter.close();
            movieDuplicateWriter.close();
        } catch (IOException error) {
            error.printStackTrace();
        }

        try {
            insertMovieStatement.close();
            insertMovieGenreStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void reportMovieError(String msg) {
        movieErrorCount++;
        try {
            movieErrorBufferedWriter.write(msg);
            movieErrorBufferedWriter.newLine();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    private void reportMovieDuplicate(String msg) {
        movieDuplicateCount++;
        try {
            movieDuplicateBufferedWriter.write(msg);
            movieDuplicateBufferedWriter.newLine();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    private boolean addMovie(String id, String title, int year, String director) {
        // Assumption: title + director is unique

        boolean inserted = !insertedMovieIds.add(id);
        if (inserted) {
            reportMovieDuplicate(title + " directed by " + director);
            return false;
        }

        // check if movie already exists
        String checkQuery = "SELECT * FROM movies WHERE id = ? OR (title = ? AND director = ?)";
        try (PreparedStatement statement = conn.prepareStatement(checkQuery)) {
            statement.setString(1, id);
            statement.setString(2, title);
            statement.setString(3, director);
            if (statement.executeQuery().next()) {
                reportMovieDuplicate(title + " directed by " + director);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            insertMovieStatement.setString(1, id);
            insertMovieStatement.setString(2, title);
            insertMovieStatement.setInt(3, year);
            insertMovieStatement.setString(4, director);
            insertMovieStatement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    /* Returns genreId */
    private int addGenre(String code) throws RuntimeException, SQLException {
        if (genreCache.containsKey(code)) {
            return genreCache.get(code);
        }

        // cat code to name mapping at http://infolab.stanford.edu/pub/movies/doc.html#CATS
        // lowercase the code
        Map<String, String> catCodeToName = Map.ofEntries(
                Map.entry("susp", "Thriller"),
                Map.entry("cnr", "Cops and Robbers"),
                // Alias
                Map.entry("cnrb", "Cops and Robbers"),
                Map.entry("dram", "Drama"),
                Map.entry("west", "Western"),
                Map.entry("myst", "Mystery"),
                // Was "science fiction", changed to "Sci-Fi" for consistency
                Map.entry("s.f.", "Sci-Fi"),
                // Alias
                Map.entry("scfi", "Sci-Fi"),
                Map.entry("scif", "Sci-Fi"),
                Map.entry("advt", "Adventure"),
                Map.entry("horr", "Horror"),
                // Was "romantic", changed to "Romance" for consistency
                Map.entry("romt", "Romance"),
                Map.entry("comd", "Comedy"),
                Map.entry("musc", "Musical"),
                Map.entry("docu", "Documentary"),
                Map.entry("porn", "Pornography"),
                Map.entry("noir", "Black"),
                // Was "biographical Picture", changed to "Biography" for consistency
                Map.entry("biop", "Biography"),
                Map.entry("tv", "TV Show"),
                Map.entry("tvs", "TV Series"),
                Map.entry("tvm", "TV Miniseries"),
                // Below are cat codes not in documentation but in XML
                Map.entry("actn", "Action"),
                Map.entry("cart", "Cartoon"),
                Map.entry("hist", "History"),
                Map.entry("epic", "Epic"),
                Map.entry("fant", "Fantasy"),
                Map.entry("surr", "Surrealist")
        );

        String genre = catCodeToName.get(code.trim().toLowerCase());
        if (genre == null)
            throw new RuntimeException("Invalid genre code " + code);

        String selectQuery = "SELECT id FROM genres WHERE name = ?";
        try (PreparedStatement statement = conn.prepareStatement(selectQuery)) {
            statement.setString(1, genre);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int foundGenreId = resultSet.getInt(1);
                    genreCache.put(genre, foundGenreId);
                    return foundGenreId;
                }
            }
        }

        String insertQuery = "INSERT INTO genres (name) VALUES (?)";
        try (PreparedStatement statement = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, genre);
            statement.executeUpdate();
            try (var resultSet = statement.getGeneratedKeys()) {
                resultSet.next();
                int foundGenreId = resultSet.getInt(1);
                genreCache.put(genre, foundGenreId);
                return foundGenreId;
            }
        }
    }

    private void addMovieGenre(int genreId, String movieId) {
        try {
            insertMovieGenreStatement.setInt(1, genreId);
            insertMovieGenreStatement.setString(2, movieId);
            insertMovieGenreStatement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void parse() {
        int movieInsertCount = 0;

        Element root = dom.getDocumentElement();

        // traverse directorfilms
        NodeList directorfilms = root.getElementsByTagName("directorfilms");
        for (int i = 0; i < directorfilms.getLength(); i++) {
            Element directorfilm = (Element) directorfilms.item(i);

            // get director name
            Element directorElement = (Element) directorfilm.getElementsByTagName("director").item(0);
            String director;
            try {
                director = Util.getTextValue(directorElement, "dirname");
            } catch (RuntimeException error) {
                reportMovieError("Invalid director name on the " + i + "th directorfilms");
                continue;
            }

            // traverse films
            Element films = (Element) directorfilm.getElementsByTagName("films").item(0);
            NodeList filmList = films.getElementsByTagName("film");
            for (int j = 0; j < filmList.getLength(); j++) {
                Element film = (Element) filmList.item(j);
                String id;
                // Schema violation: Should have <fid> tag. Solution: Ignore.
                try {
                    id = Util.getTextValue(film, "fid");
                } catch (RuntimeException error) {
                    reportMovieError("Invalid movie id directed by " + director);
                    continue;
                }

                // avoid id too large
                id = "a" + id;

                String title;
                // Schema violation: Should have only one <t> tag. Solution: Ignore.
                try {
                    title = Util.getTextValue(film, "t");
                } catch (RuntimeException error) {
                    reportMovieError("Invalid title in movie " + id);
                    continue;
                }
                int year;
                // Common sense: year should be an integer. Solution: Ignore.
                // TODO: handle cases with <released> / <re-released> tag
                try {
                    year = Util.getIntValue(film, "year");
                } catch (NumberFormatException error) {
                    reportMovieError("Invalid year in movie " + id);
                    continue;
                }

                try {
                    // Add movie to database
                    if (!addMovie(id, title, year, director))
                        continue;

                    // parse genre (cat)
                    Element cats = (Element) film.getElementsByTagName("cats").item(0);
                    // Schema violation: should have <cats> tag. Solution: Ignore.
                    if (cats == null)
                        continue;
                    List<String> catList;
                    // Schema violation: <cat> should not be empty. Solution: ignore.
                    try {
                        catList = Util.getTextValues(cats, "cat");
                    } catch (RuntimeException error) {
                        reportMovieError("Invalid cat code in movie " + id);
                        continue;
                    }

                    for (String cat : catList) {
                        try {
                            int genreId = addGenre(cat);
                            addMovieGenre(genreId, id);
                        } catch (RuntimeException error) {
                            reportMovieError("Invalid cat code " + cat + " in movie " + id);
                        }
                    }
                } catch (SQLException error) {
                    error.printStackTrace();
                }
            }
        }

        try {
            conn.setAutoCommit(false);
            movieInsertCount = insertMovieStatement.executeBatch().length;
            conn.commit();
            insertMovieGenreStatement.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Inserted " + movieInsertCount + " movies");
        System.out.println("Ignored " + movieDuplicateCount + " duplicate movies");
        System.out.println("Skipped " + movieErrorCount + " movie entries with error");
    }
}