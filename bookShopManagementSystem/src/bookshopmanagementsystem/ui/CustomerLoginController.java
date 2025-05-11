package bookshopmanagementsystem.ui;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import bookshopmanagementsystem.patterns.LoginController;
import bookshopmanagementsystem.patterns.database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomerLoginController implements Initializable, LoginController {

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button loginBtn, close;

    private double xOffset, yOffset;

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    @Override
    public void login() {
        if (username.getText().isEmpty() || password.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Fill all fields");
            return;
        }

        String sql = "SELECT * FROM customer_login WHERE username=? AND password=?";
        try (Connection conn = database.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username.getText());
            pst.setString(2, password.getText());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Stage oldStage = (Stage) loginBtn.getScene().getWindow();
                oldStage.close();

                Parent root = FXMLLoader.load(getClass().getResource("customer.fxml"));
                Stage stage = new Stage();
                stage.initStyle(StageStyle.TRANSPARENT);
                Scene scene = new Scene(root);

                root.setOnMousePressed((MouseEvent e) -> {
                    xOffset = e.getSceneX();
                    yOffset = e.getSceneY();
                });

                root.setOnMouseDragged((MouseEvent e) -> {
                    stage.setX(e.getScreenX() - xOffset);
                    stage.setY(e.getScreenY() - yOffset);
                });

                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Bad credentials");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unexpected error");
        }
    }

    @FXML
    public void loginCustomer() {
        login(); // Direct call since this class already implements LoginController
    }

    @FXML
    public void switchToAdminLogin() throws Exception {
        ((Stage) loginBtn.getScene().getWindow()).close();
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void close(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    public void minimize(ActionEvent event) {
        ((Stage) ((Button) event.getSource()).getScene().getWindow()).setIconified(true);
    }

    @FXML
    public void logout(ActionEvent event) {
        try {
            ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
            Parent root = FXMLLoader.load(getClass().getResource("CustomerLogin.fxml"));
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not logout");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
