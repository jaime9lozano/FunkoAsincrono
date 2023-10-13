package jaime.Servicios;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final HikariDataSource dataSource;
    private boolean databaseInitTables = true; // Deberíamos inicializar las tablas? Fichero de configuración
    private String databaseUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"; // Fichero de configuración se lee en el constructor
    private String databaseInitScript = "init.sql"; // Fichero de configuración se lee en el constructor
    private Connection conn;
    private DatabaseManager() {
        loadProperties();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection()) {
            if (databaseInitTables) {
                initTables(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    private synchronized void loadProperties() {
        logger.debug("Cargando fichero de configuración de la base de datos");
        try {
            var file = ClassLoader.getSystemResource("database.properties").getFile();
            var props = new Properties();
            props.load(new FileReader(file));
            // Establecemos la url de la base de datos
            databaseUrl = props.getProperty("database.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            databaseInitTables = Boolean.parseBoolean(props.getProperty("database.initTables", "false"));
            databaseInitScript = props.getProperty("database.initScript", "init.sql");
        } catch (IOException e) {
            logger.error("Error al leer el fichero de configuración de la base de datos " + e.getMessage());
        }
    }
    private synchronized void initTables(Connection conn) {
        try {
            executeScript(conn, databaseInitScript, true);
        } catch (FileNotFoundException e) {
            logger.error("Error al leer el fichero de inicialización de la base de datos " + e.getMessage());
        }
    }
    public synchronized void executeScript(Connection conn, String scriptSqlFile, boolean logWriter) throws FileNotFoundException {
        ScriptRunner sr = new ScriptRunner(conn);
        var file = ClassLoader.getSystemResource(scriptSqlFile).getFile();
        logger.debug("Ejecutando script de SQL " + file);
        Reader reader = new BufferedReader(new FileReader(file));
        sr.setLogWriter(logWriter ? new PrintWriter(System.out) : null);
        sr.runScript(reader);
    }


    public synchronized Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
