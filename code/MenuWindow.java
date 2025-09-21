import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.Font;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MenuWindow extends JFrame {
    private JTable menuTable;
    private DefaultTableModel tableModel;
    private JTextField tableNumberField;
    private JButton placeOrderButton;
    private JButton mainMenuButton;

    public MenuWindow() {
        setTitle("Timeless Restaurant - Customer Menu & Ordering");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(Color.decode("#121212"));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Welcome to Timeless Restaurant", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.decode("#FFFFFF"));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Picture", "Item Name", "Price", "Select"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return ImageIcon.class;
                if (columnIndex == 4) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        menuTable = new JTable(tableModel);
        menuTable.setRowHeight(75);
        menuTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        menuTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        menuTable.getTableHeader().setBackground(Color.decode("#212121"));
        menuTable.getTableHeader().setForeground(Color.decode("#FFFFFF"));
        menuTable.setBackground(Color.decode("#303030"));
        menuTable.setForeground(Color.decode("#FFFFFF"));
        menuTable.setGridColor(Color.decode("#424242"));
        menuTable.setSelectionBackground(Color.decode("#4CAF50"));
        menuTable.setSelectionForeground(Color.decode("#FFFFFF"));

        menuTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        menuTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        menuTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        menuTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        menuTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        
        menuTable.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        
        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setBackground(Color.decode("#303030"));
        scrollPane.getViewport().setBackground(Color.decode("#303030"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel orderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        orderPanel.setBackground(Color.decode("#212121"));
        orderPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel tableLabel = new JLabel("Enter Your Table Number:", SwingConstants.RIGHT);
        tableLabel.setForeground(Color.decode("#FFFFFF"));
        tableNumberField = new JTextField(5);

        placeOrderButton = new JButton("Place Order");
        placeOrderButton.setBackground(Color.decode("#4CAF50"));
        placeOrderButton.setForeground(Color.decode("#FFFFFF"));
        placeOrderButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        placeOrderButton.setFocusPainted(false);

        mainMenuButton = new JButton("Main Menu");
        mainMenuButton.setBackground(Color.decode("#03A9F4"));
        mainMenuButton.setForeground(Color.decode("#FFFFFF"));
        mainMenuButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainMenuButton.setFocusPainted(false);

        orderPanel.add(tableLabel);
        orderPanel.add(tableNumberField);
        orderPanel.add(placeOrderButton);
        orderPanel.add(mainMenuButton);
        
        mainPanel.add(orderPanel, BorderLayout.SOUTH);
        add(mainPanel);

        loadMenuData();
        
        placeOrderButton.addActionListener(e -> placeOrder());

        mainMenuButton.addActionListener(e -> {
            GatewayWindow gateway = new GatewayWindow();
            gateway.setVisible(true);
            dispose();
        });
    }

    private void loadMenuData() {
        for (ApplicationData.MenuItem item : ApplicationData.menuItems) {
            Vector<Object> row = new Vector<>();
            row.add(item.id);
            try {
                ImageIcon imageIcon = new ImageIcon(new ImageIcon(item.id + ".png").getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH));
                row.add(imageIcon);
            } catch (Exception e) {
                row.add(null); // No image found
            }
            row.add(item.name);
            row.add(item.price);
            row.add(false);
            tableModel.addRow(row);
        }
    }

    private class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                JLabel label = new JLabel((ImageIcon) value);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            } else {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
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
            if ((Boolean) tableModel.getValueAt(i, 4)) {
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
            
            ApplicationData.refreshActiveOrders();
            JOptionPane.showMessageDialog(this, "Order placed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (java.sql.SQLException se) {
                se.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Failed to place order.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            DatabaseManager.releaseConnection(conn);
        }
    }
}