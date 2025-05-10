package bookshopmanagementsystem;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomerController implements Initializable {

    @FXML private AnchorPane main_form;
    @FXML private Label usernameLabel;
    @FXML private ComboBox<String> purchase_bookTitle;
    @FXML private Spinner<Integer> purchase_quantity;
    @FXML private Label purchase_info_bookID, purchase_info_bookTitle,
            purchase_info_author, purchase_info_genre,
            purchase_info_date, purchase_total;
    @FXML private TableView<customerData> purchase_tableView;
    @FXML private TableColumn<customerData,Integer> purchase_col_bookID;
    @FXML private TableColumn<customerData,String>  purchase_col_bookTitle,
            purchase_col_author,
            purchase_col_genre;
    @FXML private TableColumn<customerData,Integer> purchase_col_quantity;
    @FXML private TableColumn<customerData,Double>  purchase_col_price;

    private double xOffset, yOffset;
    private int customerId;
    private double displayTotal;
    private ObservableList<customerData> purchaseCustomerList;

    @Override
    public void initialize(URL loc, ResourceBundle rb) {
        // show logged-in user
        usernameLabel.setText(getData.username);

        // draggable window
        main_form.setOnMousePressed((MouseEvent e)->{
            xOffset=e.getSceneX(); yOffset=e.getSceneY();
        });
        main_form.setOnMouseDragged((MouseEvent e)->{
            Stage s=(Stage)main_form.getScene().getWindow();
            s.setX(e.getScreenX()-xOffset);
            s.setY(e.getScreenY()-yOffset);
        });

        purchaseDisplayQTY();
        loadBookTitles();
        purchaseShowCustomerListData();
        purchaseDisplayTotal();
    }

    private void purchaseDisplayQTY() {
        SpinnerValueFactory<Integer> f=
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1,10,1);
        purchase_quantity.setValueFactory(f);
    }

    private void loadBookTitles() {
        String sql="SELECT title FROM book";
        try(Connection conn=database.connectDb();
            PreparedStatement p=conn.prepareStatement(sql);
            ResultSet rs=p.executeQuery()) {
            ObservableList<String> list=FXCollections.observableArrayList();
            while(rs.next()) list.add(rs.getString("title"));
            purchase_bookTitle.setItems(list);
            purchase_bookTitle.setOnAction(e->onTitleSelected());
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void onTitleSelected() {
        String title=purchase_bookTitle.getValue();
        if(title==null) return;
        String sql="SELECT * FROM book WHERE title=?";
        try(Connection conn=database.connectDb();
            PreparedStatement p=conn.prepareStatement(sql)) {
            p.setString(1,title);
            ResultSet rs=p.executeQuery();
            if(rs.next()) {
                purchase_info_bookID.setText(rs.getString("book_id"));
                purchase_info_bookTitle.setText(title);
                purchase_info_author.setText(rs.getString("author"));
                purchase_info_genre.setText(rs.getString("genre"));
                purchase_info_date.setText(rs.getString("pub_date"));
            }
        }catch(Exception e){e.printStackTrace();}
    }

    private void assignCustomerId() {
        try(Connection conn=database.connectDb()) {
            // current cart max
            PreparedStatement p1=conn.prepareStatement(
                    "SELECT MAX(customer_id) FROM customer");
            ResultSet r1=p1.executeQuery(); if(r1.next())
                customerId=r1.getInt(1);

            // last completed
            PreparedStatement p2=conn.prepareStatement(
                    "SELECT MAX(customer_id) FROM customer_info");
            ResultSet r2=p2.executeQuery();
            int m2=r2.next()?r2.getInt(1):0;

            if(customerId==0) customerId=1;
            else if(customerId==m2) customerId=m2+1;

        } catch(Exception e){e.printStackTrace();}
    }

    @FXML
    public void purchaseAdd() {
        if(purchase_bookTitle.getValue()==null) {
            showAlert(AlertType.ERROR,"Error","Select a book");
            return;
        }
        assignCustomerId();
        int qty=purchase_quantity.getValue();
        double unit=0;
        try(Connection conn=database.connectDb();
            PreparedStatement p=conn.prepareStatement(
                    "SELECT price FROM book WHERE title=?")) {
            p.setString(1,purchase_bookTitle.getValue());
            ResultSet r=p.executeQuery();
            if(r.next()) unit=r.getDouble("price");
        }catch(Exception e){e.printStackTrace();}

        double totalLine=unit*qty;
        String sql="INSERT INTO customer "
                + "(customer_id, book_id, title, author, genre, quantity, price, date) "
                + "VALUES (?,?,?,?,?,?,?,CURDATE())";
        try(Connection conn=database.connectDb();
            PreparedStatement p=conn.prepareStatement(sql)) {
            p.setInt(1,customerId);
            p.setInt(2,Integer.parseInt(purchase_info_bookID.getText()));
            p.setString(3,purchase_info_bookTitle.getText());
            p.setString(4,purchase_info_author.getText());
            p.setString(5,purchase_info_genre.getText());
            p.setInt(6,qty);
            p.setDouble(7,totalLine);
            p.executeUpdate();
        }catch(Exception e){e.printStackTrace();}

        purchaseShowCustomerListData();
        purchaseDisplayTotal();
    }

    private void purchaseShowCustomerListData() {
        assignCustomerId();
        purchaseCustomerList=FXCollections.observableArrayList();
        String sql="SELECT * FROM customer WHERE customer_id=?";
        try(Connection conn=database.connectDb();
            PreparedStatement p=conn.prepareStatement(sql)) {
            p.setInt(1,customerId);
            ResultSet r=p.executeQuery();
            while(r.next()) {
                purchaseCustomerList.add(new customerData(
                        r.getInt("customer_id"),
                        r.getInt("book_id"),
                        r.getString("title"),
                        r.getString("author"),
                        r.getString("genre"),
                        r.getInt("quantity"),
                        r.getDouble("price"),
                        r.getDate("date")
                ));
            }
        }catch(Exception e){e.printStackTrace();}

        purchase_col_bookID.setCellValueFactory(
                new PropertyValueFactory<>("bookId"));
        purchase_col_bookTitle.setCellValueFactory(
                new PropertyValueFactory<>("title"));
        purchase_col_author.setCellValueFactory(
                new PropertyValueFactory<>("author"));
        purchase_col_genre.setCellValueFactory(
                new PropertyValueFactory<>("genre"));
        purchase_col_quantity.setCellValueFactory(
                new PropertyValueFactory<>("quantity"));
        purchase_col_price.setCellValueFactory(
                new PropertyValueFactory<>("price"));

        purchase_tableView.setItems(purchaseCustomerList);
    }

    private void purchaseDisplayTotal() {
        String sql="SELECT SUM(price) FROM customer WHERE customer_id="+customerId;
        try(Connection conn=database.connectDb();
            PreparedStatement p=conn.prepareStatement(sql);
            ResultSet r=p.executeQuery()) {
            if(r.next()) displayTotal=r.getDouble(1);
            purchase_total.setText("$"+displayTotal);
        }catch(Exception e){e.printStackTrace();}
    }

    @FXML
    public void purchasePay() {
        if(displayTotal==0) {
            showAlert(AlertType.ERROR,"Error","Cart empty");
            return;
        }
        Alert a=new Alert(AlertType.CONFIRMATION,
                "Confirm payment of $"+displayTotal,
                ButtonType.OK,ButtonType.CANCEL);
        a.setHeaderText(null);
        Optional<ButtonType> opt=a.showAndWait();
        if(opt.isPresent() && opt.get()==ButtonType.OK) {
            try(Connection conn=database.connectDb()) {
                PreparedStatement p1=conn.prepareStatement(
                        "INSERT INTO customer_info (customer_id,total,date) "
                                + "VALUES (?,?,CURDATE())");
                p1.setInt(1,customerId);
                p1.setDouble(2,displayTotal);
                p1.executeUpdate();

                PreparedStatement p2=conn.prepareStatement(
                        "DELETE FROM customer WHERE customer_id=?");
                p2.setInt(1,customerId);
                p2.executeUpdate();

                purchase_tableView.getItems().clear();
                displayTotal=0;
                purchase_total.setText("$0.0");
                showAlert(AlertType.INFORMATION,"Done","Payment successful");
            }catch(Exception e){e.printStackTrace();}
        }
    }

    @FXML
    public void logout() throws Exception {
        ((Stage)main_form.getScene().getWindow()).close();
        Parent root=FXMLLoader.load(
                getClass().getResource("CustomerLogin.fxml")
        );
        Stage s=new Stage();
        s.initStyle(StageStyle.TRANSPARENT);
        s.setScene(new Scene(root));
        s.show();
    }

    @FXML
    public void close() {
        ((Stage)main_form.getScene().getWindow()).close();
    }

    @FXML
    public void minimize() {
        ((Stage)main_form.getScene().getWindow()).setIconified(true);
    }

    private void showAlert(AlertType t,String title,String msg) {
        Alert a=new Alert(t,msg,ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
