import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MenuWindow extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String USER = "root";
    private static final String PASS = "";    

    private JTable menuTable;
    private DefaultTableModel tableModel;

    public MenuWindow() {
        setTitle("Restaurant Menu");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Item Name", "Category", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0);
        menuTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(menuTable);
        add(scrollPane, BorderLayout.CENTER);

        loadMenuData();
    }

    private void loadMenuData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT item_id, item_name, category, price FROM menu_items")) {

            while (rs.next()) {
                int id = rs.getInt("item_id");
                String name = rs.getString("item_name");
                String category = rs.getString("category");
                double price = rs.getDouble("price");
                tableModel.addRow(new Object[]{id, name, category, price});
            }

        } catch (SQLException se) {
            se.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuWindow().setVisible(true));
    }
}