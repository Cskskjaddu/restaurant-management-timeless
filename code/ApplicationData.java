import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ApplicationData {

    public static class MenuItem {
        public int id;
        public String name;
        public double price;

        public MenuItem(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }

    public static class ActiveOrder {
        public int orderId;
        public int tableNumber;

        public ActiveOrder(int orderId, int tableNumber) {
            this.orderId = orderId;
            this.tableNumber = tableNumber;
        }
    }

    public static List<MenuItem> menuItems = new ArrayList<>();
    public static List<ActiveOrder> activeOrders = new ArrayList<>();

    public static void loadAllData() {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            
            String menuQuery = "SELECT id, name, price FROM menu";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(menuQuery)) {
                while (rs.next()) {
                    menuItems.add(new MenuItem(rs.getInt("id"), rs.getString("name"), rs.getDouble("price")));
                }
            }
            
            String ordersQuery = "SELECT id, table_no FROM orders WHERE status = 'in progress'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(ordersQuery)) {
                while (rs.next()) {
                    activeOrders.add(new ActiveOrder(rs.getInt("id"), rs.getInt("table_no")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(conn);
        }
    }

    public static void refreshActiveOrders() {
        activeOrders.clear();
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            String ordersQuery = "SELECT id, table_no FROM orders WHERE status = 'in progress'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(ordersQuery)) {
                while (rs.next()) {
                    activeOrders.add(new ActiveOrder(rs.getInt("id"), rs.getInt("table_no")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(conn);
        }
    }
}
