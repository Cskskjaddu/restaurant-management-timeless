import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JOptionPane;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://restaurant-management-timeless-restaurant-management-timeless.d.aivencloud.com:27822/defaultdb?ssl-mode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_iDPP07E54ht6kyo8m40";
    private static final int POOL_SIZE = 5;

    private static BlockingQueue<Connection> connectionPool;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connectionPool = new LinkedBlockingQueue<>(POOL_SIZE);
            for (int i = 0; i < POOL_SIZE; i++) {
                connectionPool.add(DriverManager.getConnection(DB_URL, USER, PASSWORD));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to initialize database connection pool.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            return connectionPool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Failed to get connection from pool", e);
        }
    }

    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                connectionPool.put(connection);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}