import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


class DatabaseManager {
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


public class RestaurantApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GatewayWindow gateway = new GatewayWindow();
            gateway.setVisible(true);
        });
    }
}


class GatewayWindow extends JFrame {
    public GatewayWindow() {
        setTitle("Welcome to the Restaurant");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(2, 1, 10, 10));

        JButton customerButton = new JButton("I am a Customer");
        JButton staffButton = new JButton("I am Staff");

        customerButton.setFont(new Font("Arial", Font.BOLD, 16));
        staffButton.setFont(new Font("Arial", Font.BOLD, 16));

        customerButton.addActionListener(e -> {
            MenuWindow menuWindow = new MenuWindow();
            menuWindow.setVisible(true);
            dispose();
        });

        staffButton.addActionListener(e -> {
            StaffLoginWindow loginWindow = new StaffLoginWindow();
            loginWindow.setVisible(true);
            dispose();
        });

        add(customerButton);
        add(staffButton);
    }
}


class MenuWindow extends JFrame {
    private JTable menuTable;
    private DefaultTableModel tableModel;
    private JTextField tableNumberField;
    private JButton placeOrderButton;
    private JButton mainMenuButton;

    public MenuWindow() {
        setTitle("Customer Menu & Ordering");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Item Name", "Price", "Select"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? Boolean.class : super.getColumnClass(columnIndex);
            }
        };
        menuTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(menuTable);

        JPanel orderPanel = new JPanel(new FlowLayout());
        tableNumberField = new JTextField(10);
        placeOrderButton = new JButton("Place Order");
        mainMenuButton = new JButton("Main Menu");

        orderPanel.add(new JLabel("Enter Your Table Number:"));
        orderPanel.add(tableNumberField);
        orderPanel.add(placeOrderButton);
        orderPanel.add(mainMenuButton);

        add(scrollPane, BorderLayout.CENTER);
        add(orderPanel, BorderLayout.SOUTH);

        loadMenuData();
        placeOrderButton.addActionListener(e -> placeOrder());

        mainMenuButton.addActionListener(e -> {
            GatewayWindow gateway = new GatewayWindow();
            gateway.setVisible(true);
            dispose();
        });
    }

    private void loadMenuData() {
        String query = "SELECT id, name, price FROM menu";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("id"));
                    row.add(rs.getString("name"));
                    row.add(rs.getDouble("price"));
                    row.add(false);
                    tableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load menu from database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            DatabaseManager.releaseConnection(conn);
        }
    }

    private void placeOrder() {
        String tableNumStr = tableNumberField.getText();
        if (tableNumStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your table number.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Integer> selectedItems = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((Boolean) tableModel.getValueAt(i, 3)) {
                selectedItems.add((Integer) tableModel.getValueAt(i, 0));
            }
        }

        if (selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one item.", "No Items Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            String orderSQL = "INSERT INTO orders (table_no, status) VALUES (?, 'in progress')";
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSQL, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, Integer.parseInt(tableNumStr));
                orderStmt.executeUpdate();

                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long orderId = generatedKeys.getLong(1);
                        String orderItemSQL = "INSERT INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)";
                        try (PreparedStatement orderItemStmt = conn.prepareStatement(orderItemSQL)) {
                            for (Integer itemId : selectedItems) {
                                orderItemStmt.setLong(1, orderId);
                                orderItemStmt.setInt(2, itemId);
                                orderItemStmt.setInt(3, 1);
                                orderItemStmt.addBatch();
                            }
                            orderItemStmt.executeBatch();
                        }
                    }
                }
            }
            conn.commit();
            JOptionPane.showMessageDialog(this, "Order placed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Failed to place order.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            DatabaseManager.releaseConnection(conn);
        }
    }
}


class StaffLoginWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public StaffLoginWindow() {
        setTitle("Staff Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 5, 5));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> attemptLogin());
        add(new JLabel());
        add(loginButton);
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        String query = "SELECT * FROM staff WHERE name = ? AND password_hash = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        BillingWindow billingWindow = new BillingWindow();
                        billingWindow.setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error.", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            DatabaseManager.releaseConnection(conn);
        }
    }
}


class BillingWindow extends JFrame {
    private JList<String> orderList;
    private DefaultListModel<String> listModel;
    private JTextArea billArea;
    private JButton finalizeButton;
    private JButton printButton;
    private JButton mainMenuButton;
    private int selectedOrderId = -1;

    public BillingWindow() {
        setTitle("Staff Billing System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        orderList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(orderList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Active Orders (Table No - Order ID)"));

        billArea = new JTextArea();
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane billScrollPane = new JScrollPane(billArea);
        billScrollPane.setBorder(BorderFactory.createTitledBorder("Bill Details"));

        finalizeButton = new JButton("Finalize & Mark as Completed");
        finalizeButton.setEnabled(false);
        printButton = new JButton("Print Bill");
        printButton.setEnabled(false);
        mainMenuButton = new JButton("Main Menu");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(mainMenuButton);
        buttonPanel.add(printButton);
        buttonPanel.add(finalizeButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, billScrollPane);
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadActiveOrders();

        orderList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedValue = orderList.getSelectedValue();
                if (selectedValue != null) {
                    selectedOrderId = Integer.parseInt(selectedValue.split(" - ")[1]);
                    generateBill(selectedOrderId);
                    finalizeButton.setEnabled(true);
                    printButton.setEnabled(true);
                }
            }
        });

        finalizeButton.addActionListener(e -> finalizeOrder());
        printButton.addActionListener(e -> printBill());
        
        mainMenuButton.addActionListener(e -> {
            GatewayWindow gateway = new GatewayWindow();
            gateway.setVisible(true);
            dispose();
        });
    }

    private void loadActiveOrders() {
        listModel.clear();
        String query = "SELECT id, table_no FROM orders WHERE status = 'in progress'";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    listModel.addElement(rs.getInt("table_no") + " - " + rs.getInt("id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(conn);
        }
    }

    private void generateBill(int orderId) {
        billArea.setText("Generating bill for Order ID: " + orderId + "\n\n");
        String query = "SELECT m.name, m.price, oi.quantity FROM order_items oi JOIN menu m ON oi.menu_item_id = m.id WHERE oi.order_id = ?";
        double subtotal = 0.0;
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    billArea.append(String.format("%-25s %-10s %-10s\n", "Item", "Qty", "Price"));
                    billArea.append("------------------------------------------------\n");
                    while (rs.next()) {
                        String name = rs.getString("name");
                        int quantity = rs.getInt("quantity");
                        double price = rs.getDouble("price");
                        subtotal += (price * quantity);
                        billArea.append(String.format("%-25s %-10d ₹%-9.2f\n", name, quantity, price));
                    }
                }
            }
            double tax = subtotal * 0.18;
            double total = subtotal + tax;
            billArea.append("\n------------------------------------------------\n");
            billArea.append(String.format("%-36s ₹%-9.2f\n", "Subtotal:", subtotal));
            billArea.append(String.format("%-36s ₹%-9.2f\n", "Tax (18%):", tax));
            billArea.append(String.format("%-36s ₹%-9.2f\n", "Total:", total));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(conn);
        }
    }
    
    private void printBill() {
        try {
            boolean didPrint = billArea.print();
            if (didPrint) {
                JOptionPane.showMessageDialog(this, "Printing job has been sent to the printer.", "Printing", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Printing has been cancelled.", "Printing", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.awt.print.PrinterException e) {
            JOptionPane.showMessageDialog(this, "An error occurred while trying to print.", "Print Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void finalizeOrder() {
        if (selectedOrderId == -1) return;
        String query = "UPDATE orders SET status = 'completed' WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, selectedOrderId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Order " + selectedOrderId + " marked as completed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadActiveOrders();
                billArea.setText("");
                finalizeButton.setEnabled(false);
                printButton.setEnabled(false);
                selectedOrderId = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(conn);
        }
    }
}
