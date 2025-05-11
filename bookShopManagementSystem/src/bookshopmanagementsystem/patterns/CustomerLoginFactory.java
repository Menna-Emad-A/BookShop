package bookshopmanagementsystem.patterns;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class CustomerLoginFactory implements LoginControllerFactory {
    @Override
    public Parent createLoginUI() {
        try {
            return FXMLLoader.load(getClass().getResource("/bookshopmanagementsystem/ui/CustomerLogin.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
