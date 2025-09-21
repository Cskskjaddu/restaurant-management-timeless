Timeless Restaurant Management System
This is a comprehensive desktop application built with Java Swing for a restaurant. The system allows customers to place orders from an interactive menu and enables staff to manage billing and track active orders. It uses a modular architecture with an in-memory data cache to ensure high performance.

Key Features
Customer Module: A user-friendly interface where customers can browse the menu with images and place orders.

Staff Module: A secure login system for staff to access a real-time billing system for active orders.

Database Integration: Connects to a MySQL database to manage all restaurant data.

Performance Optimization: Implements an in-memory data cache to reduce database queries and improve application speed.

How to Run
Dependencies: Ensure you have Java JDK 8+ and the MySQL Connector/J library.

Database: The application requires a MySQL database with menu, orders, and staff tables. Update the database connection details in DatabaseManager.java.

Image Assets: Place all image files (gw.png, log.png, 1.png, etc.) in the project's root directory.

Compile & Run:

javac -cp .:mysql-connector-java-8.0.28.jar *.java
java -cp .:mysql-connector-java-8.0.28.jar RestaurantApp

(Note: Adjust the MySQL Connector file name as needed.)

Project Structure
RestaurantApp.java: Main entry point.

GatewayWindow.java: Welcome screen.

MenuWindow.java: Customer ordering interface.

StaffLoginWindow.java: Staff login screen.

BillingWindow.java: Staff billing system.

DatabaseManager.java: Handles database connections.

ApplicationData.java: Manages the in-memory data cache.
