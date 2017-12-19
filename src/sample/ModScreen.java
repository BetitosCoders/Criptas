package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.xml.soap.Text;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static sample.Controller.clientData;

public class ModScreen {
    private String[] requiredEntryNames, optionalEntryNames, otherTableEntries;
    private TextField[] entryTxtFields;
    private Label[] entryLbls;
    private int entries;
    private int hboxCount;
    private int idCliente;
    private VBox vBox1 = new VBox();
    private BorderPane borderPane = new BorderPane();
    private HBox[] hBoxes;
    private Button btnAccept = new Button();
    private Button btnCancel = new Button();
    private String currentTable;
    private String ID;
    private static ConexionMySQL objConexion;
    ComboBox cmbClients = new ComboBox();
    VBox cmbVbox = new VBox();

    ModScreen(String[] requiredEntryNames, String[] optionalEntryNames, String[] otherTableEntries, int entries, String currentTable, ConexionMySQL objConexion, String ID) {
        this.requiredEntryNames = requiredEntryNames;
        this.optionalEntryNames = optionalEntryNames;
        this.otherTableEntries = otherTableEntries;
        this.entries = entries;
        this.currentTable = currentTable;
        ModScreen.objConexion = objConexion;
        this.ID = ID;
    }

    private void generateEntryTxtFields() {
        entryTxtFields = new javafx.scene.control.TextField[entries];
        entryLbls = new javafx.scene.control.Label[entries];
        if (entries % 3 == 0) {
            hboxCount = (entries / 3) + 2;
        } else {
            hboxCount = (entries / 3) + 1;
        }
        hBoxes = new HBox[hboxCount];
        for (int l = 0; l < hboxCount; l++) {
            hBoxes[l] = new HBox();
        }
        VBox[] vBoxes = new VBox[hboxCount * 3];
        int k = 0;
        int j = 0;
        boolean cmbAdded = false;
        for (int i = 0; i < entries; i++) {
            entryTxtFields[i] = new javafx.scene.control.TextField();
            entryLbls[i] = new javafx.scene.control.Label();
            entryTxtFields[i].setPrefSize(300, 20);
            vBoxes[i] = new VBox();
            vBoxes[i].setPadding(new Insets(0, 10, 0, 10));
            vBoxes[i].getChildren().add(entryLbls[i]);
            vBoxes[i].getChildren().add(entryTxtFields[i]);
            if ((currentTable.equals("Pagos") || currentTable.equals("Documentos")) && !cmbAdded){
                hBoxes[k].getChildren().add(cmbVbox);
                j++;
                cmbAdded = true;
            }
            if (j != 2) {
                hBoxes[k].getChildren().add(vBoxes[i]);
                j++;
            } else {
                hBoxes[k].getChildren().add(vBoxes[i]);
                j = 0;
                k++;
            }
        }
    }

    private void setLabelText() {
        int i;
        for (i = 0; i < (entries - (otherTableEntries.length)); i++) {
            if (i < requiredEntryNames.length) {
                entryLbls[i].setText(requiredEntryNames[i] + "*");
            } else {
                entryLbls[i].setText(optionalEntryNames[i - (requiredEntryNames.length)]);
            }
        }
        for (int j = i; j < entries; j++) {
            entryLbls[j].setText(otherTableEntries[(j - i)] + "*");
        }
    }

    private void positionEntries() {
        vBox1.getChildren().clear();
        for (int i = 0; i < hboxCount; i++) {
            vBox1.setFillWidth(false);
            vBox1.setSpacing(10);
            vBox1.getChildren().add(hBoxes[i]);
        }
    }

    private void generateButtons() {
        HBox buttonsH = new HBox();
        btnAccept.setPrefSize(150, 20);
        btnAccept.setText("Acceptar");
        btnAccept.addEventHandler(ActionEvent.ACTION, event -> {
            if (checkForEmptyTxt())
                new Alert(Alert.AlertType.ERROR, "No fueron ingresados todos los datos necesarios.").show();
            else {
                switch (currentTable) {
                    case "Clientes": {
                        int ladoIndex = getIndex("Lado*");
                        String date = entryTxtFields[getIndex("Fecha de Inscripción*")].getText();
                        String idNicho = entryTxtFields[getIndex("Nicho*")].getText();
                        if (!checkLado(ladoIndex) || !checkNicho(idNicho)) {
                            new Alert(Alert.AlertType.ERROR, "El lado es incorrecto.").show();
                            break;
                        }
                        else if (!checkNicho(idNicho)) {
                            new Alert(Alert.AlertType.ERROR, "El nicho es incorrecto.").show();
                            break;
                        }
                        else if (!checkDate(date)) {
                            new Alert(Alert.AlertType.ERROR, "La fecha es incorrecta.").show();
                            break;
                        }
                        else {
                            String queryIns = "UPDATE CLientes SET Nombre=?,AP_Paterno=?,AP_Materno=?,ID_Nicho=?," +
                                    "Lado=?,Saldo=?,Dirección=?,Teléfono=?,Fecha_Ins=?,Ben1=?,Ben1_Par=?," +
                                    "Ben2=?,Ben2_Par=?,Lim_Credito=? WHERE ID_Cliente=?";
                            idCliente = getLastClientID();
                            String queryUpdate = "UPDATE " + entryTxtFields[ladoIndex].getText() + " SET ID_Cliente=?, Estado='Ocupado', Nombre_Dif=?, AP_Pat_Dif=?, AP_Mat_Dif=? WHERE ID_Nicho=?";
                            try {
                                PreparedStatement insert = fillUpdate1StatementClientes(queryIns);
                                PreparedStatement update = fillUpdate2StatementClientes(queryUpdate);
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
                    case "Pagos": {
                        String nicho = entryTxtFields[getIndex("Nicho*")].getText();
                        String date = entryTxtFields[getIndex("Fecha*")].getText();
                        String type = entryTxtFields[getIndex("Tipo de Nicho*")].getText();
                        if (!checkDouble(getIndex("Cantidad*"))) {
                            new Alert(Alert.AlertType.ERROR, "La cantidad es incorrecta.").show();
                            break;
                        }
                        else if (!checkNicho(nicho)) {
                            new Alert(Alert.AlertType.ERROR, "El nicho es incorrecto.").show();
                            break;
                        }
                        else if (!checkDate(date)){
                            new Alert(Alert.AlertType.ERROR, "La fecha es incorrecta.").show();
                            break;
                        }
                        else if (!checkType(type)) {
                            new Alert(Alert.AlertType.ERROR, "El tipo es incorrecto.").show();
                            break;
                        }
                        else {
                            String queryPago = "UPDATE Pagos SET Cantidad=?, Fecha=?, ID_Nicho=?, Tipo_Nicho=?, ID_Cliente=? WHERE ID_Pago=?";
                            try {
                                PreparedStatement update = objConexion.getConexion().prepareStatement(queryPago);
                                for(int i=0;i<entryTxtFields.length;i++){
                                    update.setString(i+1, entryTxtFields[i].getText());
                                }
                                String idClient = cmbClients.getSelectionModel().getSelectedItem().toString();
                                String[] idClientSep = idClient.split(":");
                                idClient = idClientSep[0];
                                update.setString(5, idClient);
                                update.setString(6, ID);
                                update.execute();
                            } catch (SQLException e) {
                                new Alert(Alert.AlertType.INFORMATION, e.getMessage()).show();
                                e.printStackTrace();
                            }
                            Node source = (Node) event.getSource();
                            switchScene(source);
                            break;
                        }
                    }
                    case "Gastos": {
                        String date = entryTxtFields[getIndex("Fecha*")].getText();
                        if (!checkDouble(getIndex("Monto*"))){
                            new Alert(Alert.AlertType.ERROR, "El monto es incorrecto.").show();
                            break;
                        }
                        else if (!checkDate(date)){
                            new Alert(Alert.AlertType.ERROR, "La fecha es incorrecta.").show();
                            break;
                        }
                        else {
                            String queryGasto="UPDATE Gastos SET Descripción=?, Monto=?, Fecha=? WHERE ID_Gasto=?";
                            try {
                                PreparedStatement update = objConexion.getConexion().prepareStatement(queryGasto);
                                for(int i=0;i<entryTxtFields.length;i++){
                                    update.setString(i+1, entryTxtFields[i].getText());
                                }
                                update.setString(4, ID);
                                update.execute();
                            } catch (SQLException e) {
                                new Alert(Alert.AlertType.INFORMATION, e.getMessage()).show();
                                e.printStackTrace();
                            }
                            Node source = (Node) event.getSource();
                            switchScene(source);
                            break;
                        }
                    }
                    case "Documentos": {
                        String date = entryTxtFields[getIndex("Fecha*")].getText();
                        if (!checkDate(date)) {
                            new Alert(Alert.AlertType.ERROR, "La fecha es incorrecta.").show();
                            break;
                        }
                        else {
                            String queryGasto="UPDATE Documentos SET Tipo=?, Fecha=?, ID_Cliente=? WHERE ID_Documentos=?";
                            try {
                                PreparedStatement update = objConexion.getConexion().prepareStatement(queryGasto);
                                for(int i=0;i<entryTxtFields.length;i++){
                                    update.setString(i+1, entryTxtFields[i].getText());
                                }
                                String idClient = cmbClients.getSelectionModel().getSelectedItem().toString();
                                String[] idClientSep = idClient.split(":");
                                idClient = idClientSep[0];
                                update.setString(3, idClient);
                                update.setString(4, ID);
                                update.execute();
                            } catch (SQLException e) {
                                new Alert(Alert.AlertType.INFORMATION, e.getMessage()).show();
                                e.printStackTrace();
                            }
                            Node source = (Node) event.getSource();
                            switchScene(source);
                            break;
                        }
                    }
                    case "Ingresos":{
                        String date = entryTxtFields[getIndex("Fecha*")].getText();
                        if (!checkDate(date)) {
                            new Alert(Alert.AlertType.ERROR, "La fecha es incorrecta.").show();
                            break;
                        }
                        else if (checkDouble(getIndex("Monto*"))) {
                            new Alert(Alert.AlertType.ERROR, "El monto es incorrecto.").show();
                            break;
                        }
                        else {
                            String queryGasto="UPDATE Ingresos SET Descripción=?, Monto=?, Fecha=? WHERE ID_Ingreso=?";
                            try {
                                PreparedStatement update = objConexion.getConexion().prepareStatement(queryGasto);
                                for(int i=0;i<entryTxtFields.length;i++){
                                    update.setString(i+1, entryTxtFields[i].getText());
                                }
                                update.setString(4, ID);
                                update.execute();
                            } catch (SQLException e) {
                                new Alert(Alert.AlertType.INFORMATION, e.getMessage()).show();
                                e.printStackTrace();
                            }
                            Node source = (Node) event.getSource();
                            switchScene(source);
                            break;
                        }
                    }
                }

            }
        });
        btnCancel.setPrefSize(150, 20);
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

    private PreparedStatement fillUpdate1StatementClientes(String _query) {
        try {
            PreparedStatement insert = objConexion.getConexion().prepareStatement(_query);
            for (int i = 0; i < 14; i++) {
                if (i == 5 || i == 13) {
                    if (checkDouble(i))
                        insert.setDouble(i + 1, Double.parseDouble(entryTxtFields[i].getText()));
                    else {
                        new Alert(Alert.AlertType.ERROR, "Campos Incorrectos.").show();
                        break;
                    }
                } else {
                    insert.setString(i + 1, entryTxtFields[i].getText());
                }
            }
            insert.setString(15, ID);
            return insert;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private PreparedStatement fillUpdate2StatementClientes(String _query) {
        try {
            PreparedStatement update = objConexion.getConexion().prepareStatement(_query);
            update.setInt(1, idCliente);
            for (int i = 1; i < 4; i++) {
                update.setString(i + 1, entryTxtFields[i + 13].getText());
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
        } catch (Exception e) {
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
            ((Stage) source.getScene().getWindow()).setScene(new Scene(parent, 1280, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkForEmptyTxt() {
        for(int i=0;i<entryLbls.length;i++){
            if(entryLbls[i].getText().matches(".*\\*$")){
                String x=entryTxtFields[i].getText();
                if(x == null || x.isEmpty()){
                    return true;
                }
            }
        }
        return false;
    }

    private void positionScroll() {
        borderPane.setCenter(vBox1);
        borderPane.setPadding(new Insets(10, 0, 10, 10));
        vBox1.setAlignment(Pos.CENTER);
        VBox.setVgrow(vBox1, Priority.ALWAYS);
    }

    private int getIndex(String labelText) {
        for (int i=0;i<entryLbls.length;i++){
            if(entryLbls[i].getText().equals(labelText)){
                return i;
            }
        }
        return -1;
    }

    private void fillTextBoxes() {
        List<String> clientInfo = clientData;
        int i;
        for (i = 0; i < requiredEntryNames.length + optionalEntryNames.length; i++) {
            entryTxtFields[i].setText(clientInfo.get(i));
        }
        if (otherTableEntries.length > 0) {
            for (; (i-(requiredEntryNames.length+optionalEntryNames.length)) < otherTableEntries.length; i++) {
                entryTxtFields[i].setText(clientInfo.get(i));
            }
        }
    }

    private boolean checkLado(int _ladoIndex) {
        if (entryTxtFields[_ladoIndex].getText().equals("Derecho")) {
            entryTxtFields[_ladoIndex].setText("Piedad");
            return true;
        } else if (entryTxtFields[_ladoIndex].getText().equals("Izquierdo")) {
            entryTxtFields[_ladoIndex].setText("Buen_Pastor");
            return true;
        } else {
            return false;
        }
    }

    private boolean checkNicho(String _nicho) {
            return _nicho.matches("[A-M][0-9]{1,2}") || _nicho.matches("[N][0-9]{1,2}[-][1-4][P]") || _nicho.matches("[A-N][0-9]{1,2}[-][1-4]");
    }

    private boolean checkDate(String _date){
        return _date.matches("[0-9]{4}[-](([0][0-9])|([1]?[0-2]))[-](([0-2][0-9])|([3][0-1]))");
    }

    private boolean checkType(String _type){
        return _type.equals("Imagen") || _type.equals("Normal");
    }

    private void fillClientsCombo() {
        int k;
        if (currentTable.equals("Pagos"))
            k=4;
        else
            k=2;
        for (int i = k;i<clientData.size();i++) {
            cmbClients.getItems().add(clientData.get(i));
        }
        for(int j = 0;j<cmbClients.getItems().size();j++){
            if (cmbClients.getItems().get(j).toString().matches(".*\\*$")) {
                cmbClients.getSelectionModel().select(j);
                break;
            }
        }
        Label cmbLabel = new Label("Clientes");
        cmbVbox.getChildren().add(cmbLabel);
        cmbVbox.getChildren().add(cmbClients);
    }

    Scene makeScene() {
        if (currentTable.equals("Clientes") || currentTable.equals("Gastos") || currentTable.equals("Ingresos")) {
            generateEntryTxtFields();
            setLabelText();
            positionEntries();
            generateButtons();
            fillTextBoxes();
            positionScroll();
        }
        else if (currentTable.equals("Pagos") || currentTable.equals("Documentos")) {
            fillClientsCombo();
            generateEntryTxtFields();
            setLabelText();
            positionEntries();
            generateButtons();
            fillTextBoxes();
            positionScroll();
        }
        return new Scene(borderPane, 1280, 700);
    }
}
