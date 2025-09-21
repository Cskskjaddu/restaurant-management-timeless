import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class GatewayWindow extends JFrame {
    public GatewayWindow() {
        setTitle("Timeless Restaurant");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ImageIcon backgroundImage = new ImageIcon("gw.png");
        Image scaledImage = backgroundImage.getImage().getScaledInstance(800, 500, Image.SCALE_SMOOTH);
        JLabel backgroundLabel = new JLabel(new ImageIcon(scaledImage));
        backgroundLabel.setLayout(new BorderLayout());
        backgroundLabel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        welcomePanel.setBackground(new Color(50, 50, 50, 200));
        JLabel welcomeLabel = new JLabel("Welcome to Timeless Restaurant");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.add(welcomeLabel);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton customerButton = new JButton("Customer");
        JButton staffButton = new JButton("Staff");

        customerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        customerButton.setBackground(new Color(50, 50, 50));
        customerButton.setForeground(Color.WHITE);
        customerButton.setFocusPainted(false);
        customerButton.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));

        staffButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        staffButton.setBackground(new Color(50, 50, 50));
        staffButton.setForeground(Color.WHITE);
        staffButton.setFocusPainted(false);
        staffButton.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));

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

        buttonPanel.add(customerButton);
        buttonPanel.add(staffButton);
        
        JPanel quotePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        quotePanel.setBackground(new Color(50, 50, 50, 200));
        JLabel quoteLabel = new JLabel("\"The Grand Hotel - Where every meal is a memory.\"", SwingConstants.CENTER);
        quoteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        quoteLabel.setForeground(Color.WHITE);
        quotePanel.add(quoteLabel);

        backgroundLabel.add(welcomePanel, BorderLayout.NORTH);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(buttonPanel, gbc);
        
        backgroundLabel.add(contentPanel, BorderLayout.CENTER);
        backgroundLabel.add(quotePanel, BorderLayout.SOUTH);

        add(backgroundLabel);
    }
}
