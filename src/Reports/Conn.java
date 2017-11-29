package Reports;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conn {

    public static Connection getMySQLConnection()
            throws ClassNotFoundException, SQLException {
        String hostName = "tec.codigobueno.org";
        String dbName = "criptas";
        String userName = "teccodig_carloss";
        String password = "devpass9";
        return getMySQLConnection(hostName, dbName, userName, password);
    }

    public static Connection getMySQLConnection(String hostName, String dbName,
                                                String userName, String password) throws SQLException,
            ClassNotFoundException {

        // Declare the class Driver for MySQL DB
        // This is necessary with Java 5 (or older)
        // Java6 (or newer) automatically find the appropriate driver.
        // If you use Java> 5, then this line is not needed.
        Class.forName("com.mysql.jdbc.Driver");

        // Cấu trúc URL Connection dành cho Oracle
        // Ví dụ: jdbc:mysql://localhost:3306/simplehr
        String connectionURL = "jdbc:mysql://" + hostName + ":3306/" + dbName;

        Connection conn = DriverManager.getConnection(connectionURL, userName,
                password);
        System.out.println("Funciona");
        return conn;
    }
}