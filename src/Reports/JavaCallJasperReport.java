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



    public JavaCallJasperReport() throws JRException, SQLException, ClassNotFoundException {
        String reportSrcFile = "C:/report1.jasper";

        // First, compile jrxml file.

        Connection conn = Conn.getMySQLConnection();

        // Parameters for report
        Map<String, Object> parameters = new HashMap<String, Object>();

        JasperPrint print = JasperFillManager.fillReport(reportSrcFile,
                parameters, conn);
        JasperViewer viewer = new JasperViewer(print,false); //Creamos la vista del Reporte
        viewer.setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Le agregamos que se cierre solo el reporte cuando lo cierre el usuario
        viewer.setVisible(true);
//        // Make sure the output directory exists.
//        File outDir = new File("C:/jasperoutput");
//        outDir.mkdirs();
//
//        // PDF Exportor.
//        JRPdfExporter exporter = new JRPdfExporter();
//
//        ExporterInput exporterInput = new SimpleExporterInput(print);
//        // ExporterInput
//        exporter.setExporterInput(exporterInput);
//
//        // ExporterOutput
//        OutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(
//                "C:/jasperoutput/FirstJasperReport.pdf");
//        // Output
//        exporter.setExporterOutput(exporterOutput);
//
//        //
//        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();

    }
    private String conectarSQL() {
        String estado = null;
        objConexion = new ConexionMySQL("teccodig_carloss", "devpass9", "teccodig_Criptas", "tec.codigobueno.org", 3306);
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


