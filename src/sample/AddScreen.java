package sample;


import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static sample.Controller.clientData;

class AddScreen {
    private VBox vBox1 = new VBox();
    private BorderPane borderPane = new BorderPane();
    private HBox[] hBoxes;
    private int entries, cantidad, hboxCount;
    private ConexionMySQL objConexion;
    private String currentTable;
    private String[] optionalEntryNames, requiredEntryNames, otherTableEntries;
    private TextField[] entryTxtFields;
    private Label[] entryLbls = null;
    private Button btnAccept = new Button();
    private Button btnCancel = new Button();
    private int idCliente;

    AddScreen(int _entries, String[] _optionalEntryNames, String[] _requiredEntryNames, String _currentTable ,ConexionMySQL _objConexion, String[] _otherTableEntries) {
        this.entries =_entries;
        this.optionalEntryNames = _optionalEntryNames;
        this.requiredEntryNames = _requiredEntryNames;
        this.currentTable = _currentTable;
        this.objConexion = _objConexion;
        this.otherTableEntries =_otherTableEntries;
    }

    private void generateEntryTxtFields() {
        entryTxtFields = new TextField[entries];
        entryLbls = new Label[entries];
        if (entries%3 == 0) {
            hboxCount = (entries/3)+2;
        }
        else {
            hboxCount = (entries/3)+1;
        }
        hBoxes = new HBox[hboxCount];
        for (int l = 0; l < hboxCount; l++) {
            hBoxes[l] = new HBox();
        }
        VBox[] vBoxes = new VBox[hboxCount * 3];
        int k = 0;
        int j = 0;
        for (int i = 0; i < entries; i++) {
            entryTxtFields[i] = new TextField();
            entryLbls[i] = new Label();
            entryTxtFields[i].setPrefSize(300, 20);
            vBoxes[i] = new VBox();
            vBoxes[i].setPadding(new Insets(0,10,0,10));
            vBoxes[i].getChildren().add(entryLbls[i]);
            vBoxes[i].getChildren().add(entryTxtFields[i]);
            if (j != 2) {
                hBoxes[k].getChildren().add(vBoxes[i]);
                j++;
            }else {
                k++;
                hBoxes[k].getChildren().add(vBoxes[i]);
                j=0;
            }
        }
    }

    private void setLabelText() {
        int i;
        for (i = 0; i < (entries - (otherTableEntries.length)); i++) {
            if (i < requiredEntryNames.length){
                entryLbls[i].setText(requiredEntryNames[i] + "*");
            }
            else {
                entryLbls[i].setText(optionalEntryNames[i - (requiredEntryNames.length)]);
            }
        }
        for (int j = i; j < entries; j++){
            entryLbls[j].setText(otherTableEntries[(j - i)] + "*");
        }
    }

    private void positionEntries() {
        vBox1.getChildren().clear();
        for (int i = 0; i < hboxCount; i ++) {
            vBox1.setFillWidth(false);
            vBox1.setSpacing(10);
            vBox1.getChildren().add(hBoxes[i]);
        }
    }

    private void generateButtons() {
        HBox buttonsH = new HBox();
        btnAccept.setPrefSize(150, 20);
        btnAccept.setText("Agregar");
        btnAccept.addEventHandler(ActionEvent.ACTION, event -> {
            if (checkForEmptyTxt())
                new Alert(Alert.AlertType.ERROR, "No fueron ingresados todos los datos necesarios.").show();
            else {
                switch (currentTable) {
                    case "Clientes" : {
                        int ladoIndex = getLadoIndex();
                        String idNicho = getNichoID();
                        if (!checkLado(ladoIndex)) {
                            new Alert(Alert.AlertType.ERROR, "El lado es incorrecto.").show();
                            break;
                        }
                        else {
                            String queryIns = "INSERT INTO Clientes(Nombre,AP_Materno,AP_Paterno,ID_Nicho,Lado,Saldo,Dirección,Teléfono,Fecha_Ins,Ben1,Ben1_Par,Ben2,Ben2_Par,Lim_Credito)" +
                                    " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                            idCliente = getLastClientID();
                            String queryUpdate = "UPDATE " + entryTxtFields[ladoIndex].getText() + " SET ID_Cliente=?, Estado='Ocupado', Nombre_Dif=?, AP_Pat_Dif=?, AP_Mat_Dif=? WHERE ID_Nicho=?";
                            try {
                                PreparedStatement insert = fillInsertStatementClientes(queryIns);
                                PreparedStatement update = fillUpdateStatementClientes(queryUpdate);
                                insert.execute();
                                update.setString(5, idNicho);
                                update.execute();
                            } catch (SQLException e) {
                                e.printStackTrace();
                                new Alert(Alert.AlertType.INFORMATION, e.getMessage()).show();
                                break;
                            }
                            Node source = (Node) event.getSource();
                            switchScene(source);
                            break;
                        }
                    }
                    case "Pagos" : {
                        List<String> clientInfo = clientData;
                        if (clientInfo != null) {
                            if (checkDouble(0)) {
                                String clientID = clientInfo.get(0);
                                String nichoID = clientInfo.get(2);
                                String nichoType = clientInfo.get(3);
                                LocalDate localDate = LocalDate.now();
                                String date = DateTimeFormatter.ofPattern("yyy/MM/dd").format(localDate);
                                String query = "INSERT INTO Pagos(ID_Cliente, Tipo_Nicho, Fecha, Cantidad, ID_Nicho) VALUES(?,?,?,?,?)";
                                try {
                                    PreparedStatement insert = objConexion.getConexion().prepareStatement(query);
                                    insert.setString(1, clientID);
                                    insert.setString(2, nichoType);
                                    insert.setString(3, date);
                                    insert.setDouble(4, Double.parseDouble(entryTxtFields[0].getText()));
                                    insert.setString(5,nichoID);
                                    insert.execute();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                Node source = (Node) event.getSource();
                                switchScene(source);
                                break;
                            }
                            else {
                                new Alert(Alert.AlertType.ERROR, "Campo incorrecto.").show();
                                break;
                            }
                        }
                        break;
                    }
                    case "Gastos" : {
                        String query = "INSERT INTO Gastos(Descripcion, Monto, Fecha) VALUES(?,?,?)";
                        LocalDate localDate = LocalDate.now();
                        String date = DateTimeFormatter.ofPattern("yyy/MM/dd").format(localDate);
                        if (checkDouble(1)) {
                            try {
                                PreparedStatement insert = objConexion.getConexion().prepareStatement(query);
                                insert.setString(1, entryTxtFields[0].getText());
                                insert.setDouble(2, Double.parseDouble(entryTxtFields[1].getText()));
                                insert.setString(3,date);
                                insert.execute();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            Node source = (Node) event.getSource();
                            switchScene(source);
                            break;
                        }
                        else {
                            new Alert(Alert.AlertType.ERROR, "Campo incorrecto.").show();
                            break;
                        }
                    }
                    case "Documentos" : {
                        List<String> clientInfo = clientData;
                        String clientID = clientInfo.get(0);
                        String docPath = Controller.docPath;
                        LocalDate localDate = LocalDate.now();
                        String date = DateTimeFormatter.ofPattern("yyy/MM/dd").format(localDate);
                        String query = "INSERT INTO Documentos(ID_Cliente, Tipo, Fecha, Ruta) VALUES(?,?,?,?)";
                        try {
                            PreparedStatement insert = objConexion.getConexion().prepareStatement(query);
                            insert.setString(1,clientID);
                            insert.setString(2,entryTxtFields[0].getText());
                            insert.setString(3, date);
                            insert.setString(4, docPath);
                            insert.execute();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        Node source = (Node) event.getSource();
                        switchScene(source);
                        break;
                    }
                }

            }
        });
        btnCancel.setPrefSize(150,20);
        btnCancel.setText("Cancelar");
        btnCancel.addEventHandler(ActionEvent.ACTION, event -> {
            Node source = (Node) event.getSource();
            switchScene(source);
        });
        buttonsH.getChildren().add(btnAccept);
        buttonsH.getChildren().add(btnCancel);
        buttonsH.setSpacing(15);
        vBox1.getChildren().add(buttonsH);
    }

    private PreparedStatement fillInsertStatementClientes(String _query){
        try {
            PreparedStatement insert = objConexion.getConexion().prepareStatement(_query);
            for (int i = 0; i < 14; i++) {
                if (i == 5 || i == 13)  {
                    if (checkDouble(i))
                        insert.setDouble(i+1,Double.parseDouble(entryTxtFields[i].getText()));
                    else {
                        new Alert(Alert.AlertType.ERROR, "Campos Incorrectos.").show();
                        break;
                    }
                }
                else {
                    insert.setString(i+1, entryTxtFields[i].getText());
                }
            }
            return insert;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private PreparedStatement fillUpdateStatementClientes(String _query) {
        try {
            PreparedStatement update = objConexion.getConexion().prepareStatement(_query);
            update.setInt(1, idCliente);
            for (int i = 1; i < 4; i++) {
                update.setString(i+1, entryTxtFields[i+13].getText());
            }
            return update;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getLastClientID() {
        ResultSet idClienteRS = objConexion.consultar("SELECT MAX(ID_Cliente) FROM Clientes");
        try {
            if (idClienteRS.next()) {
                return idClienteRS.getInt("MAX(ID_Cliente)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean checkDouble(int _i) {
        try {
            Double.parseDouble(entryTxtFields[_i].getText());
            return true;
        } catch (Exception e){
            return false;
        }
    }

    private int getLadoIndex() {
        for (int i = 0; i < entryLbls.length; i++) {
            if (entryLbls[i].getText().equals("Lado*")) {
                return i;
            }
        }
        return 0;
    }

    private String getNichoID() {
        for (int i = 0; i < entryLbls.length; i++) {
            if (entryLbls[i].getText().equals("Nicho*")){
                return entryTxtFields[i].getText();
            }
        }
        return null;
    }

    private boolean checkLado(int _ladoIndex) {
        if (entryTxtFields[_ladoIndex].getText().equals("Derecho")) {
            entryTxtFields[_ladoIndex].setText("Piedad");
            return true;
        }
        else if (entryTxtFields[_ladoIndex].getText().equals("Izquierdo")) {
            entryTxtFields[_ladoIndex].setText("Buen_Pastor");
            return true;
        }
        else {
            return false;
        }
    }

    private void switchScene(Node source) {
        Parent parent = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
            parent = loader.load();
            (source.getScene().getWindow()).setWidth(1280);
            (source.getScene().getWindow()).setHeight(700);
            ((Stage)source.getScene().getWindow()).setScene(new Scene(parent, 1280, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkForEmptyTxt() {
        int i;
        for (i = 0; i < (entries - (optionalEntryNames.length + otherTableEntries.length)); i++) {
            if (entryTxtFields[i].getText().isEmpty()) {
                return true;
            }
        }
        for (int j = i; j < (entries - otherTableEntries.length); j++) {
            if (entryTxtFields[j].getText().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void positionScroll() {
        borderPane.setCenter(vBox1);
        borderPane.setPadding(new Insets(10,0,10,10));
        vBox1.setAlignment(Pos.CENTER);
        VBox.setVgrow(vBox1, Priority.ALWAYS);
    }

    Scene makeScene() {
        generateEntryTxtFields();
        setLabelText();
        positionEntries();
        generateButtons();
        positionScroll();
        return new Scene(borderPane, 1280, 700);
    }
}
