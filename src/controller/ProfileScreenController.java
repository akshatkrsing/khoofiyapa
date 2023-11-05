package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfileScreenController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    @FXML
    MenuItem fileMenuItem;
    @FXML
    MenuItem folderMenuItem;
    @FXML
    MenuItem refreshMenuItem;
    @FXML
    MenuItem aesAlgoMenuItem;
    @FXML
    MenuItem desAlgoMenuItem;
    @FXML
    MenuItem rsaAlgoMenuItem;
    @FXML
    Label algoLabel;
    @FXML
    Label fileLabel;
    @FXML
    Button encryptButton;

    public void first(){

    }
    public void refresh(){

    }
    public void browseFile(){

    }
    public void browseFolder(){

    }
    public void encryptFile(){

    }
}
