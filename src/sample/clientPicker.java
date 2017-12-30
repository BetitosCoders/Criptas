package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class clientPicker extends Application {
    @FXML TextField txtSearch;
    @FXML ListView listClients;
    private static ConexionMySQL objConexion;
    @FXML Button btnAccept;
    public static String clientId, clientName, clientNicho, tipoNicho;

    @Override
    public void start(Stage primaryStage) {}

    public void initialize() throws SQLException {
        objConexion = new ConexionMySQL("root", "devpass9", "Criptas", "localhost", 3306);
        try {
            String estado = objConexion.conectar();
        } catch (ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage()).show();
        }
        ResultSet results = objConexion.consultar("SELECT ID_Cliente, Nombre, ID_Nicho FROM Clientes");
        while (results.next()) {
            String idClient = results.getString("ID_Cliente");
            String name = results.getString("Nombre");
            String idNicho = results.getString("ID_Nicho");
            listClients.getItems().add(idClient + ": " + name + ": " + idNicho);
        }
    }

    public void search() throws SQLException {
        ResultSet results = objConexion.consultar("SELECT ID_Cliente, Nombre, ID_Nicho FROM Clientes WHERE Nombre LIKE '%" + txtSearch.getText() + "%'");
        listClients.getItems().clear();
        while (results.next()) {
            String idClient = results.getString("ID_Cliente");
            String name = results.getString("Nombre");
            String idNicho = results.getString("ID_Nicho");
            listClients.getItems().add(idClient + ": " + name + ": " + idNicho);
        }
    }

    public void accept(ActionEvent ev) throws SQLException {
        if (listClients.getSelectionModel().getSelectedIndex() == -1) {
            new Alert(Alert.AlertType.ERROR, "Seleccione un cliente.").show();
        }
        else {
            String[] output = listClients.getSelectionModel().getSelectedItem().toString().split(":");
            clientId = output[0];
            clientName = output[1];
            clientNicho = output[2];
            if (Controller.currentTable.equals("Documentos")){
                String[] optionalEntries = new String[]{};
                String[] requiredEntries = new String[]{"Tipo", "Fecha"};
                String[] otherTableEntries = new String[]{};
                copyFile(Controller.doc);
                Node source = (Node) ev.getSource();
                AddScreen pagoScreen = new AddScreen(2, optionalEntries, requiredEntries, Controller.currentTable, objConexion, otherTableEntries);
                ((Stage) source.getScene().getWindow()).setScene(pagoScreen.makeScene());
            }
            else {
                ResultSet lado = objConexion.consultar("SELECT Lado FROM Clientes WHERE ID_Cliente=" + clientId);
                ResultSet nichoType = null;
                while (lado.next()) {
                    String table;
                    if (lado.getString("Lado").equals("Derecho")) {
                        table = "piedad";
                    } else {
                        table = "buen_pastor";
                    }
                    nichoType = objConexion.consultar("SELECT Tipo_Nicho FROM " + table + " WHERE ID_Cliente=" + clientId);
                }
                // Se utiliza una consulta para saber que tipo de nicho esta utilizando el cliente.
                if (nichoType != null) {
                    while (nichoType.next()) {
                        tipoNicho = nichoType.getString("Tipo_Nicho");
                    }
                }
                String[] optionalEntries = new String[]{};
                String[] requiredEntries = new String[]{"Cantidad", "Fecha"};
                String[] otherTableEntries = new String[]{};
                Node source = (Node) ev.getSource();
                AddScreen pagoScreen = new AddScreen(2, optionalEntries, requiredEntries, Controller.currentTable, objConexion, otherTableEntries);
                ((Stage) source.getScene().getWindow()).setScene(pagoScreen.makeScene());
            }
        }
    }

    public void cancel(ActionEvent ev) {
        Node source = (Node) ev.getSource();
        Parent parent = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            parent = loader.load();
            Scene scene=new Scene(parent,1280,700);
            ((Stage) source.getScene().getWindow()).setScene(scene);
            scene.getWindow().centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyFile(File _doc) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(cal.getTime());
        File docOut = new File(clientPicker.clientName + ":" + clientPicker.clientNicho + "-" + time);
        try {
            Files.copy(_doc.toPath(), docOut.toPath());
            Controller.docPath = docOut.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
