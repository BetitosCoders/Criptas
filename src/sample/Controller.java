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
import javafx.util.Callback;
import net.sf.jasperreports.engine.JRException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class Controller extends Application {
    private static ConexionMySQL objConexion;
    private Alert resultado;
    private Connection conexion;
    private ObservableList<ObservableList> data;
    private Stage stage;
    private String nombre;
    private String currentTable, currentId;
    private String[] tablesInUse;
    private String tableinTab,status,type;
    static List<String> clientData;
    static String docPath = null;
    @FXML TableView tabledata;
    @FXML Tab tabPiedad,tabBuen_Pastor,tabDisponibilidad,tabTabla;
    @FXML TextField txtUser;
    @FXML private TextField txtPass;
    @FXML BorderPane borderMain;
    @FXML Button btnClientes,btnPiedad,btnPastor,btnPagos,btnDocumentos;
    @FXML Button A1,A2;
    @FXML
    GridPane gridDisp,gridDisp2;
    @FXML
    private TabPane TabPaneM,TabPaneD;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
    }

    // La utlilizamos para pasar del login a la vista principal.
    public void login(ActionEvent ev){
        String estado = conectarSQL();
        System.out.println(estado);
        Parent root =null;
        // Checha que no este vacio ningun campo.
        if (txtUser.getText() != null && txtPass.getText() != null) {
            // Compara el usuario y la password con las credenciales en la base de datos.
            if (userCheck(txtUser.getText(), txtPass.getText())) {
                    Node source = (Node) ev.getSource();
                    switchScene("sample.fxml", source);
            }
            else {
                resultado = new Alert(Alert.AlertType.ERROR, "Nombre de usuario o contraseña equivoacada.");
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
            ((Stage)source.getScene().getWindow()).setScene(new Scene(parent, 1280, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Aqui establecemos la conexion a la base de datos. Utilizando la clase de ConexionMySQL
    private String conectarSQL() {
        String estado = null;
        /* Guardamos el objeto de la conexion como la conexion en si porque
        * mas adelante necesitamos ambos. */
        objConexion = new ConexionMySQL("root", "devpass9", "Criptas", "localhost", 3306);
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


    /* EN LOS SIGUIENTE 6 CLICKS: */
    /* Fijamos que tabla estamos utilizando junto con su respectiva llave primaria y llamamos a un
    procedimiento almacenado en la base de datos dependiendo del boton al que se le dio click. */
    /* Se registra la tabla para usar al momento de insertar y
    la llave primaria para usar  al momento de eliminar. */
    /* Para llamar el procedimiento almacenado, se utiliza una funcion al cual se le manda
    * el nombre del procedimiento almacenado. */
    public void clickClientes(ActionEvent ev){
        currentTable = "Clientes";
        currentId = "ID_Cliente";
        callProcedure("{call showClients()}");
    }

    public void clickPiedad(ActionEvent ev){
        currentTable = "piedad";
        currentId = "ID_Nicho";
        callProcedure("{call showPiedad()}");
    }

    public void clickBuenPastor(ActionEvent ev){
        currentTable = "buen_pastor";
        currentId = "ID_Nicho";
        callProcedure("{call showBuenPastor()}");
    }

    public void clickPagos(ActionEvent ev){
        currentTable = "Pagos";
        currentId = "ID_Pago";
        callProcedure("{call showPagos()}");
    }

    public void clickDocumentos(ActionEvent ev){
        currentTable = "Documentos";
        callProcedure("{call showDocs()}");
    }

    public void clickGastos(ActionEvent ev){

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
    private void loadTable(ResultSet _tableRS){
        // Aclaramos toda la tabla en caso de uso previo.
        tabledata.getItems().clear();
        tabledata.getColumns().clear();
        // Inicializamos la lista en donde se guardaron los registros.
        data = FXCollections.observableArrayList();
        // Agregamos columnas.
        addCol(_tableRS);
        // Agregamos Registros a una lista.
        addList(_tableRS);
        // Agregamos lista de registros a nuestra tabla.
        tabledata.setItems(data);
    }

    // Aqui es donde agregamos las columnas en la tabla dependiendo del boton al que se le dio click.
    private void addCol(ResultSet tableRS) {
        try {
            // Recibe un conjunto de resultados de alguna consultado previamente ejecutada.
            // El for corre por cada columna que tiene el conjunto de resultados recibido.
            for(int i=0 ; i<tableRS.getMetaData().getColumnCount(); i++){
                // La i nos sirve para obtener los valores dentro del conjunto de resultados.
                /* Al momento de Fijar las columnas dentro de la tabla nos pide que el indicador sea
                 * final por lo cual creamos a j */
                final int j = i;
                // Creamos nueva columna
                TableColumn col = new TableColumn();
                // Fijamos el texto utilizando el conjunto de resultados.
                col.setText(tableRS.getMetaData().getColumnName(i+1));
                // Cambiamos la anchura por estetica.
                col.setMinWidth(250);
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
                while(tableRS.next()){
                    /* Se crea una lista para poder juntar todas las columnas
                    * de una fila en un solo registro. */
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for(int i=1 ; i<=tableRS.getMetaData().getColumnCount(); i++){
                        if(tableRS.getString(i)!=null){
                            row.add(tableRS.getString(i));
                        }else{
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
    public void deleteRegister() {
        /* Revisamos si se tiene seleccionado un registro. De
        no sera asi se le pide al usuario que seleccione uno. */
        if (isSelected()) {
            // Se le pide confirmacion para borrar el registro.
            if (confirmDelete()) {
                // Se ejecuta consulta para borrar.
                String queryRes = objConexion.insertModDel("DELETE FROM " + currentTable + " WHERE " + currentId + "=" + getID());
                resultado = new Alert(Alert.AlertType.INFORMATION, queryRes);
                resultado.show();
            }
        }
        else {
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
        ObservableList<TablePosition> selectedCells = tabledata.getSelectionModel().getSelectedCells();
        TablePosition selectedCell = selectedCells.get(0);
        TableColumn desiredColumn = selectedCell.getTableColumn();
        int rowIndex = selectedCell.getRow();
        Object ID = desiredColumn.getCellObservableValue(rowIndex).getValue();
        return Integer.parseInt(String.valueOf(ID));
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
                    // Se fijan campos a insertar y sus nombres a mostrar.
                    optionalEntries = new String[]{"Beneficicario 2", "Parentesco de Beneficiario 2", "Limite de Credito"};
                    requiredEntries = new String[]{"Nombre", "Apellido Paterno", "Apellido Materno", "Nicho", "Lado", "Saldo", "Dirección", "Telefono", "Fecha de Inscripcion", "Benficiario 1", "Parentesco de Beneficiario 1"};
                    otherTableEntries = new String[] {"Nombre del Difunto", "Apellido Paterno del Difunto", "Apellido Materno del Difunto"};
                    // Se crea una pantalla para insertar y se muestra dicha pantalla.
                    AddScreen clienteScreen = new AddScreen(17, optionalEntries, requiredEntries, currentTable, objConexion, otherTableEntries);
                    Node source = (Node) ev.getSource();
                    ((Stage) source.getScene().getWindow()).setScene(clienteScreen.makeScene());
                    break;
                }
                case "Pagos": {
                    // Se utiliza una funcion declarada mas adelante para obtener el cliente que va a pagar.
                    List<String> clientIDyNombre = getClientName();
                    // Se revisa que se eligio un cliente.
                    if (clientIDyNombre != null) {
                        // Se fijan campos a insertar y sus nombres a mostrar.
                        optionalEntries = new String[]{};
                        requiredEntries = new String[]{"Cantidad"};
                        otherTableEntries = new String[]{};
                        // Se crea una pantalla para insertar y se muestra dicha pantalla.
                        AddScreen pagoScreen = new AddScreen(1, optionalEntries, requiredEntries, currentTable, objConexion, otherTableEntries);
                        Node source = (Node) ev.getSource();
                        ((Stage) source.getScene().getWindow()).setScene(pagoScreen.makeScene());
                        break;
                    }
                    else {
                        new Alert(Alert.AlertType.ERROR, "No se eligio ningun cliente.").show();
                        break;
                    }
                }
                case "Documentos": {
                    // Se utiliza una funcion declarada mas adelante para obtener el cliente que va a pagar.
                    List<String> clientIDyNombre = getClientName();
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
                    File doc;
                    doc = docChoose.showOpenDialog(source.getScene().getWindow());
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    String time = sdf.format(cal.getTime());
                    File docOut = new File("./" + clientData.get(1) + ":" + clientData.get(0) + "-" + time);
                    try {
                        Files.copy(doc.toPath(), docOut.toPath());
                        docPath = docOut.getAbsolutePath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Se revisa que se eligio un cliente.
                    if (clientIDyNombre != null) {
                        // Se fijan campos a insertar y sus nombres a mostrar.
                        optionalEntries = new String[]{};
                        requiredEntries = new String[]{"Tipo"};
                        otherTableEntries = new String[]{};
                        // Se crea una pantalla para insertar y se muestra dicha pantalla.
                        AddScreen pagoScreen = new AddScreen(1, optionalEntries, requiredEntries, currentTable, objConexion, otherTableEntries);
                        ((Stage) source.getScene().getWindow()).setScene(pagoScreen.makeScene());
                        break;
                    }
                    else {
                        new Alert(Alert.AlertType.ERROR, "No se eligio ningun cliente.").show();
                        break;
                    }
                }
                case "Gastos": {
                    // Se fijan campos a insertar y sus nombres a mostrar.
                    optionalEntries = new String[]{};
                    requiredEntries = new String[]{"Descripcion", "Monto"};
                    otherTableEntries = new String[]{};
                    // Se crea una pantalla para insertar y se muestra dicha pantalla.
                    AddScreen pagoScreen = new AddScreen(2, optionalEntries, requiredEntries, currentTable, objConexion, otherTableEntries);
                    Node source = (Node) ev.getSource();
                    ((Stage) source.getScene().getWindow()).setScene(pagoScreen.makeScene());
                    break;
                }
            }
        }
    }

    // Esta funcion genera una ventana para que el usuario seleccione un cliente.
    private static List<String> getClientName() {
        // Obtenemos todos los clientes en la base de datos.
        ResultSet clients = objConexion.consultar("SELECT ID_Cliente, Nombre, ID_Nicho FROM Clientes ORDER BY Nombre");
        // Se hace una lista para almacenar a las opciones posibles.
        List<String> comboChoices = new ArrayList<>();
        // Este variable almacena la informacion del cliente seleccionado.
        clientData = new ArrayList<>();
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
                if( i == 0) {
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
                    }
                    else {
                        table = "buen_pastor";
                    }
                    nichoType = objConexion.consultar("SELECT Tipo_Nicho FROM " + table +" WHERE ID_Cliente=" + clientData.get(0));
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

    private void checkSpace(ActionEvent ae) {
        Object o = ae.getSource();
        Button b = null;
        SingleSelectionModel<Tab> selectionModel = TabPaneM.getSelectionModel();


        if (o instanceof Button)
            b = (Button) o;

        if (b != null)

            selectionModel.select(tabTabla);



    }

    public void loadStatus() {
        String[] t=new String[200];
        String[] statusar=new String[200];
        String[] typear=new String[200];
        int cont=0;
        Button[] button = new Button[200];

        gridDisp.setPadding(new Insets(5));
        gridDisp.setHgap(5);
        gridDisp.setVgap(5);

        for (char alphabet = 'A'; alphabet <= 'N'; alphabet++) {
            for(int i=1;i<=14;i++) {
                t[cont] = alphabet + Integer.toString(i);
                statusar[cont]=getStatus(t[cont]);

                typear[cont]=getType(t[cont]);
                cont++;
            }
        }

        for (int r = 0; r < 14; r++) {
            for (int c = 0; c < 14; c++) {
                int number = 14 * r + c;
                button[number] =new Button(t[number]);

                switch (typear[number]){
                    case "Imagen":
                        button[number].getStyleClass().add("buttonbold");
                        break;

                }

                switch (statusar[number]!=null ? statusar[number] : "Libre") {
                    case "Ocupado":
                        button[number].getStyleClass().add("buttonfree");
                        break;
                    case "Libre":
                        button[number].getStyleClass().add("buttonused");
                        break;
                }
                button[number].setPrefSize(50,50);
                gridDisp.add(button[number], c, r);
                button[number].setOnAction(event -> checkSpace(event));
            }
        };
    }

    public void loadStatusBP() {
        String[] t=new String[200];
        String[] statusar=new String[200];
        String[] typear=new String[200];
        int cont=0;
        Button[] button = new Button[200];

        gridDisp2.setPadding(new Insets(5));
        gridDisp2.setHgap(5);
        gridDisp2.setVgap(5);

        for (char alphabet = 'A'; alphabet <= 'N'; alphabet++) {
            for(int i=1;i<=14;i++) {
                t[cont] = alphabet + Integer.toString(i);
                statusar[cont]=getStatus(t[cont]);

                typear[cont]=getType(t[cont]);
                cont++;
            }
        }

        for (int r = 0; r < 14; r++) {
            for (int c = 0; c < 14; c++) {
                int number = 14 * r + c;
                button[number] =new Button(t[number]);

                switch (typear[number]){
                    case "Imagen":
                        button[number].getStyleClass().add("buttonbold");
                        break;

                }

                switch (statusar[number]!=null ? statusar[number] : "Libre") {
                    case "Ocupado":
                        button[number].getStyleClass().add("buttonfree");
                        break;
                    case "Libre":
                        button[number].getStyleClass().add("buttonused");
                        break;
                }
                button[number].setPrefSize(50,50);
                gridDisp2.add(button[number], c, r);
                button[number].setOnAction(event -> checkSpace(event));
            }
        };
    }

    private String getStatus(String id){

        try{
            if(tableinTab.equals("piedad")) {

                ResultSet c = objConexion.consultar("SELECT Estado FROM " + tableinTab + " WHERE ID_Nicho='" + id + "';");
                while (c.next()) {
                    status = c.getString("Estado");



                }}
                if (tableinTab.equals("buen_pastor")) {

                    List<String> list = new ArrayList<>();
                    ResultSet c = objConexion.consultar("SELECT estado FROM buen_pastor where id_nicho LIKE '"+id+"__' group by estado having count(*) >= 4;");
                    while (c.next()) {
                        status=c.getString("estado");

                    }
                    return status;
                }


        }catch (Exception e){
            resultado = new Alert(Alert.AlertType.ERROR,"No funciona la parte del estado" );
            resultado.show();
        }

        return status;
    }

    private String getType(String id){
        try{
            if(tableinTab.equals("piedad")){

                ResultSet c = objConexion.consultar("SELECT Tipo_Nicho FROM piedad WHERE ID_Nicho='"+id+"';");
                while (c.next()) {
                    type = c.getString("Tipo_Nicho");
                }
            }
            if(tableinTab.equals("buen_pastor")){

                ResultSet c = objConexion.consultar("SELECT Tipo_Nicho FROM buen_pastor WHERE ID_Nicho='"+id+"-1';");
                while (c.next()) {
                    type = c.getString("Tipo_Nicho");

                }
            }

        }catch (Exception e){
            resultado = new Alert(Alert.AlertType.ERROR,"No funciona la parte del tipo" );
            resultado.show();
        }
        return type;
    }

    public void checkTab(){
        if(TabPaneD.getSelectionModel().getSelectedItem()==tabPiedad) {
            tableinTab="piedad";
            loadStatus();
        }
        if(TabPaneD.getSelectionModel().getSelectedItem()==tabBuen_Pastor) {
            tableinTab="buen_pastor";
            loadStatusBP();
        }
    }

    public void showReport(){
        try {
            new JavaCallJasperReport(currentTable);
        } catch (JRException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}