package sample;

import Reports.JavaCallJasperReport;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import net.sf.jasperreports.components.map.Item;
import net.sf.jasperreports.engine.JRException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Controller extends Application {
    private static ConexionMySQL objConexion;
    private Alert resultado;
    private Connection conexion;
    private ObservableList<ObservableList> data;
    private Scene scene;
    private int number;
    private String[] numb = new String[196];
    private String[] t = new String[200];
    private String nombre;
    private String id_obt, ladoobt, estado;
    static String currentTable;
    private String currentId, myId;
    private String tableinTab, status, type;
    static File doc;
    static List<String> clientData;
    static String docPath = null;
    private final Button[] button = new Button[200];
    static boolean piedad = false;
    static String clickedNicho;
    private static String btext;

    @FXML
    TableView tabledata;
    @FXML
    Tab tabpiedad, tabbuen_pastor, tabDisponibilidad, tabTabla, tabForm;
    @FXML
    TextField txtUser;
    @FXML
    private TextField txtPass;
    @FXML
    BorderPane borderMain;
    @FXML
    Button btnClientes, btnpiedad, btnPastor, btnFinanzas, btnDocumentos, btnAdd, btnMod, btnDel, btnImp;
    @FXML
    Button btnDispP, btnDispBP;
    @FXML
    GridPane gridDisp, gridDisp2;
    @FXML
    private TabPane tabPaneM, tabPaneD;
    @FXML
    private Label tableLabel;
    @FXML
    private TextField txtNombre, txtAP, txtAM, txtCalle, txtNumero, txtColonia, txtCiudad, txtCP, txtTelefono, txtPrecio, txtNombreB1, txtP1, txtNombreB2, txtP2, txtNombreD, txtAPD, txtAMD;
    @FXML
    ComboBox cmbFormaPago, cmbLado, cmbNicho;
    @FXML
    private CheckBox checkDepositado;
    private String ladoCliente;
    private String nullString = null;
    private boolean modify=false;

    @FXML
    private DatePicker dateApertura, dateLiquidacion;

    @Override
    public void start(Stage primaryStage) {
        Stage stage = primaryStage;

    }

    // La utlilizamos para pasar del login a la vista principal.
    public void login(ActionEvent ev) {
        String estado = conectarSQL();
        System.out.println(estado);
        Parent root = null;
        clientData = FXCollections.observableArrayList();
        // Checha que no este vacio ningun campo.
        if (txtUser.getText() != null && txtPass.getText() != null) {
            // Compara el usuario y la password con las credenciales en la base de datos.
            if (userCheck(txtUser.getText(), txtPass.getText())) {
                Node source = (Node) ev.getSource();

                switchScene("sample.fxml", source);

            } else {
                resultado = new Alert(Alert.AlertType.ERROR, "Nombre de usuario o contraseña equivocada.");
                resultado.show();
            }
        }
    }

    // La utilizamos para cambiar de escena.
    private void switchScene(String sceneName, Node source) {
        Parent parent = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(sceneName));
            parent = loader.load();
            scene = new Scene(parent, 1280, 700);
            ((Stage) source.getScene().getWindow()).setScene(scene);
            scene.getWindow().centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Aqui establecemos la conexion a la base de datos. Utilizando la clase de ConexionMySQL
    private String conectarSQL() {
        String estado = null;
        /* Guardamos el objeto de la conexion como la conexion en si porque
         * mas adelante necesitamos ambos. */
//        objConexion = new ConexionMySQL("u5818148_carlos", "=jkst[RHJunx", "u5818148_criptas", "62.210.247.90", 3306);
        objConexion = new ConexionMySQL("root", "devpass9", "criptas", "localhost", 3306);
        try {
            estado = objConexion.conectar();
            conexion = objConexion.getConexion();
        } catch (ClassNotFoundException e) {
            resultado = new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage());
            resultado.show();
        }
        return estado;
    }

    // Esta funcion compara las credenciales insertadas por el usuario con las que estan en la base de datos.
    private boolean userCheck(String recUser, String recPass) {
        // Declaramos un conjunto de resultados, una query preparado y el query en si.
        ResultSet userRS;
        PreparedStatement loginPS;
        String loginQuery = "Select Nombre, Usuario, Contraseña from Usuarios Where usuario=? and contraseña=?";
        try {
            // Preparamos la consulta
            loginPS = conexion.prepareStatement(loginQuery);
            // Fijamos valores en la consulta con los valores que inserto el usuario.
            loginPS.setString(1, recUser);
            loginPS.setString(2, recPass);
            // Ejecutamos consulta
            userRS = loginPS.executeQuery();
            /* Si lo que ingreso el usuario es correcto se regresara un valor y
            por lo tanto se le fijara un valor a la variable nombre */
            /* De no estar correcto lo que inserto el usuario, la variable
             * nombre se mantendra null */
            while (userRS.next()) {
                nombre = userRS.getString(1);
            }
            /* Utilizamos el valor de nombre para saber si
            lo que inserto el usuario fue correcto */
            return nombre != null;
        } catch (SQLException e) {
            resultado = new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage());
            resultado.show();
            return false;
        }
    }

    //Abre tab de Disponibilidad y botones superiores
    public void clickDisponibilidad() {
        btnAdd.setDisable(true);
        btnMod.setDisable(true);
        btnDel.setDisable(true);
        btnImp.setDisable(true);
        tabPaneM.getSelectionModel().select(tabDisponibilidad);
        btnDispBP.setVisible(true);
        btnDispP.setVisible(true);
    }

    //Funciona el botón filtrar en Finanzas
    public void FiltrarFinanzas() {

    }

    //Cambia de tab según lado del botón
    public void clickDispBP() {
        tabPaneD.getSelectionModel().select(tabbuen_pastor);
        tableLabel.setText("Buen Pastor");
    }

    //Cambia de tab según lado del botón
    public void clickDispP() {
        tabPaneD.getSelectionModel().select(tabpiedad);
        tableLabel.setText("Piedad");
    }
    /* EN LOS SIGUIENTE 6 CLICKS: */
    /* Fijamos que tabla estamos utilizando junto con su respectiva llave primaria y llamamos a un
    procedimiento almacenado en la base de datos dependiendo del boton al que se le dio click. */
    /* Se registra la tabla para usar al momento de insertar y
    la llave primaria para usar  al momento de eliminar. */
    /* Para llamar el procedimiento almacenado, se utiliza una funcion al cual se le manda
     * el nombre del procedimiento almacenado. */


    public void clickClientes() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Clientes";
        currentId = "ID_Cliente";
        tableLabel.setText("Clientes");
        callProcedure("{call showClients()}");
        btnDispBP.setVisible(false);
        btnDispP.setVisible(false);
    }

    public void clickpiedad() {
        btnAdd.setDisable(true);
        btnMod.setDisable(true);
        btnDel.setDisable(true);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "piedad";
        currentId = "ID_Nicho";
        tableLabel.setText("Piedad");
        callProcedure("{call showpiedad()}");
        btnDispBP.setVisible(false);
        btnDispP.setVisible(false);
        piedad = true;
    }

    public void clickBuenPastor() {
        btnAdd.setDisable(true);
        btnMod.setDisable(true);
        btnDel.setDisable(true);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "buen_pastor";
        currentId = "ID_Nicho";
        tableLabel.setText("Buen Pastor");
        callProcedure("{call showBuenPastor()}");
        btnDispBP.setVisible(false);
        btnDispP.setVisible(false);
        piedad = false;
    }

    public void clickPagos() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Pagos";
        currentId = "ID_Pago";
        tableLabel.setText("Pagos");
        callProcedure("{call showPagos()}");
        btnDispBP.setVisible(false);
        btnDispP.setVisible(false);
    }

    public void clickDocumentos() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Documentos";
        currentId = "ID_Documentos";
        tableLabel.setText("Documentos");
        callProcedure("{call showDocs()}");
        btnDispBP.setVisible(false);
        btnDispP.setVisible(false);
    }

    public void clickGastos() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Gastos";
        tableLabel.setText("Gastos");
        callProcedure("{call showGastos()}");
        btnDispBP.setVisible(false);
        btnDispP.setVisible(false);
    }

    public void clickIngresos() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Ingresos";
        tableLabel.setText("Ingresos");
        callProcedure("{call showIngresos()}");
        btnDispBP.setVisible(false);
        btnDispP.setVisible(false);
    }

    public void clickFinanzas() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Ingresos";
        tableLabel.setText("Finanzas");
        callProcedure("{call showIngresos()}");
        btnDispBP.setVisible(false);
        btnDispP.setVisible(false);
    }

    // Esta es la funcion que llama el procedimiento almacenado.
    private void callProcedure(String _procedure) {
        try {
            // se crea y se llena una variable para guardar el resultado del procedimiento.
            CallableStatement showClients = objConexion.getConexion().prepareCall(_procedure);
            // Aseguramos que si se regreso algo nuestra consulta.
            if (showClients.execute()) {
                // Si asi es guardamos los resultados y cargamos la tabla utilizandolos.
                ResultSet tableRS = showClients.getResultSet();
                loadTable(tableRS);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* Cargamos la tabla con su respectivas columnas
     * y registros dependiendo del boton seleccionado. */
    private void loadTable(ResultSet _tableRS) {
        // Aclaramos toda la tabla en caso de uso previo.
        tabledata.getItems().clear();
        tabledata.getColumns().clear();
        // Inicializamos la lista en donde se guardaron los registros.
        data = FXCollections.observableArrayList();
        // Agregamos columnas.
        addCol(_tableRS);
        // Agregamos Registros a una lista.
        addList(_tableRS);

        tabledata.setRowFactory(tv -> {
            TableRow<Item> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    String selection = tabledata.getSelectionModel().getSelectedItems().get(0).toString();
                    selection = selection.replace('[', ' ').trim();
                    String splitSelection[] = selection.split(",");
                    clickedNicho = splitSelection[0];
                    Node source = (Node) event.getSource();
                    switchScene("details.fxml", source);
                }
            });
            return row ;
        });

        // Agregamos lista de registros a nuestra tabla.
        tabledata.setColumnResizePolicy(new Callback<TableView.ResizeFeatures, Boolean>() {
            @Override
            public Boolean call(TableView.ResizeFeatures p) {
                return true;
            }
        });
        tabledata.setItems(data);
    }

    // Aqui es donde agregamos las columnas en la tabla dependiendo del boton al que se le dio click.
    private void addCol(ResultSet tableRS) {
        try {
            // Recibe un conjunto de resultados de alguna consultado previamente ejecutada.
            // El for corre por cada columna que tiene el conjunto de resultados recibido.
            for (int i = 0; i < tableRS.getMetaData().getColumnCount(); i++) {
                // La i nos sirve para obtener los valores dentro del conjunto de resultados.
                /* Al momento de Fijar las columnas dentro de la tabla nos pide que el indicador sea
                 * final por lo cual creamos a j */
                final int j = i;
                // Creamos nueva columna
                TableColumn col = new TableColumn();
                // Fijamos el texto utilizando el conjunto de resultados.
                col.setText(tableRS.getMetaData().getColumnName(i + 1));
                // Cambiamos la anchura por estetica.

                // Esto nos permite manipular celdas dentro de la columna que insertamos.
                col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> new SimpleStringProperty(param.getValue().get(j).toString()));
                // Finalmente agregamos nuestra columna a nuestra tabla.
                tabledata.getColumns().addAll(col);
            }
        } catch (SQLException e) {
            resultado = new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
            resultado.show();
        }
    }

    // Esta funcion almacena los registros dentro de la base de datos en una lista.
    private void addList(ResultSet tableRS) {
        try {
            // Utilizamos el conjunto de resultados que recibimos.
            while (tableRS.next()) {
                /* Se crea una lista para poder juntar todas las columnas
                 * de una fila en un solo registro. */
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= tableRS.getMetaData().getColumnCount(); i++) {
                    if (tableRS.getString(i) != null) {
                        row.add(tableRS.getString(i));
                    } else {
                        row.add("");
                    }
                }
                // Agregamos el registro ya compuesto a nuestro arreglo de registros.
                data.add(row);
            }
        } catch (SQLException e) {
            resultado = new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
            resultado.show();
        }
    }

    // Revisamos si esta seleccionado algun dato en la tabla.
    private boolean isSelected() {
        return tabledata.getSelectionModel().getSelectedIndex() != -1;
    }

    // Borramos un registro seleccionado.
    public void deleteRegister() throws SQLException, IOException {
        /* Revisamos si se tiene seleccionado un registro. De
        no sera asi se le pide al usuario que seleccione uno. */
        if (isSelected()) {
            // Se le pide confirmacion para borrar el registro.
            if (confirmDelete()) {
                if (currentTable.equals("Documentos")) {
                    int id = getID();
                    String ruta = null;
                    ResultSet rutaRs = objConexion.consultar("SELECT Ruta FROM Documentos WHERE ID_Documentos=" + id);
                    while (rutaRs.next()) {
                        ruta = rutaRs.getString("Ruta");
                    }
                    Files.deleteIfExists(Paths.get(ruta));
                }
                // Se ejecuta consulta para borrar.
                String queryRes = objConexion.insertModDel("DELETE FROM " + currentTable + " WHERE " + currentId + "=" + getID());
                resultado = new Alert(Alert.AlertType.INFORMATION, queryRes);
                resultado.show();
                switch (currentTable) {
                    case "Clientes": {
                        clickClientes();
                        break;
                    }
                    case "Pagos": {
                        clickPagos();
                        break;
                    }
                    case "Documentos": {
                        clickDocumentos();
                        break;
                    }
                    case "Gastos": {
                        clickGastos();
                        break;
                    }
                    case "Ingresos": {
                        clickIngresos();
                        break;
                    }
                }
            }
        } else {
            resultado = new Alert(Alert.AlertType.INFORMATION, "Eliga el registro a eliminar.");
            resultado.show();
        }
    }

    // Funcion para confirmar que se desea borrar un registro.
    private boolean confirmDelete() {
        // Se muestra alerta pidiendo confirmacion.
        resultado = new Alert(Alert.AlertType.CONFIRMATION, "Esta seguro que quiere eliminar el registro seleccionado?");
        resultado.showAndWait();
        // Se registra respuesta y se envia si fue falso o verdadero.
        String alertAnswer = resultado.getResult().getText();
        return alertAnswer.equals("OK");
    }

    // Se obtiene el identificador de cierto registro para poder borrar.
    private int getID() {
        String selection = tabledata.getSelectionModel().getSelectedItems().get(0).toString();
        selection = selection.replace('[', ' ').trim();
        String splitSelection[] = selection.split(",");
        return Integer.parseInt(splitSelection[0]);
    }

    // Funcion para agregar un registro.
    public void addRegister(ActionEvent ev) {
        // Revisa que este seleccionada una tabla para insertar en ella.
        if (!currentTable.isEmpty()) {
            /* Se crean variables para saber los datos necesarios para insertar.
             * Uno para campos obligatorios, otro para los opcionales y aun otro
             * para los nombres que se le mostrara al usuario. */
            String[] optionalEntries;
            String[] requiredEntries, otherTableEntries;
            // Revisamos que tabla esta seleccionada.
            switch (currentTable) {
                case "Clientes": {
//                    // Se fijan campos a insertar y sus nombres a mostrar.
//                    optionalEntries = new String[]{"Beneficicario 2", "Parentesco de Beneficiario 2", "Limite de Credito"};
//                    requiredEntries = new String[]{"Nombre", "Apellido Paterno", "Apellido Materno", "Nicho", "Lado", "Saldo", "Dirección", "Telefono", "Fecha de Inscripción", "Benficiario 1", "Parentesco de Beneficiario 1"};
//                    otherTableEntries = new String[]{"Nombre del Difunto", "Apellido Paterno del Difunto", "Apellido Materno del Difunto"};
//                    // Se crea una pantalla para insertar y se muestra dicha pantalla.
//                    AddScreen clienteScreen = new AddScreen(17, optionalEntries, requiredEntries, currentTable, objConexion, otherTableEntries);
//                    Node source = (Node) ev.getSource();
//                    ((Stage) source.getScene().getWindow()).setScene(clienteScreen.makeScene());
                    ObservableList formas = FXCollections.observableArrayList(
                            "a 4 meses", "a 20 meses", "a Contado");
                    ObservableList lados = FXCollections.observableArrayList(
                            "Buen Pastor", "Piedad");
                    btnAdd.setDisable(true);
                    btnMod.setDisable(true);
                    btnDel.setDisable(true);
                    btnImp.setDisable(true);
                    cmbFormaPago.setItems(formas);
                    if(!cmbLado.getItems().equals(lados)){
                        cmbLado.setItems(lados);
                    }
                    tabPaneM.getSelectionModel().select(tabForm);
                    modify=false;
                    break;
                }
                case "Pagos": {
                    Node source = (Node) ev.getSource();
                    switchScene("clientPicker.fxml", source);
                    break;
                }
                case "Documentos": {
                    // Se utiliza una funcion declarada mas adelante para obtener el cliente que va a pagar.
                    /* Se le muestra una pantalla al usuario para que elija el documento que quiere guardar.
                     * La pantalla solo permite elegir archivos pdf o imagenes. */
                    FileChooser docChoose = new FileChooser();
                    Node source = (Node) ev.getSource();
                    docChoose.setTitle("Eliga el documento a insertar.");
                    docChoose.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
                    docChoose.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen", "*.jpg", "*.jpeg", "*.png"));
                    /* Se obtiene el archivo elegido y se guarda una copia en la carpeta del proyecto.
                     * El archivo se guarda con el nombre del cliente, el nicho y la fecha y hora para
                     * nombre iguales. */
                    doc = docChoose.showOpenDialog(source.getScene().getWindow());
                    // Se revisa que se eligio un cliente.
                    // Se fijan campos a insertar y sus nombres a mostrar.
                    switchScene("clientPicker.fxml", source);
                    break;
                }
                case "Gastos": {
                    // Se fijan campos a insertar y sus nombres a mostrar.
                    optionalEntries = new String[]{};
                    requiredEntries = new String[]{"Descripcion", "Monto", "Fecha"};
                    otherTableEntries = new String[]{};
                    // Se crea una pantalla para insertar y se muestra dicha pantalla.
                    AddScreen pagoScreen = new AddScreen(3, optionalEntries, requiredEntries, currentTable, objConexion, otherTableEntries);
                    Node source = (Node) ev.getSource();
                    ((Stage) source.getScene().getWindow()).setScene(pagoScreen.makeScene());
                    break;
                }
                case "Ingresos": {
                    // Se fijan campos a insertar y sus nombres a mostrar.
                    optionalEntries = new String[]{};
                    requiredEntries = new String[]{"Descripcion", "Monto", "Fecha"};
                    otherTableEntries = new String[]{};
                    // Se crea una pantalla para insertar y se muestra dicha pantalla.
                    AddScreen pagoScreen = new AddScreen(3, optionalEntries, requiredEntries, currentTable, objConexion, otherTableEntries);
                    Node source = (Node) ev.getSource();
                    ((Stage) source.getScene().getWindow()).setScene(pagoScreen.makeScene());
                    break;
                }
            }
        }
    }

    // Esta funcion genera una ventana para que el usuario seleccione un cliente.
    private static List<String> getClientInfoCombo() {
        // Obtenemos todos los clientes en la base de datos.
        ResultSet clients = objConexion.consultar("SELECT ID_Cliente, Nombre, ID_Nicho FROM Clientes ORDER BY Nombre");
        // Se hace una lista para almacenar a las opciones posibles.
        List<String> comboChoices = new ArrayList<>();
        // Este variable almacena la informacion del cliente seleccionado.
        clientData.clear();
        // Este variable nos ayudara a fijar un cliente predeterminado.
        String firstChoice = null;
        int i = 0;
        try {
            // Recorre todos los clientes.
            while (clients.next()) {
                // Obtenemos el ID, nombre y nicho del cliente.
                String idClient = clients.getString("ID_Cliente");
                String name = clients.getString("Nombre");
                String idNicho = clients.getString("ID_Nicho");
                // Si es el primer cliente, se fija como predeterminado.
                if (i == 0) {
                    firstChoice = idClient + ": " + name + ": " + idNicho;
                    i++;
                }
                // Agregamos cada cliente a un ComboBox.
                comboChoices.add(idClient + ": " + name + ": " + idNicho);
            }
            // Se muestra pantalla para eligir cliente.
            ChoiceDialog chooseClient = new ChoiceDialog<>(firstChoice, comboChoices);
            chooseClient.setTitle("Clientes");
            chooseClient.setHeaderText("Eliga un cliente: ");
            chooseClient.setContentText("Clientes: ");
            // Se guarda cliente elegido.
            Optional response = chooseClient.showAndWait();
            if (response.isPresent()) {
                // Se separa la seleccion para poder obtener cada dato individual.
                String selection = response.get().toString();
                String[] selectionSeperated = selection.split(":");
                for (int j = 0; j < selectionSeperated.length; j++) {
                    selectionSeperated[j] = selectionSeperated[j].trim();
                    clientData.add(selectionSeperated[j]);
                }
                // Se utiliza una consulta para saber que pared esta utilizando el cliente.
                ResultSet lado = objConexion.consultar("SELECT Lado FROM Clientes WHERE ID_Cliente=" + clientData.get(0));
                ResultSet nichoType = null;
                while (lado.next()) {
                    String table;
                    if (lado.getString("Lado").equals("Derecho")) {
                        table = "piedad";
                    } else {
                        table = "buen_pastor";
                    }
                    nichoType = objConexion.consultar("SELECT Tipo_Nicho FROM " + table + " WHERE ID_Cliente=" + clientData.get(0));
                }
                // Se utiliza una consulta para saber que tipo de nicho esta utilizando el cliente.
                if (nichoType != null) {
                    while (nichoType.next()) {
                        clientData.add(nichoType.getString("Tipo_Nicho"));
                    }
                }
                // Se regresa una lista con toda la informacion del cliente seleccionado.
                return clientData;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Te manda a la fila en la tabla cuando das a un botón de disponibilidad
    private void checkSpace(ActionEvent ae, int n) {
        //Obtiene informacion del boton
        Object o = ae.getSource();
        Button b = null;
        if (o instanceof Button) b = (Button) o;
        if (b != null) {
            btext = b.getText();
            //TabPaneM.getSelectionModel().select(tabTabla);
            int x = 0;
            for (int i = 0; i < numb.length; i++) {
                if (numb[i].equals(btext)) {

                    if (tabPaneD.getSelectionModel().isSelected(1)) {
                        clickpiedad();
                        tabledata.getSelectionModel().select(i);
                        tabPaneM.getSelectionModel().select(tabTabla);
                        tabledata.scrollTo(i);
                    }

                    x = (i * 4);

                    if (tabPaneD.getSelectionModel().isSelected(0)) {
                        clickBuenPastor();
                        tabledata.getSelectionModel().select(x);
                        tabPaneM.getSelectionModel().select(tabTabla);
                        tabledata.scrollTo(x);
                    }
                }
            }


        }
    }

    //Crea los botones de piedad en el gridpane
    public void loadStatus() {
        String[] statusar = new String[200];
        String[] t = new String[200];
        String[] typear = new String[200];
        int cont = 0;
        //Espacio entre los botones
        gridDisp.setPadding(new Insets(5));
        gridDisp.setHgap(5);
        gridDisp.setVgap(5);
        //For para crear el texto de los botones
        for (char alphabet = 'A'; alphabet <= 'N'; alphabet++) {
            for (int i = 1; i <= 14; i++) {
                //en este arreglo se guarda el nombre de los botones
                t[cont] = alphabet + Integer.toString(i);
                //Obtiene el estado de ese botón
                statusar[cont] = getStatus(t[cont]);
                //Obtiene el tipo de ese botón
                typear[cont] = getType(t[cont]);
                cont++;
            }
        }
        //For para crear los botones
        for (int r = 0; r < 14; r++) {
            for (int c = 0; c < 14; c++) {
                //Calcula el numero del botón
                number = 14 * r + c;
                //Crea el botón
                button[number] = new Button(t[number]);
                numb[number] = t[number];
                switch (typear[number]) {
                    //Si es imagen el tipo le añade el estilo buttonbold
                    case "Imagen":
                        button[number].getStyleClass().add("buttonbold");
                        break;

                }

                switch (statusar[number] != null ? statusar[number] : "Libre") {
                    //Si es ocupado el estado usa el estilo buttonfree
                    case "Ocupado":
                        button[number].getStyleClass().add("buttonused");
                        break;
                    //Si es libre el estado usa buttonused
                    case "Libre":
                        button[number].getStyleClass().add("buttonfree");
                        break;
                }
                //define la medida de los botones
                button[number].setPrefSize(50, 50);
                button[number].setOnAction(event -> checkSpace(event, number));
                //añade el botón al grid
                gridDisp.add(button[number], c, r);
                //Accion del botón

            }
        }
    }


    //Crea los botones de buen pastor en el gridpane
    public void loadStatusBP() {

        String[] statusar = new String[200];
        String[] typear = new String[200];
        int cont = 0;
        Button[] button = new Button[200];
        //Espacio entre los botones
        gridDisp2.setPadding(new Insets(5));
        gridDisp2.setHgap(5);
        gridDisp2.setVgap(5);
        //For para crear el texto de los botones
        for (char alphabet = 'A'; alphabet <= 'N'; alphabet++) {
            for (int i = 1; i <= 14; i++) {
                //en este arreglo se guarda el nombre de los botones
                t[cont] = alphabet + Integer.toString(i);
                //Obtiene el estado de ese botón
                statusar[cont] = getStatus(t[cont]);
                //Obtiene el tipo de ese botón
                typear[cont] = getType(t[cont]);
                cont++;
            }
        }
        //For para crear los botones
        for (int r = 0; r < 14; r++) {
            for (int c = 0; c < 14; c++) {
                //Calcula el numero del botón
                number = 14 * r + c;
                //Crea el botón
                button[number] = new Button(t[number]);
                numb[number] = t[number];
                button[number].setOnAction(actionEvent -> {

                });
                switch (typear[number]) {
                    //Si es imagen el tipo le añade el estilo buttonbold
                    case "Imagen":
                        button[number].getStyleClass().add("buttonbold");
                        break;

                }

                switch (statusar[number] != null ? statusar[number] : "Libre") {
                    //Si es ocupado el estado usa el estilo buttonfree
                    case "Ocupado":
                        button[number].getStyleClass().add("buttonused");
                        break;
                    //Si es libre el estado usa buttonused
                    case "Libre":
                        button[number].getStyleClass().add("buttonfree");
                        break;
                }
                //define la medida de los botones
                button[number].setPrefSize(50, 50);
                button[number].setOnAction(event -> checkSpace(event, number));

                //añade el botón al grid
                gridDisp2.add(button[number], c, r);
                //Accion del botón

            }
        }
    }

    //Obtiene el estado del nicho para poder marcarlo ocupado o libre
    private String getStatus(String id) {

        try {
            if (tableinTab.equals("piedad")) {

                ResultSet c = objConexion.consultar("SELECT Estado FROM " + tableinTab + " WHERE ID_Nicho='" + id + "';");
                while (c.next()) {
                    status = c.getString("Estado");


                }
            }
            if (tableinTab.equals("buen_pastor")) {

                List<String> list = new ArrayList<>();
                ResultSet c = objConexion.consultar("SELECT estado FROM buen_pastor where id_nicho LIKE '" + id + "__' group by estado having count(*) >= 4;");
                while (c.next()) {
                    status = c.getString("estado");

                }
                return status;
            }


        } catch (Exception e) {
            resultado = new Alert(Alert.AlertType.ERROR, "No funciona la parte del estado");
            resultado.show();
        }

        return status;
    }

    //Obtiene el tipo de nicho que es para marcarlo diferente
    private String getType(String id) {
        try {
            if (tableinTab.equals("piedad")) {

                ResultSet c = objConexion.consultar("SELECT Tipo_Nicho FROM piedad WHERE ID_Nicho='" + id + "';");
                while (c.next()) {
                    type = c.getString("Tipo_Nicho");
                }
            }
            if (tableinTab.equals("buen_pastor")) {

                ResultSet c = objConexion.consultar("SELECT Tipo_Nicho FROM buen_pastor WHERE ID_Nicho='" + id + "-1';");
                while (c.next()) {
                    type = c.getString("Tipo_Nicho");

                }
            }

        } catch (Exception e) {
            resultado = new Alert(Alert.AlertType.ERROR, "No funciona la parte del tipo");
            resultado.show();
        }
        return type;
    }

    //Checa cual pestaña esta seleccionada y carga los botones de disponiblidad
    public void checkTab() {
        if (tabPaneD.getSelectionModel().getSelectedItem() == tabpiedad) {
            tableinTab = "piedad";
            loadStatus();
        }
        if (tabPaneD.getSelectionModel().getSelectedItem() == tabbuen_pastor) {
            tableinTab = "buen_pastor";
            loadStatusBP();
        }
    }

    //Muestra el reporte segun la tabla cuando le das imprimir
    public void showReport() {
        try {
            System.out.println(currentTable);
            new JavaCallJasperReport(currentTable);
        } catch (JRException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Se usa en modificar para obtener la información de la tabla en los textfields
    private void getClientInfoTable() {
        switch (currentTable) {
            case "Clientes": {
                String queryClients = "SELECT * FROM Clientes WHERE ID_Cliente=" + getID();
                ResultSet clients = objConexion.consultar(queryClients);
                clientData.clear();
                try {
                    while (clients.next()) {
                        txtNombre.setText(clients.getString("Nombre"));
                        txtAP.setText(clients.getString("AP_Paterno"));
                        txtAM.setText(clients.getString("AP_Materno"));
                        id_obt = clients.getString("ID_Nicho");
                        ladoobt = clients.getString("Lado");
                        ObservableList lados = FXCollections.observableArrayList(
                                "Buen Pastor", "Piedad");
                        if (clients.getString("Lado").equals("Izquierdo")) {
                            if(!cmbLado.getItems().equals(lados)){
                                cmbLado.setItems(lados);
                            }
                            cmbLado.setValue("Buen Pastor");
                        } else if (clients.getString("Lado").equals("Derecho")) {
                            if(!cmbLado.getItems().equals(lados)){
                                cmbLado.setItems(lados);
                            }

                            cmbLado.setValue("Piedad");
                        }
                        cmbNicho.setValue(clients.getString("ID_Nicho"));
                        // clientData.add(clients.getString("Saldo"));
                        txtCalle.setText(clients.getString("Calle"));
                        txtNumero.setText(clients.getString("Numero"));
                        txtColonia.setText(clients.getString("Colonia"));
                        txtCiudad.setText(clients.getString("Ciudad"));
                        txtCP.setText(clients.getString("Codigo_Postal"));
                        txtTelefono.setText(clients.getString("Teléfono"));
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                        dateApertura.setValue(LocalDate.parse(clients.getString("Fecha_Apertura"),dateFormatter));
                        if (clients.getString("Fecha_Apertura") != null) {
                            dateApertura.setValue(LocalDate.parse(clients.getString("Fecha_Apertura"), dateFormatter));
                        }
                        if (clients.getString("Fecha_Liquidacion") != null) {
                            dateLiquidacion.setValue(LocalDate.parse(clients.getString("Fecha_Liquidacion"), dateFormatter));
                        }

                        txtNombreB1.setText(clients.getString("Ben1"));
                        txtP1.setText(clients.getString("Ben1_Par"));
                        txtNombreB2.setText(clients.getString("Ben2"));
                        txtP2.setText(clients.getString("Ben2_Par"));
                        cmbFormaPago.setValue(clients.getString("Forma_Pago"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String lado;
                if (ladoobt.equals("Derecho"))
                    lado = "piedad";
                else
                    lado = "buen_pastor";

                String queryDif = "SELECT * FROM " + lado + " WHERE ID_Nicho='" + id_obt + "'";
                ResultSet dif = objConexion.consultar(queryDif);
                try {
                    while (dif.next()) {
                        txtNombreD.setText(dif.getString("Nombre_Dif"));
                        txtAPD.setText(dif.getString("AP_Pat_Dif"));
                        txtAMD.setText(dif.getString("AP_Mat_Dif"));
                        estado = dif.getString("Estado");

                        txtPrecio.setText(dif.getString("Costo"));
                    }
                    if (Objects.equals(estado, "Ocupado")) {
                        checkDepositado.setSelected(true);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "Pagos": {
                String queryPagos = "SELECT * FROM Pagos WHERE ID_Pago=" + getID();
                ResultSet pagos = objConexion.consultar(queryPagos);
                clientData.clear();
                String idClient = null;
                try {
                    while (pagos.next()) {
                        idClient = pagos.getString("ID_Cliente");
                        clientData.add(pagos.getString("Cantidad"));
                        clientData.add(pagos.getString("Fecha"));
                        clientData.add(pagos.getString("ID_Nicho"));
                        clientData.add(pagos.getString("Tipo_Nicho"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String queryClients = "SELECT ID_Cliente, Nombre, AP_Paterno, AP_Materno FROM Clientes";
                ResultSet clients = objConexion.consultar(queryClients);
                try {
                    while (clients.next()) {
                        if (clients.getString("ID_Cliente").equals(idClient)) {
                            clientData.add(clients.getString("ID_Cliente") + ": " +
                                    clients.getString("Nombre") + " " +
                                    clients.getString("AP_Paterno") + " " +
                                    clients.getString("AP_Materno") + "*");
                        } else {
                            clientData.add(clients.getString("ID_Cliente") + ": " +
                                    clients.getString("Nombre") + " " +
                                    clients.getString("AP_Paterno") + " " +
                                    clients.getString("AP_Materno"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "Gastos": {
                String queryGastos = "SELECT * FROM Gastos WHERE ID_Gasto=" + getID();
                ResultSet gastos = objConexion.consultar(queryGastos);
                clientData.clear();
                try {
                    while (gastos.next()) {
                        clientData.add(gastos.getString("Descripción"));
                        clientData.add(gastos.getString("Monto"));
                        clientData.add(gastos.getString("Fecha"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "Ingresos": {
                String queryGastos = "SELECT * FROM Ingresos WHERE ID_Ingreso=" + getID();
                ResultSet gastos = objConexion.consultar(queryGastos);
                clientData.clear();
                try {
                    while (gastos.next()) {
                        clientData.add(gastos.getString("Descripción"));
                        clientData.add(gastos.getString("Monto"));
                        clientData.add(gastos.getString("Fecha"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "Documentos": {
                String queryDocs = "SELECT * FROM Documentos WHERE ID_Documentos=" + getID();
                ResultSet Docs = objConexion.consultar(queryDocs);
                clientData.clear();
                String idClient = null;
                try {
                    while (Docs.next()) {
                        idClient = Docs.getString("ID_Clientes");
                        clientData.add(Docs.getString("Tipo"));
                        clientData.add(Docs.getString("Fecha"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String queryClients = "SELECT ID_Cliente, Nombre, AP_Paterno, AP_Materno FROM Clientes";
                ResultSet clients = objConexion.consultar(queryClients);
                try {
                    while (clients.next()) {
                        if (clients.getString("ID_Cliente").equals(idClient)) {
                            clientData.add(clients.getString("ID_Cliente") + ": " +
                                    clients.getString("Nombre") + " " +
                                    clients.getString("AP_Paterno") + " " +
                                    clients.getString("AP_Materno") + "*");
                        } else {
                            clientData.add(clients.getString("ID_Cliente") + ": " +
                                    clients.getString("Nombre") + " " +
                                    clients.getString("AP_Paterno") + " " +
                                    clients.getString("AP_Materno"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    //Abre ventana de modificar
    public void modify(ActionEvent ev) {
        String[] optionalEntries;
        String[] requiredEntries, otherTableEntries;
        if (isSelected()) {
            switch (currentTable) {
                case "Clientes": {

//                    optionalEntries = new String[]{"Beneficicario 2", "Parentesco de Beneficiario 2", "Limite de Credito"};
//                    requiredEntries = new String[]{"Nombre", "Apellido Paterno", "Apellido Materno", "Nicho", "Lado", "Saldo", "Dirección", "Telefono", "Fecha de Inscripcion", "Benficiario 1", "Parentesco de Beneficiario 1"};
//                    otherTableEntries = new String[]{"Nombre del Difunto", "Apellido Paterno del Difunto", "Apellido Materno del Difunto"};
//                    ModScreen modify = new ModScreen(requiredEntries, optionalEntries, otherTableEntries, 17, currentTable, objConexion, getID() + "");
//                    Node source = (Node) ev.getSource();
//                    ((Stage) source.getScene().getWindow()).setScene(modify.makeScene());
                    modify=true;
                    ObservableList formas = FXCollections.observableArrayList(
                            "a 4 meses", "a 20 meses", "a Contado");

                    btnAdd.setDisable(true);
                    btnMod.setDisable(true);
                    btnDel.setDisable(true);
                    btnImp.setDisable(true);
                    cmbFormaPago.setItems(formas);
                    getClientInfoTable();
                    tabPaneM.getSelectionModel().select(tabForm);
                    break;
                }
                case "Pagos": {
                    getClientInfoTable();
                    optionalEntries = new String[]{};
                    requiredEntries = new String[]{"Cantidad", "Fecha", "Nicho", "Tipo de Nicho"};
                    otherTableEntries = new String[]{};
                    ModScreen modify = new ModScreen(requiredEntries, optionalEntries, otherTableEntries, 4, currentTable, objConexion, getID() + "");
                    Node source = (Node) ev.getSource();
                    ((Stage) source.getScene().getWindow()).setScene(modify.makeScene());
                    break;
                }
                case "Documentos": {
                    getClientInfoTable();
                    optionalEntries = new String[]{};
                    requiredEntries = new String[]{"Tipo", "Fecha"};
                    otherTableEntries = new String[]{};
                    ModScreen modify = new ModScreen(requiredEntries, optionalEntries, otherTableEntries, 2, currentTable, objConexion, getID() + "");
                    Node source = (Node) ev.getSource();
                    ((Stage) source.getScene().getWindow()).setScene(modify.makeScene());
                    break;
                }
                case "Gastos": {
                    getClientInfoTable();
                    optionalEntries = new String[]{};
                    requiredEntries = new String[]{"Descripción", "Monto", "Fecha"};
                    otherTableEntries = new String[]{};
                    ModScreen modify = new ModScreen(requiredEntries, optionalEntries, otherTableEntries, 3, currentTable, objConexion, getID() + "");
                    Node source = (Node) ev.getSource();
                    ((Stage) source.getScene().getWindow()).setScene(modify.makeScene());
                    break;
                }
                case "Ingresos": {
                    getClientInfoTable();
                    optionalEntries = new String[]{};
                    requiredEntries = new String[]{"Descripción", "Monto", "Fecha"};
                    otherTableEntries = new String[]{};
                    ModScreen modify = new ModScreen(requiredEntries, optionalEntries, otherTableEntries, 3, currentTable, objConexion, getID() + "");
                    Node source = (Node) ev.getSource();
                    ((Stage) source.getScene().getWindow()).setScene(modify.makeScene());
                    break;
                }
            }
        } else {
            resultado = new Alert(Alert.AlertType.INFORMATION, "Eliga el registro a modificar.");
            resultado.show();
        }
    }

    //Ventana de acerca de
    public void runAbout() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("about.fxml"));
        Parent parent = loader.load();
        scene = new Scene(parent, 600, 291);
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setScene(scene);
        dialog.showAndWait();

    }

    public void sideSelected() {
        conectarSQL();
       // cmbNicho.getItems().clear();
        ResultSet nichosRS;
        PreparedStatement nichosPS;
        List<String> results = new ArrayList<String>();
        results.clear();

        if (cmbLado.getValue().toString().equals("Buen Pastor")) {
            cmbNicho.setValue("");
            cmbNicho.setDisable(false);
            String nichosQuery = "SELECT ID_Nicho from Buen_Pastor";
            try {
                // Preparamos la consulta
                nichosPS = conexion.prepareStatement(nichosQuery);

                // Ejecutamos consulta
                nichosRS = nichosPS.executeQuery();

                while (nichosRS.next()) {
                    results.add(nichosRS.getString("ID_Nicho"));
                }
                ObservableList nichos = FXCollections.observableArrayList(
                        results);
                cmbNicho.setItems(nichos);
                new AutoCompleteComboBoxListener<>(cmbNicho);
            } catch (SQLException e) {
                resultado = new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage());
                resultado.show();

            }
        }
        if (cmbLado.getValue().toString().equals("Piedad")) {
            cmbNicho.setValue("");
            cmbNicho.setDisable(false);
            String nichosQuery = "SELECT ID_Nicho from Piedad";
            try {
                // Preparamos la consulta
                nichosPS = conexion.prepareStatement(nichosQuery);

                // Ejecutamos consulta
                nichosRS = nichosPS.executeQuery();

                while (nichosRS.next()) {
                    results.add(nichosRS.getString("ID_Nicho"));
                }
                ObservableList nichos = FXCollections.observableArrayList(
                        results);
                cmbNicho.setItems(nichos);
                new AutoCompleteComboBoxListener<>(cmbNicho);
            } catch (SQLException e) {
                resultado = new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage());
                resultado.show();

            }
        }
    }

    //Agregar cliente
    public void addClient() {
        if (!checkObligatories()) {
            addClients();
        }
    }

    public String lastID_Clients() {
        ResultSet ladoRS;
        PreparedStatement ladoPS;
        String loginQuery = "SELECT MAX(ID_Cliente) from clientes";
        try {
            // Preparamos la consulta
            ladoPS = conexion.prepareStatement(loginQuery);

            // Ejecutamos consulta
            ladoRS = ladoPS.executeQuery();

            while (ladoRS.next()) {
                myId = ladoRS.getString(1);
            }
            return myId;
        } catch (SQLException e) {
            resultado = new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage());
            resultado.show();
            return myId;
        }
    }

    private boolean checkObligatories() {
        boolean fieldsBad = false;
        String[] requiredFields = new String[]{txtNombre.getText(), txtAP.getText(), txtAM.getText(),
                txtTelefono.getText(),/*txtPrecio.getText(),*/
                txtNombreB1.getText(), txtP1.getText(),
                txtNombreB2.getText(), txtP2.getText()};


        for (String requiredField : requiredFields) {
            if (requiredField.isEmpty()) {
                resultado = new Alert(Alert.AlertType.ERROR, "Hay un campo obligatorio vacío.");
                resultado.show();
                fieldsBad = true;
                break;
            } else {


                if (cmbFormaPago.getValue().equals("Escoger:")) {
                    resultado = new Alert(Alert.AlertType.ERROR, "No se escogió forma de pago.");
                    resultado.show();
                    fieldsBad = true;
                    break;
                }
                if (cmbNicho.getValue().equals("Escoger:") || cmbNicho.getValue().toString().isEmpty()) {
                    resultado = new Alert(Alert.AlertType.ERROR, "No se escogió nicho.");
                    resultado.show();
                    fieldsBad = true;
                    break;
                }
                if (cmbLado.getValue().equals("Escoger:")) {
                    resultado = new Alert(Alert.AlertType.ERROR, "No se escogió lado.");
                    resultado.show();
                    fieldsBad = true;
                    break;
                }
                fieldsBad = false;
            }
        }


        if (!txtNumero.getText().isEmpty()) {
            if (!txtNumero.getText().matches("[0-9]+")) {
                resultado = new Alert(Alert.AlertType.ERROR, "En el campo número hay caracteres que no son numeros.");
                resultado.show();
                fieldsBad = true;
            }
        }

        if (!txtPrecio.getText().isEmpty()) {
            if (!txtPrecio.getText().matches("^[0-9]+([,.][0-9]+)?$")) {
                resultado = new Alert(Alert.AlertType.ERROR, "En el campo precio hay caracteres que no son numeros.");
                resultado.show();
                fieldsBad = true;
            }
        }
        if (!txtCP.getText().isEmpty()) {
            if (!txtCP.getText().matches("[0-9]+")) {
                resultado = new Alert(Alert.AlertType.ERROR, "En el campo Código Postal hay caracteres que no son numeros.");
                resultado.show();
                fieldsBad = true;
            }

        }
        if (dateApertura.getValue() == null) {
            resultado = new Alert(Alert.AlertType.ERROR, "La fecha apertura esta vacia.");
            resultado.show();
            fieldsBad = true;
        }
        return fieldsBad;

    }

    private void addClients() {
        //Agregar
        String estado = conectarSQL();
        System.out.println(estado);
        String clientQuery;
        PreparedStatement clientPS;
        if(modify){
            clientQuery = "UPDATE clientes SET ID_Nicho=IFNULL(?,ID_Nicho),Lado=IFNULL(?,Lado),AP_Paterno=IFNULL(?,AP_Paterno),AP_Materno=IFNULL(?,AP_Materno),Nombre=IFNULL(?,Nombre),Fecha_Apertura=IFNULL(?,Fecha_Apertura),Fecha_Liquidacion=IFNULL(?,Fecha_Liquidacion),Calle=IFNULL(?,Calle),Numero=IFNULL(?,Numero),Colonia=IFNULL(?,Colonia), Ciudad=IFNULL(?,Ciudad),Codigo_Postal=IFNULL(?,Codigo_Postal),Teléfono=IFNULL(?,Teléfono),Saldo=IFNULL(?,Saldo),Ben1=IFNULL(?,Ben1),Ben1_Par=IFNULL(?,Ben1_Par),Ben2=IFNULL(?,Ben2),Ben2_Par=IFNULL(?,Ben2_Par),Forma_Pago=IFNULL(?,Forma_Pago) Where ID_Cliente=?";

        }else{
            clientQuery = "INSERT INTO clientes (ID_Nicho,Lado,AP_Paterno,AP_Materno,Nombre,Fecha_Apertura,Fecha_Liquidacion,Calle,Numero,Colonia, Ciudad,Codigo_Postal,Teléfono,Saldo,Ben1,Ben1_Par,Ben2,Ben2_Par,Forma_Pago,ID_Cliente) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        }

        try {
            // Preparamos la consulta
            clientPS = conexion.prepareStatement(clientQuery);
            // Fijamos valores en la consulta con los valores que inserto el usuario.
            clientPS.setString(1, cmbNicho.getValue().toString());
            if (cmbLado.getValue().toString().equals("Buen Pastor")) {
                clientPS.setString(2, "Izquierdo");
            } else if (cmbLado.getValue().toString().equals("Piedad")) {
                clientPS.setString(2, "Derecho");
            }
            clientPS.setString(3, txtAP.getText());
            clientPS.setString(4, txtAM.getText());
            clientPS.setString(5, txtNombre.getText());
            clientPS.setString(6, dateApertura.getValue().toString());
            if (dateLiquidacion.getValue() == null) {
                clientPS.setString(7, nullString);
            } else {
                clientPS.setString(7, dateLiquidacion.getValue().toString());
            }

            clientPS.setString(8, txtCalle.getText());
            if (txtNumero.getText().isEmpty()) {

                clientPS.setString(9, nullString);
            } else {
                clientPS.setString(9, txtNumero.getText());
            }
            clientPS.setString(10, txtColonia.getText());
            clientPS.setString(11, txtCiudad.getText());
            if (txtCP.getText().isEmpty()) {
                clientPS.setString(12, nullString);
            } else {
                clientPS.setString(12, txtCP.getText());
            }
            clientPS.setString(13, txtTelefono.getText());
            if (txtPrecio.getText().isEmpty()) {
                clientPS.setString(14, nullString);
            } else {
                clientPS.setString(14, txtPrecio.getText());
            }
            clientPS.setString(15, txtNombreB1.getText());
            clientPS.setString(16, txtP1.getText());
            clientPS.setString(17, txtNombreB2.getText());
            clientPS.setString(18, txtP2.getText());
            clientPS.setString(19, cmbFormaPago.getValue().toString());
            int lastId = (Integer.parseInt(lastID_Clients())) + 1;
           if(modify){
               clientPS.setString(20, getID()+"");
           }else{
               clientPS.setString(20, lastId + "");
           }

            // Ejecutamos consulta
            clientPS.executeQuery();
            //Manda datos a otra tabla

            ResultSet nichoRS;
            PreparedStatement nichoPS;

            if (cmbLado.getValue().toString().equals("Buen Pastor")) {
                ladoCliente = "buen_pastor";
            } else if (cmbLado.getValue().toString().equals("Piedad")) {
                ladoCliente = "piedad";
            }
            String nichoQuery = "UPDATE " + ladoCliente + " SET ID_Cliente=?,Costo=IFNULL(?,Costo),Estado=?,AP_Pat_Dif=IFNULL(?,AP_Pat_Dif),AP_Mat_Dif=IFNULL(?,AP_Mat_Dif),Nombre_Dif=IFNULL(?,Nombre_Dif) Where ID_Nicho=?";

            try {
                // Preparamos la consulta
                nichoPS = conexion.prepareStatement(nichoQuery);
                // Fijamos valores en la consulta con los valores que inserto el usuario.
                nichoPS.setString(1, lastID_Clients());
                if (txtPrecio.getText().isEmpty()) {
                    nichoPS.setString(2, nullString);
                } else {
                    nichoPS.setString(2, txtPrecio.getText());
                }
                if (checkDepositado.isSelected()) {
                    nichoPS.setString(3, "Ocupado");
                } else {
                    nichoPS.setString(3, nullString);
                }
                if (txtAPD.getText().isEmpty()) {
                    nichoPS.setString(4, nullString);
                } else {
                    nichoPS.setString(4, txtAPD.getText());
                }
                if (txtAMD.getText().isEmpty()) {
                    nichoPS.setString(5, nullString);
                } else {
                    nichoPS.setString(5, txtAMD.getText());
                }
                if (txtNombreD.getText().isEmpty()) {
                    nichoPS.setString(6, nullString);
                } else {
                    nichoPS.setString(6, txtNombreD.getText());
                }
                if (cmbNicho.getValue().toString().isEmpty()) {
                    nichoPS.setString(7, nullString);
                } else {
                    nichoPS.setString(7, cmbNicho.getValue().toString());
                }


                // Ejecutamos consulta
                nichoPS.executeQuery();
            } catch (SQLException e) {
                resultado = new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage());
                resultado.show();

            }
            resultado = new Alert(Alert.AlertType.INFORMATION, "Se agregó satisfactoriamente. ");
            clearForm();
            resultado.show();
            //Limpiar


        } catch (SQLException e) {
            resultado = new Alert(Alert.AlertType.ERROR, "Error en la conexion: " + e.getMessage());
            resultado.show();

        }
    }

    //Cierra el formulario de agregar/modificar cliente
    public void Cancel() {
        clickClientes();
        clearForm();
    }

    private void clearForm() {
     //   cmbNicho.getSelectionModel().clearSelection();
      //  cmbLado.getSelectionModel().clearSelection();
        txtAP.clear();
        txtAM.clear();
        txtNombre.clear();
        dateApertura.setValue(null);
        dateLiquidacion.setValue(null);
        txtCalle.clear();
        txtNumero.clear();
        txtColonia.clear();
        txtCiudad.clear();
        txtCP.clear();
        txtTelefono.clear();
        txtPrecio.clear();
        txtNombreB1.clear();
        txtP1.clear();
        txtNombreB2.clear();
        txtP2.clear();
        cmbFormaPago.getSelectionModel().clearSelection();
    }
}

