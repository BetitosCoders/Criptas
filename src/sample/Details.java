package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static sample.Controller.clickedNicho;
import static sample.Controller.piedad;

public class Details extends Application implements Initializable {
    private static ConexionMySQL objConexion=new ConexionMySQL("root", "devpass9", "criptas", "localhost", 3306);
    @FXML Label cell, num, seccion, titName, titLastName, address, tel, col, mun, cp,
                ben1Name, ben2Name, dep1, pay, saldo;
    @FXML Button btnBack;

    @Override
    public void start(Stage primaryStage) {
        Stage stage = primaryStage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            String estado = objConexion.conectar();
        } catch (ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage()).show();
        }
        if (piedad) {
            seccion.setText("Piedad");
            cell.setText(clickedNicho.charAt(0)+"");
            num.setText(clickedNicho.charAt(1)+"");
            ResultSet results = objConexion.consultar("SELECT CONCAT(AP_Pat_Dif, ' ', AP_Mat_Dif, ' ', Nombre_Dif) AS dif FROM Piedad WHERE ID_Nicho='"+clickedNicho+"';");
            try {
                while (results.next()) {
                    dep1.setText(results.getString("dif"));
                }
            }
            catch (SQLException x) {
                x.printStackTrace();
            }
        }
        else {
            seccion.setText("Buen Pastor");
            cell.setText(clickedNicho.charAt(0)+"");
            if (clickedNicho.length() > 1) {
                num.setText(clickedNicho.substring(1, clickedNicho.length()));
            }
            else {
                num.setText(clickedNicho.charAt(1) + "");
            }
            ResultSet results = objConexion.consultar("SELECT CONCAT(AP_Pat_Dif, ' ', AP_Mat_Dif, ' ', Nombre_Dif) AS dif FROM Buen_Pastor WHERE ID_Nicho='"+clickedNicho+"';");
            try {
                while (results.next()) {
                    dep1.setText(results.getString("dif"));
                }
            }
            catch (SQLException x) {
                x.printStackTrace();
            }
        }
        ResultSet results = objConexion.consultar("SELECT Nombre, CONCAT(AP_Paterno, ' ', AP_Materno) AS Apellido, CONCAT(Calle, ' ', Numero) AS Direccion, Colonia, Ciudad, Codigo_Postal, Teléfono, Saldo, Ben1, Ben2, Forma_Pago FROM Clientes WHERE ID_Nicho='"+clickedNicho+"';");
        try {
            while (results.next()) {
                titName.setText(results.getString("Nombre"));
                titLastName.setText(results.getString("Apellido"));
                address.setText(results.getString("Direccion"));
                col.setText(results.getString("Colonia"));
                mun.setText(results.getString("Ciudad"));
                cp.setText(results.getString("Codigo_Postal"));
                tel.setText(results.getString("Teléfono"));
                ben1Name.setText(results.getString("Ben1"));
                ben2Name.setText(results.getString("Ben2"));
                saldo.setText(results.getString("Saldo"));
                pay.setText(results.getString("Forma_Pago"));
            }
        }
        catch (SQLException x) {
            x.printStackTrace();
        }
    }

    public void back(ActionEvent ae) {
        Parent parent = null;
        Node source = (Node) ae.getSource();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        try {
            parent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(parent, 1280, 700);
        ((Stage) source.getScene().getWindow()).setScene(scene);
        scene.getWindow().centerOnScreen();
    }
}
