import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
import javax.swing.JList;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class BillingWindow extends JFrame {
    private JList<String> orderList;
    private DefaultListModel<String> listModel;
    private JTextArea billArea;
    private JButton finalizeButton;
    private JButton printButton;
    private JButton mainMenuButton;
    private int selectedOrderId = -1;

    public BillingWindow() {
        setTitle("Timeless Restaurant - Staff Billing System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBackground(Color.decode("#121212"));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel restaurantNameLabel = new JLabel("Timeless Restaurant", SwingConstants.CENTER);
        restaurantNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        restaurantNameLabel.setForeground(Color.decode("#ffffff"));
        mainPanel.add(restaurantNameLabel, BorderLayout.NORTH);
        
        listModel = new DefaultListModel<>();
        orderList = new JList<>(listModel);
        orderList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orderList.setSelectionBackground(Color.decode("#e0e0e0"));

        JScrollPane listScrollPane = new JScrollPane(orderList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), "Active Orders (Table No - Order ID)", 0, 0, new Font("Segoe UI", Font.BOLD, 18), Color.decode("#333333")));
        listScrollPane.setBackground(Color.WHITE);

        billArea = new JTextArea();
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        billArea.setBackground(Color.decode("#ffffff"));
        JScrollPane billScrollPane = new JScrollPane(billArea);
        billScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), "Bill Details", 0, 0, new Font("Segoe UI", Font.BOLD, 18), Color.decode("#333333")));

        finalizeButton = new JButton("Finalize & Mark as Completed");
        finalizeButton.setBackground(Color.decode("#28a745"));
        finalizeButton.setForeground(Color.WHITE);
        finalizeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        finalizeButton.setFocusPainted(false);
        finalizeButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        finalizeButton.setEnabled(false);

        printButton = new JButton("Print Bill");
        printButton.setBackground(Color.decode("#6c757d"));
        printButton.setForeground(Color.WHITE);
        printButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        printButton.setFocusPainted(false);
        printButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        printButton.setEnabled(false);
        
        mainMenuButton = new JButton("Main Menu");
        mainMenuButton.setBackground(Color.decode("#007bff"));
        mainMenuButton.setForeground(Color.WHITE);
        mainMenuButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainMenuButton.setFocusPainted(false);
        mainMenuButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.decode("#121212"));
        buttonPanel.add(mainMenuButton);
        buttonPanel.add(printButton);
        buttonPanel.add(finalizeButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, billScrollPane);
        splitPane.setDividerLocation(250);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        ApplicationData.refreshActiveOrders();
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
        for (ApplicationData.ActiveOrder order : ApplicationData.activeOrders) {
            listModel.addElement(order.tableNumber + " - " + order.orderId);
        }
    }

    private void generateBill(int orderId) {
        billArea.setText("");
        billArea.append("------------------------------------------------\n");
        billArea.append(String.format("%30s\n", "Timeless Restaurant"));
        billArea.append("------------------------------------------------\n\n");
        
        billArea.append("Bill for Order ID: " + orderId + "\n\n");
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
                
                ApplicationData.refreshActiveOrders();
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
