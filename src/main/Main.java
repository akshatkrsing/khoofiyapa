package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage){
        System.out.println("Application invoked!");
        FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("../fxml/ProfileScreen.fxml"));
        System.out.println("Login FXML Loaded!");
//        try {
//            System.out.println("Creating a new connection");
//            socket=new Socket("localhost",6971);
//            System.out.println(socket);
//            oos=new ObjectOutputStream(socket.getOutputStream());
//            ois=new ObjectInputStream(socket.getInputStream());
//            System.out.println("Connection established and io streams created");

//            System.out.println(Thread.currentThread());
//        } catch (IOException e) {
//            System.out.println("socket connect negative!");
//            e.printStackTrace();
//        }

//        primaryStage.setTitle("Sign In");
        try {
            primaryStage.setScene(new Scene(fxmlLoader.load()));
//            LoginController login=fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.show();
    }
}
