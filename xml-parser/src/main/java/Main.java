import java.sql.Connection;
import java.sql.DriverManager;

class Main {

    public static void main(String[] args) throws Exception {
        // Get datasource
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        String mainFilename = "mains243.xml";
        String actorFilename = "actors63.xml";
        String castFilename = "casts124.xml";

        long totalStartTime = System.currentTimeMillis();

        long startTime = System.currentTimeMillis();
        System.out.println("MainParser started");
        MainParser mainParser = new MainParser(conn);
        mainParser.init(mainFilename);
        mainParser.parse();
        mainParser.clean();
        System.out.println("MainParser finished in " + (System.currentTimeMillis() - startTime) + "ms");

        startTime = System.currentTimeMillis();
        System.out.println("ActorParser started");
        ActorParser actorParser = new ActorParser(conn);
        actorParser.init(actorFilename);
        actorParser.parse();
        actorParser.clean();
        System.out.println("ActorParser finished in " + (System.currentTimeMillis() - startTime) + "ms");

        startTime = System.currentTimeMillis();
        System.out.println("CastParser started");
        CastParser castParser = new CastParser(conn);
        castParser.init(castFilename);
        castParser.parse();
        castParser.clean();
        System.out.println("CastParser finished in " + (System.currentTimeMillis() - startTime) + "ms");

        System.out.println("All parsing finished in " + (System.currentTimeMillis() - totalStartTime) + "ms");
    }
}