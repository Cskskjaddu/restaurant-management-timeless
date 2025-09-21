import javax.swing.SwingUtilities;

public class RestaurantApp {
    public static void main(String[] args) {
        ApplicationData.loadAllData();
        
        SwingUtilities.invokeLater(() -> {
            GatewayWindow gateway = new GatewayWindow();
            gateway.setVisible(true);
        });
    }
}
