package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import main.Main;
import table.ParamsTable;
import util.HashUtil;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SetRootPathDialogController implements Initializable {

    private Connection connection;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    @FXML
    private Button rootFolderBrowseButton;
    @FXML
    TextField rootFolderPathField;
    @FXML
    private PasswordField enterPasswordField;
//    @FXML
//    private Button applyRootFolderPathButton;
//    @FXML
//    private Button confirmRootFolderPathButton;
    private DialogPane dialogPane;
    private ButtonType applyButtonType;

    private String previousRootFolderPathFieldContent;
    public void first(){
        connection = Main.getConnection();
//        applyRootFolderPathButton.setDisable(true);
        dialogPane = getCurrentDialogPane(rootFolderBrowseButton);
        applyButtonType = dialogPane.getButtonTypes().stream()
                .filter(buttonType -> buttonType.getButtonData() == ButtonType.APPLY.getButtonData())
                .findFirst()
                .orElse(null);
        DirectoryChooser directoryChooser = new DirectoryChooser();
        rootFolderBrowseButton.setOnAction(event -> {
            File selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {
                System.out.println("Selected Folder: " + selectedDirectory.getAbsolutePath());
                rootFolderPathField.setText(selectedDirectory.getAbsolutePath());
                // Handle the selected folder as needed
            } else {
                //
                System.out.println("No folder selected");
            }
            if(rootFolderPathField.getText() != previousRootFolderPathFieldContent) {
                if (applyButtonType != null) {
                    dialogPane.lookupButton(applyButtonType).setDisable(false);
                }
                previousRootFolderPathFieldContent = rootFolderPathField.getText();
            }
        });
//        applyRootFolderPathButton.setOnAction(event -> {
//            // check if folder exists
//
//            profileScreenController.rootFolderPath = rootFolderPathField.getText();
//            // update param in db
//
//            applyRootFolderPathButton.setDisable(true);
//        });
//        confirmRootFolderPathButton.setOnAction(event -> {
//            // create root folder
//
//            // close dialog
//
//        });



    }
    public void enterPassword(){
        int resultquery=0;
        try{
            PreparedStatement preparedStatement=connection.prepareStatement(ParamsTable.QUERY_UPDATE_PARAM_VALUE);
            preparedStatement.setString(1, HashUtil.getMd5(enterPasswordField.getText()));
            preparedStatement.setString(2,"password");
            System.out.println(preparedStatement);
            resultquery=preparedStatement.executeUpdate();
            System.out.println(resultquery);

        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    private DialogPane getCurrentDialogPane(Button button) {
        Scene scene = button.getScene();
        if (scene != null) {
            return (DialogPane) scene.getRoot();
        }
        return null;
    }
}
