package main;

import controller.ProfileScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends Application {
    private static Connection connection;
    @Override
    public void start(Stage primaryStage) throws IOException {
        System.out.println("Application invoked!");
        FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("../fxml/ProfileScreen.fxml"));
        System.out.println("Login FXML Loaded!");
        try {
            primaryStage.setScene(new Scene(fxmlLoader.load()));
        } catch (IOException e) {
            System.out.println("Exception in Main while loading FXML!");
            e.printStackTrace();
        }
        primaryStage.setResizable(false);
        primaryStage.show();
        ProfileScreenController profileScreenController = fxmlLoader.getController();
        profileScreenController.first();
    }
    public static Connection getConnection() {
        System.out.println("Connecting to database....");
        if(connection != null) return connection;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url="jdbc:mysql://localhost:3306/ngdb";
            connection= DriverManager.getConnection(url,"root","Siddh@nt234");

            System.out.println("Database connected!!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
