package Server;

import Server.controller.Admin;
import Server.controller.Client;
import Server.controller.ClientManager;
import Server.controller.RoomManager;
import Shared.security.RSA;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunServer {

    public static volatile ClientManager clientManager;
    public static volatile RoomManager roomManager;
    public static volatile RSA serverSide;
    public static boolean isShutDown = false;
    public static ServerSocket ss;

    public RunServer() {

        try {
            int port = 5056;

            ss = new ServerSocket(port);
            System.out.println("Created Server at port " + port + ".");

            // init rsa key
            serverSide = new RSA()
                    .preparePrivateKey("src/Server/rsa_keypair/privateKey");

            // init managers
            clientManager = new ClientManager();
            roomManager = new RoomManager();

            // create threadpool
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    10, // corePoolSize
                    100, // maximumPoolSize
                    10, // thread timeout
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(8) // queueCapacity
            );

            // admin
            executor.execute(new Admin());

            // server main loop - listen to client's connection
            while (!isShutDown) {
                try {
                    // socket object to receive incoming client requests
                    Socket s = ss.accept();
                    // System.out.println("+ New Client connected: " + s);

                    // create new client runnable object
                    Client c = new Client(s);
                    clientManager.add(c);

                    // execute client runnable
                    executor.execute(c);

                } catch (IOException ex) {
                    // Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    isShutDown = true;
                }
            }

            System.out.println("shutingdown executor...");
            executor.shutdownNow();

        } catch (IOException ex) {
            Logger.getLogger(RunServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        // Load MySQL JDBC driver from library folder dynamically
        try {
            String libPath = "library/mysql-connector-j-8.0.33.jar";
            if (Files.exists(Paths.get(libPath))) {
                URL jarUrl = Paths.get(libPath).toAbsolutePath().toUri().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, ClassLoader.getSystemClassLoader());
                
                // Load and register driver class explicitly
                Class.forName("com.mysql.cj.jdbc.Driver", true, classLoader);
                System.out.println("✓ Loaded JDBC driver from: " + libPath);
            } else {
                System.err.println("⚠ Warning: JDBC jar not found at " + libPath);
            }
        } catch (Exception e) {
            System.err.println("⚠ Warning: Could not load JDBC jar: " + e.getMessage());
            e.printStackTrace();
        }
        
        new RunServer();
    }
}
