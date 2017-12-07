package Reports;

import javafx.scene.control.Alert;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import sample.ConexionMySQL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class JavaCallJasperReport {
    private Alert resultado;
    private static Connection conexion;
    private static ConexionMySQL objConexion;



    public JavaCallJasperReport(String nombre) throws JRException, SQLException, ClassNotFoundException {
        // Ruta del .jasper
        String reportSrcFile = "src/Reports/"+nombre+".jasper";

        // Conexion
        conectarSQL();
        Connection conn = conexion;

        // Parameters for report
        Map<String, Object> parameters = new HashMap<String, Object>();

        JasperPrint print = JasperFillManager.fillReport(reportSrcFile,
                parameters, conn);
        JasperViewer viewer = new JasperViewer(print,false); //Creamos la vista del Reporte
        viewer.setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Le agregamos que se cierre solo el reporte cuando lo cierre el usuario
        viewer.setVisible(true);


    }
    //Conexión
    private String conectarSQL() {
        String estado = null;
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
    }


