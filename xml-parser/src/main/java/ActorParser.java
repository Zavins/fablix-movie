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
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashSet;
import java.util.Set;

public class ActorParser {
    private int starDuplicateCount;
    private int starInsertCount;
    private int starErrorCount;
    private Document dom;
    private Connection conn;

    private FileWriter starDuplicateWriter;
    private BufferedWriter starDuplicateBufferedWriter;
    private FileWriter starErrorWriter;
    private BufferedWriter starErrorBufferedWriter;
    private PreparedStatement insertStarStatement;
    private Set<String> insertedStarIds;

    public ActorParser(Connection conn) {
        this.conn = conn;
        this.starDuplicateCount = 0;
        this.starInsertCount = 0;
        this.starErrorCount = 0;
        insertedStarIds = new HashSet<>();
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
            starDuplicateWriter = new FileWriter("star-duplicates.txt");
            starDuplicateBufferedWriter = new BufferedWriter(starDuplicateWriter);
            starErrorWriter = new FileWriter("star-errors.txt");
            starErrorBufferedWriter = new BufferedWriter(starErrorWriter);
        } catch (IOException error) {
            error.printStackTrace();
        }

        String insertQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
        try {
            insertStarStatement = conn.prepareStatement(insertQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clean() {
        try {
            starDuplicateBufferedWriter.close();
            starDuplicateWriter.close();
            starErrorBufferedWriter.close();
            starErrorWriter.close();
        } catch (IOException error) {
            error.printStackTrace();
        }

        try {
            insertStarStatement.close();
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    private void reportStarDuplicate(String msg) {
        starDuplicateCount++;
        try {
            starDuplicateBufferedWriter.write(msg + "\n");
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    private void reportStarError(String msg) {
        starErrorCount++;
        try {
            starErrorBufferedWriter.write(msg + "\n");
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    private void addStar(String name, Integer birthYear) throws SQLException {
        // Check if exists
        String checkQuery = "SELECT * FROM stars WHERE name = ? AND (? OR birthYear = ?)";
        try (PreparedStatement statement = conn.prepareStatement(checkQuery)) {
            statement.setString(1, name);
            statement.setBoolean(2, birthYear == null);
            statement.setInt(3, birthYear == null ? 0 : birthYear);
            if (statement.executeQuery().next()) {
                reportStarDuplicate(name + " " + birthYear);
                return;
            }
        }

        // Insert
        String id = Util.generateId(name + birthYear, 8);
        // avoid id too large
        id = "a" + id;
        boolean inserted = !insertedStarIds.add(id);
        if (inserted) {
            reportStarDuplicate(name + " " + birthYear);
            return;
        }
        try {
            insertStarStatement.setString(1, id);
            insertStarStatement.setString(2, name);
            if (birthYear == null)
                insertStarStatement.setNull(3, java.sql.Types.INTEGER);
            else
                insertStarStatement.setInt(3, birthYear);
            insertStarStatement.addBatch();
        } catch (SQLIntegrityConstraintViolationException error) {
            throw new RuntimeException(error);
        }
    }

    public void parse() {
        Element rootElement = dom.getDocumentElement(); // <actors>

        // Traverse actors
        NodeList actorNodeList = rootElement.getElementsByTagName("actor");
        for (int i = 0; i < actorNodeList.getLength(); i++) {
            Element actorElement = (Element) actorNodeList.item(i);

            String name;
            try {
                name = Util.getTextValue(actorElement, "stagename");
            } catch (RuntimeException error) {
                reportStarError(i + "th actor name empty");
                continue;
            }

            Integer birthYear;
            try {
                birthYear = Util.getIntValue(actorElement, "dob");
            } catch (RuntimeException error) {
                birthYear = null;
            }

            try {
                addStar(name, birthYear);
            } catch (SQLException error) {
                error.printStackTrace();
            }
        }

        try {
            conn.setAutoCommit(false);
            starInsertCount = insertStarStatement.executeBatch().length;
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }

        System.out.println("Inserted " + starInsertCount + " stars");
        System.out.println("Ignored " + starDuplicateCount + " duplicate stars");
        System.out.println("Skipped " + starErrorCount + " star entries with error");
    }
}
