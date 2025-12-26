/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.DB.Layers.DBConnector;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nguye
 */
public class MysqlConnector {

    // Static loader to prevent garbage collection
    private static URLClassLoader jdbcClassLoader = null;
    private static boolean driverLoaded = false;

    Connection conn = null;

    String server = "localhost:3306";
    String db = "carodb";
    String user = "root";
    String pass = "123456";

    public MysqlConnector() {
        checkDriver();
        setupConnection();
    }

    public void logIn(String userName, String pass) {
        this.user = userName;
        this.pass = pass;
        setupConnection();
    }

    boolean checkDriver() {
        // Nếu đã load driver rồi, không cần load lại
        if (driverLoaded) {
            return true;
        }
        
        try {
            // Thử driver MySQL mới trước
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                driverLoaded = true;
                return true;
            } catch (ClassNotFoundException e1) {
                // Nếu không có, thử load từ file jar
                try {
                    String jarPath = "library/mysql-connector-j-8.0.33.jar";
                    if (Files.exists(Paths.get(jarPath))) {
                        URL jarUrl = Paths.get(jarPath).toAbsolutePath().toUri().toURL();
                        // Lưu reference static để tránh garbage collection
                        jdbcClassLoader = new URLClassLoader(new URL[]{jarUrl}, ClassLoader.getSystemClassLoader());
                        Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.Driver", true, jdbcClassLoader);
                        
                        // Explicitly register driver with DriverManager
                        java.sql.Driver driver = (java.sql.Driver) driverClass.getDeclaredConstructor().newInstance();
                        DriverManager.registerDriver(driver);
                        
                        driverLoaded = true;
                        System.out.println("✓ Loaded MySQL JDBC driver dynamically from " + jarPath);
                        return true;
                    }
                } catch (Exception e2) {
                    System.err.println("Failed to load from jar: " + e2.getMessage());
                    // Tiếp tục thử driver cũ
                }
                
                // Thử driver cũ nếu driver mới không có
                Class.forName("com.mysql.jdbc.Driver");
                driverLoaded = true;
                return true;
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Khong tim thay Driver mysql !!");
            return false;
        } catch (Exception e) {
            System.err.println("Error loading driver: " + e.getMessage());
            return false;
        }
    }

    public boolean setupConnection() {
        try {
            // Thử URL với timezone trước (MySQL 8.0)
            String url = "jdbc:mysql://" + server + "/" + db + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false";
            try {
                conn = DriverManager.getConnection(url, user, pass);
                return true;
            } catch (SQLException e1) {
                // Nếu thất bại, thử URL không có timezone (MySQL 5.x)
                url = "jdbc:mysql://" + server + "/" + db + "?useUnicode=true&characterEncoding=UTF-8";
                conn = DriverManager.getConnection(url, user, pass);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Loi ket noi DB: " + e.getMessage());
            return false;
        }
    }

    public ResultSet sqlQry(PreparedStatement stm) {
        if (checkConnection()) {
            try {
                ResultSet rs = stm.executeQuery();
                return rs;
            } catch (SQLException e) {
                System.err.println("Loi thuc thi sql query: " + stm.toString() + " , " + e.getMessage());
            }
        }
        return null;
    }

    public boolean sqlUpdate(PreparedStatement stm) {
        if (checkConnection()) {
            try {
                stm.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("Loi thuc thi sql update: " + stm.toString() + " , " + e.getMessage());
            }
        }
        return false;
    }

    public boolean checkConnection() {
        if (conn == null) {
            return false;
        }
        try {
            // Kiểm tra xem connection có còn hợp lệ không
            return !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Không thể đóng kết nối tới " + db);
            return false;
        }
    }

    public Connection getConnection() {
        return conn;
    }

}
