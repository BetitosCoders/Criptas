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
    private Dialog about;
    private Connection conexion;
    private ObservableList<ObservableList> data;
    private Stage stage;
    private Scene scene;
    private int number;
    private String[] numb = new String[196];
    private int cont;
    private String[] t = new String[200];
    private String nombre;
    private String currentTable, currentId;
    private String tableinTab, status, type;
    static List<String> clientData;
    static String docPath = null;
    @FXML
    TableView tabledata;
    @FXML
    Tab tabPiedad, tabBuen_Pastor, tabDisponibilidad, tabTabla;
    @FXML
    TextField txtUser;
    @FXML
    private TextField txtPass;
    @FXML
    BorderPane borderMain;
    @FXML
    Button btnClientes, btnPiedad, btnPastor, btnPagos, btnDocumentos, btnAdd, btnMod, btnDel,btnImp;
    @FXML
    Button A1, A2;
    @FXML
    GridPane gridDisp, gridDisp2;
    @FXML
    private TabPane tabPaneM, tabPaneD;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
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
            scene=new Scene(parent,1280,700);
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
    public void clickClientes() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Clientes";
        currentId = "ID_Cliente";
        callProcedure("{call showClients()}");
    }

    public void clickPiedad() {
        btnAdd.setDisable(true);
        btnMod.setDisable(true);
        btnDel.setDisable(true);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "piedad";
        currentId = "ID_Nicho";
        callProcedure("{call showPiedad()}");
    }

    public void clickBuenPastor() {
        btnAdd.setDisable(true);
        btnMod.setDisable(true);
        btnDel.setDisable(true);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "buen_pastor";
        currentId = "ID_Nicho";
        callProcedure("{call showBuenPastor()}");
    }

    public void clickPagos() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Pagos";
        currentId = "ID_Pago";
        callProcedure("{call showPagos()}");
    }

    public void clickDocumentos() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Documentos";
        callProcedure("{call showDocs()}");
    }

    public void clickGastos() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Gastos";
        callProcedure("{call showGastos()}");
    }

    public void clickIngresos() {
        btnAdd.setDisable(false);
        btnMod.setDisable(false);
        btnDel.setDisable(false);
        btnImp.setDisable(false);
        tabPaneM.getSelectionModel().select(tabTabla);
        currentTable = "Ingresos";
        callProcedure("{call showIngresos()}");
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
        // Agregamos lista de registros a nuestra tabla.
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
                col.setMinWidth(150);
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
        selection = selection.replace('[',' ').trim();
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
                    // Se fijan campos a insertar y sus nombres a mostrar.
                    optionalEntries = new String[]{"Beneficicario 2", "Parentesco de Beneficiario 2", "Limite de Credito"};
                    requiredEntries = new String[]{"Nombre", "Apellido Paterno", "Apellido Materno", "Nicho", "Lado", "Saldo", "Dirección", "Telefono", "Fecha de Inscripción", "Benficiario 1", "Parentesco de Beneficiario 1"};
                    otherTableEntries = new String[]{"Nombre del Difunto", "Apellido Paterno del Difunto", "Apellido Materno del Difunto"};
                    // Se crea una pantalla para insertar y se muestra dicha pantalla.
                    AddScreen clienteScreen = new AddScreen(17, optionalEntries, requiredEntries, currentTable, objConexion, otherTableEntries);
                    Node source = (Node) ev.getSource();
                    ((Stage) source.getScene().getWindow()).setScene(clienteScreen.makeScene());
                    break;
                }
                case "Pagos": {
                    // Se utiliza una funcion declarada mas adelante para obtener el cliente que va a pagar.
                    List<String> clientIDyNombre = getClientInfoCombo();
                    // Se revisa que se eligio un cliente.
                    if (clientIDyNombre != null) {
                        // Se fijan campos a insertar y sus nombres a mostrar.
                        optionalEntries = new String[]{};
                        requiredEntries = new String[]{"Cantidad", "Fecha"};
                        otherTableEntries = new String[]{};
                        // Se crea una pantalla para insertar y se muestra dicha pantalla.
                        AddScreen pagoScreen = new AddScreen(2, optionalEntries, requiredEntries, currentTable, objConexion, otherTableEntries);
                        Node source = (Node) ev.getSource();
                        ((Stage) source.getScene().getWindow()).setScene(pagoScreen.makeScene());
                        break;
                    } else {
                        new Alert(Alert.AlertType.ERROR, "No se eligio ningun cliente.").show();
                        break;
                    }
                }
                case "Documentos": {
                    // Se utiliza una funcion declarada mas adelante para obtener el cliente que va a pagar.
                    List<String> clientIDyNombre = getClientInfoCombo();
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
                        requiredEntries = new String[]{"Tipo", "Fecha"};
                        otherTableEntries = new String[]{};
                        // Se crea una pantalla para insertar y se muestra dicha pantalla.
                        AddScreen pagoScreen = new AddScreen(2, optionalEntries, requiredEntries, currentTable, objConexion, otherTableEntries);
                        ((Stage) source.getScene().getWindow()).setScene(pagoScreen.makeScene());
                        break;
                    } else {
                        new Alert(Alert.AlertType.ERROR, "No se eligio ningun cliente.").show();
                        break;
                    }
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
            String btext = b.getText();
            //TabPaneM.getSelectionModel().select(tabTabla);
            int x=0;
            for (int i = 0; i<numb.length; i++) {
            if(numb[i].equals(btext)) {

                if(tabPaneD.getSelectionModel().isSelected(1)) {
                    clickPiedad();
                    tabledata.getSelectionModel().select(i);
                    tabPaneM.getSelectionModel().select(tabTabla);
                    tabledata.scrollTo(i);
                }

                    x = (i * 4);

                if(tabPaneD.getSelectionModel().isSelected(0)) {
                    clickBuenPastor();
                    tabledata.getSelectionModel().select(x);
                    tabPaneM.getSelectionModel().select(tabTabla);
                    tabledata.scrollTo(x);


                }}}


        }
    }

    //Crea los botones de piedad en el gridpane
    public void loadStatus() {
        String[] statusar=new String[200];
        String[] t=new String[200];
        String[] typear=new String[200];
        int cont=0;
        Button[] button = new Button[200];
    //Espacio entre los botones
        gridDisp.setPadding(new Insets(5));
        gridDisp.setHgap(5);
        gridDisp.setVgap(5);
    //For para crear el texto de los botones
        for (char alphabet = 'A'; alphabet <= 'N'; alphabet++) {
            for(int i=1;i<=14;i++) {
                //en este arreglo se guarda el nombre de los botones
                t[cont] = alphabet + Integer.toString(i);
                //Obtiene el estado de ese botón
                statusar[cont]=getStatus(t[cont]);
                //Obtiene el tipo de ese botón
                typear[cont]=getType(t[cont]);
                cont++;
            }
        }
    //For para crear los botones
        for (int r = 0; r < 14; r++) {
            for (int c = 0; c < 14; c++) {
                //Calcula el numero del botón
                 number = 14 * r + c;
                //Crea el botón
                button[number] =new Button(t[number]);
                numb[number]=t[number];
                switch (typear[number]){
                    //Si es imagen el tipo le añade el estilo buttonbold
                    case "Imagen":
                        button[number].getStyleClass().add("buttonbold");
                        break;

                }

                switch (statusar[number]!=null ? statusar[number] : "Libre") {
                    //Si es ocupado el estado usa el estilo buttonfree
                    case "Ocupado":
                        button[number].getStyleClass().add("buttonfree");
                        break;
                     //Si es libre el estado usa buttonused
                    case "Libre":
                        button[number].getStyleClass().add("buttonused");
                        break;
                }
                //define la medida de los botones
                button[number].setPrefSize(50,50);
                button[number].setOnAction(event -> checkSpace(event,number));
                //añade el botón al grid
                gridDisp.add(button[number], c, r);
                //Accion del botón

            }
        }
    }

    //Crea los botones de buen pastor en el gridpane
    public void loadStatusBP() {

        String[] statusar=new String[200];
        String[] typear=new String[200];
        cont=0;
        Button[] button = new Button[200];
        //Espacio entre los botones
        gridDisp2.setPadding(new Insets(5));
        gridDisp2.setHgap(5);
        gridDisp2.setVgap(5);
        //For para crear el texto de los botones
        for (char alphabet = 'A'; alphabet <= 'N'; alphabet++) {
            for(int i=1;i<=14;i++) {
                //en este arreglo se guarda el nombre de los botones
                t[cont] = alphabet + Integer.toString(i);
                //Obtiene el estado de ese botón
                statusar[cont]=getStatus(t[cont]);
                //Obtiene el tipo de ese botón
                typear[cont]=getType(t[cont]);
                cont++;
            }
        }
        //For para crear los botones
        for (int r = 0; r < 14; r++) {
            for (int c = 0; c < 14; c++) {
                //Calcula el numero del botón
                 number = 14 * r + c;
                //Crea el botón
                button[number] =new Button(t[number]);
                numb[number]=t[number];
                switch (typear[number]){
                    //Si es imagen el tipo le añade el estilo buttonbold
                    case "Imagen":
                        button[number].getStyleClass().add("buttonbold");
                        break;

                }

                switch (statusar[number]!=null ? statusar[number] : "Libre") {
                    //Si es ocupado el estado usa el estilo buttonfree
                    case "Ocupado":
                        button[number].getStyleClass().add("buttonfree");
                        break;
                    //Si es libre el estado usa buttonused
                    case "Libre":
                        button[number].getStyleClass().add("buttonused");
                        break;
                }
                //define la medida de los botones
                button[number].setPrefSize(50,50);
                button[number].setOnAction(event -> checkSpace(event,number));

                //añade el botón al grid
                gridDisp2.add(button[number], c, r);
                //Accion del botón

            }
        }
    }

    //Obtiene el estado del nicho para poder marcarlo ocupado o libre
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

    //Obtiene el tipo de nicho que es para marcarlo diferente
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

    //Checa cual pestaña esta seleccionada y carga los botones de disponiblidad
    public void checkTab(){
        if(tabPaneD.getSelectionModel().getSelectedItem()==tabPiedad) {
            tableinTab="piedad";
            loadStatus();
        }
        if(tabPaneD.getSelectionModel().getSelectedItem()==tabBuen_Pastor) {
            tableinTab="buen_pastor";
            loadStatusBP();
        }
    }

    //Muestra el reporte segun la tabla cuando le das imprimir
    public void showReport(){
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
            case "Clientes" : {
                String queryClients = "SELECT * FROM Clientes WHERE ID_Cliente=" + getID();
                ResultSet clients = objConexion.consultar(queryClients);
                clientData.clear();
                try {
                    while (clients.next()) {
                        clientData.add(clients.getString("Nombre"));
                        clientData.add(clients.getString("AP_Paterno"));
                        clientData.add(clients.getString("AP_Materno"));
                        clientData.add(clients.getString("ID_Nicho"));
                        clientData.add(clients.getString("Lado"));
                        clientData.add(clients.getString("Saldo"));
                        clientData.add(clients.getString("Dirección"));
                        clientData.add(clients.getString("Teléfono"));
                        clientData.add(clients.getString("Fecha_Ins"));
                        clientData.add(clients.getString("Ben1"));
                        clientData.add(clients.getString("Ben1_Par"));
                        clientData.add(clients.getString("Ben2"));
                        clientData.add(clients.getString("Ben2_Par"));
                        clientData.add(clients.getString("Lim_Credito"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String lado;
                if (clientData.get(4).equals("Derecho"))
                    lado = "Piedad";
                else
                    lado = "Buen_Pastor";
                String queryDif = "SELECT * FROM " + lado + " WHERE ID_Nicho='" + clientData.get(3) + "'";
                ResultSet dif = objConexion.consultar(queryDif);
                try {
                    while(dif.next()) {
                        clientData.add(dif.getString("Nombre_Dif"));
                        clientData.add(dif.getString("AP_Pat_Dif"));
                        clientData.add(dif.getString("AP_Mat_Dif"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "Pagos" : {
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
                ResultSet clients=objConexion.consultar(queryClients);
                try {
                    while (clients.next()) {
                        if (clients.getString("ID_Cliente").equals(idClient)) {
                            clientData.add(clients.getString("ID_Cliente") + ": " +
                                    clients.getString("Nombre") + " " +
                                    clients.getString("AP_Paterno") + " " +
                                    clients.getString("AP_Materno") + "*");
                        }
                        else {
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
            case "Gastos":{
                String queryGastos="SELECT * FROM Gastos WHERE ID_Gasto=" + getID();
                ResultSet gastos=objConexion.consultar(queryGastos);
                clientData.clear();
                try {
                    while (gastos.next()){
                        clientData.add(gastos.getString("Descripción"));
                        clientData.add(gastos.getString("Monto"));
                        clientData.add(gastos.getString("Fecha"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "Ingresos":{
                String queryGastos="SELECT * FROM Ingresos WHERE ID_Ingreso=" + getID();
                ResultSet gastos=objConexion.consultar(queryGastos);
                clientData.clear();
                try {
                    while (gastos.next()){
                        clientData.add(gastos.getString("Descripción"));
                        clientData.add(gastos.getString("Monto"));
                        clientData.add(gastos.getString("Fecha"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "Documentos":{
                String queryDocs = "SELECT * FROM Documentos WHERE ID_Documentos=" + getID();
                ResultSet Docs = objConexion.consultar(queryDocs);
                clientData.clear();
                String idClient = null;
                try {
                    while (Docs.next()) {
                        idClient = Docs.getString("ID_Cliente");
                        clientData.add(Docs.getString("Tipo"));
                        clientData.add(Docs.getString("Fecha"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String queryClients = "SELECT ID_Cliente, Nombre, AP_Paterno, AP_Materno FROM Clientes";
                ResultSet clients=objConexion.consultar(queryClients);
                try {
                    while (clients.next()) {
                        if (clients.getString("ID_Cliente").equals(idClient)) {
                            clientData.add(clients.getString("ID_Cliente") + ": " +
                                    clients.getString("Nombre") + " " +
                                    clients.getString("AP_Paterno") + " " +
                                    clients.getString("AP_Materno") + "*");
                        }
                        else {
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
    public void modify(ActionEvent ev){
        String[] optionalEntries;
        String[] requiredEntries, otherTableEntries;
        if (isSelected()) {
            switch (currentTable) {
                case "Clientes": {
                    getClientInfoTable();
                    optionalEntries = new String[]{"Beneficicario 2", "Parentesco de Beneficiario 2", "Limite de Credito"};
                    requiredEntries = new String[]{"Nombre", "Apellido Paterno", "Apellido Materno", "Nicho", "Lado", "Saldo", "Dirección", "Telefono", "Fecha de Inscripcion", "Benficiario 1", "Parentesco de Beneficiario 1"};
                    otherTableEntries = new String[] {"Nombre del Difunto", "Apellido Paterno del Difunto", "Apellido Materno del Difunto"};
                    ModScreen modify = new ModScreen(requiredEntries, optionalEntries, otherTableEntries, 17, currentTable, objConexion, getID() + "");
                    Node source = (Node) ev.getSource();
                    ((Stage) source.getScene().getWindow()).setScene(modify.makeScene());
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
        }
        else {
            resultado = new Alert(Alert.AlertType.INFORMATION, "Eliga el registro a modificar.");
            resultado.show();
        }
    }

    //Ventana de acerca de
    public void runAbout() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("about.fxml"));
        Parent parent = loader.load();
        scene=new Scene(parent,600,291);
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setScene(scene);
        dialog.showAndWait();

    }
}