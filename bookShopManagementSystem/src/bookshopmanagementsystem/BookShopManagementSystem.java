package bookshopmanagementsystem;

import bookshopmanagementsystem.patterns.AdminLoginFactory;
import bookshopmanagementsystem.patterns.CustomerLoginFactory;
import bookshopmanagementsystem.patterns.LoginControllerFactory;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BookShopManagementSystem extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // ممكن تخلي دا جاي من اختيار المستخدم مثلاً من شاشة أولية
        String userType = "customer"; // أو "admin"

        LoginControllerFactory factory;

        if ("admin".equalsIgnoreCase(userType)) {
            factory = new AdminLoginFactory();
        } else {
            factory = new CustomerLoginFactory();
        }

        // الحصول على الواجهة بناءً على نوع المستخدم
        Parent root = factory.createLoginUI();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Bookshop Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
