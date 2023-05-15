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

    public ActorParser(Connection conn) {
        this.conn = conn;
        this.starDuplicateCount = 0;
        this.starInsertCount = 0;
        this.starErrorCount = 0;
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
            }
        }

        // Insert
        String id = Util.generateId(name + birthYear, 10);
        String insertQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(insertQuery)) {
            statement.setString(1, id);
            statement.setString(2, name);
            if (birthYear == null)
                statement.setNull(3, java.sql.Types.INTEGER);
            else
                statement.setInt(3, birthYear);
            statement.executeUpdate();
            starInsertCount++;
        } catch (SQLIntegrityConstraintViolationException error) {
            reportStarDuplicate(name + " " + birthYear);
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

        System.out.println("Inserted " + starInsertCount + " stars");
        System.out.println("Ignored " + starDuplicateCount + " duplicate stars");
        System.out.println("Skipped " + starErrorCount + " star entries with error");
    }
}
