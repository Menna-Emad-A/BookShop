package bookshopmanagementsystem.patterns;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class AdminLoginFactory implements LoginControllerFactory {
    @Override
    public Parent createLoginUI() {
        try {
            return FXMLLoader.load(getClass().getResource("/bookshopmanagementsystem/ui/FXMLDocument.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
