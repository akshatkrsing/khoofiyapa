package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import util.GuiUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
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
    @FXML
    PasswordField privateKeyField;
    @FXML
    PasswordField confirmPrivateKeyField;
    @FXML
    CheckMenuItem customCheckMenuItem;
    @FXML
    CheckMenuItem generatedCheckMenuItem;

    public void first(){
        searchField.setPromptText("Enter search term");

        // Set up a short delay to trigger search after typing
        searchDelay.getKeyFrames().add(new KeyFrame(Duration.millis(500), event -> performSearch()));
        searchDelay.setCycleCount(1);

        searchField.setOnKeyTyped(this::handleKeyTyped);
    }
    public void refresh(){

    }


        File browsedFile;
    public void browseFile(){
        FileChooser fileChooser = new FileChooser();
        browsedFile = fileChooser.showOpenDialog(null);
    }
    public void browseFolder(){

    }
    public void encryptFile(){
        try {
            // Generate a random AES key
            assert browsedFile != null;
            FileChooser encryptedFileChooser = new FileChooser();
            File encryptedFile = encryptedFileChooser.showSaveDialog(null);
            encryptedFile.createNewFile();
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();

            // Create a random IV (Initialization Vector)
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Initialize the AES cipher for encryption
            Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            FileInputStream inputStream = new FileInputStream(browsedFile);
            FileOutputStream outputStream = new FileOutputStream(encryptedFile);
            outputStream.write(iv); // Write the IV to the output file

            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, encryptCipher);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }

            cipherOutputStream.close();
            inputStream.close();
            outputStream.close();
            browsedFile = null;
            System.out.println("File encrypted successfully.");
        } catch(AssertionError e){
            GuiUtil.alert(Alert.AlertType.ERROR,"No files or folder selected!");
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private TextField searchField;

    @FXML
    private FlowPane searchResultFlowPane;

//    @FXML
//    private Button searchButton;

    private String searchDirectory = ""; // Change to the directory you want to search

    private Timeline searchDelay = new Timeline();


    private void handleKeyTyped(KeyEvent event) {
        // Reset the timer on each key typed
        searchDelay.stop();
        searchDelay.setCycleCount(1);
        searchDelay.play();
    }
//    public void searchButtonClicked(ActionEvent event) {
//        performSearch();
//    }
    @FXML
    private void performSearch() {
        String searchTerm = searchField.getText();
        searchResultFlowPane.getChildren().clear();
        if (searchTerm.length() > 0) {
            List<File> foundFiles = searchFiles(searchDirectory, searchTerm);
            for(File file : foundFiles){
                FileSystemView fileSystemView = FileSystemView.getFileSystemView();
                Icon icon = fileSystemView.getSystemIcon(file);
                BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
                icon.paintIcon(null,bufferedImage.getGraphics(),0,0);
                int dw = 64;
                int dh = 64;
                BufferedImage scaledImage = new BufferedImage(dw, dh,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = scaledImage.createGraphics();
                g2d.drawImage(bufferedImage, 0, 0, dw, dh, null);
                g2d.dispose();

                Image fxImage = SwingFXUtils.toFXImage(scaledImage,null);

                ImageView imageView = new ImageView(fxImage);
                imageView.setFitWidth(64);
                imageView.setFitHeight(64);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);
                searchResultFlowPane.getChildren().add(imageView);
            }
//            displaySearchResults(foundFiles);
        } else {
           //
        }
    }

    private List<File> searchFiles(String directory, String searchQuery) {
        List<File> foundFiles = new ArrayList<>();
        File dir = new File(directory);

        if (!dir.exists()) {
            return foundFiles;
        }

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    foundFiles.addAll(searchFiles(file.getAbsolutePath(), searchQuery));
                } else {
                    if (file.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                        foundFiles.add(file);
                    }
                }
            }
        }
        return foundFiles;
    }

//    private void displaySearchResults(List<File> foundFiles) {
//        resultTextArea.clear();
//
//        if (foundFiles.isEmpty()) {
//            resultTextArea.appendText("No matching files found.");
//        } else {
//            for (File file : foundFiles) {
//                resultTextArea.appendText(file.getAbsolutePath() + "\n");
//            }
//        }
//    }
}
