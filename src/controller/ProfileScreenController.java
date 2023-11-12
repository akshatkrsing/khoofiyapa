package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import main.Main;
import table.ParamsTable;
import util.GuiUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProfileScreenController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    @FXML
    private MenuItem fileMenuItem;
    @FXML
    private MenuItem folderMenuItem;
    @FXML
    private MenuItem refreshMenuItem;
    @FXML
    MenuItem aesAlgoMenuItem;
    @FXML
    private MenuItem desAlgoMenuItem;
    @FXML
    private MenuItem rsaAlgoMenuItem;
    @FXML
    private Label algoLabel;
    @FXML
    private Label fileLabel;
    @FXML
    private Button encryptButton;
    @FXML
    private PasswordField privateKeyField;
    @FXML
    private PasswordField confirmPrivateKeyField;
    @FXML
    private CheckMenuItem customCheckMenuItem;
    @FXML
    private CheckMenuItem generatedCheckMenuItem;
    private String rootFolderPath;
    private BooleanProperty listViewVisible;
    private Connection connection;
    @FXML
    private Button rootFolderBrowseButton;
    @FXML
    private TextField rootFolderPathField;
    @FXML
    private Button applyRootFolderPathButton;
    @FXML
    private Button confirmRootFolderPathButton;
    public void first(){
        searchField.setPromptText("Enter search term");
        connection = Main.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(ParamsTable.QUERY_FETCH_ROOT_FOLDER_PATH);
            preparedStatement.setString(1,"rootFolderPath");
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            rootFolderPath = resultSet.getString(ParamsTable.COLUMN_PARAM_VALUE);
        } catch (SQLException e) {

            e.printStackTrace();
        }
        if(rootFolderPath == null){
            // Documents directory as default
            rootFolderPath = System.getProperty("user.home") + "\\Documents";
            System.out.println("Document Directory: " + rootFolderPath);
            FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("../fxml/setPathDialogFXML.fxml"));
            // open dialog

            applyRootFolderPathButton.setDisable(true);
            rootFolderPathField.setText(rootFolderPath);
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
                if(rootFolderPathField.getText() != rootFolderPath) applyRootFolderPathButton.setDisable(false);
            });
            applyRootFolderPathButton.setOnAction(event -> {
                // check if folder exists

                rootFolderPath = rootFolderPathField.getText();
                applyRootFolderPathButton.setDisable(true);
            });
            confirmRootFolderPathButton.setOnAction(event -> {
                // create root folder

                // close dialog

            });
        }
        // Set up a short delay to trigger search after typing
        searchDelay.getKeyFrames().add(new KeyFrame(Duration.millis(500), event -> performSearch()));
        searchDelay.setCycleCount(1);

        searchField.setOnKeyTyped(this::handleKeyTyped);
        listViewVisible = new SimpleBooleanProperty(true);
        searchResultListView.visibleProperty().bind(listViewVisible);
        listViewToggleButton.setOnAction(event -> {
            listViewVisible.set(!listViewVisible.get());
        });
        searchResultListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN  && searchResultListView.getSelectionModel().getSelectedItem() != null) {
                searchField.setText(searchResultListView.getSelectionModel().getSelectedItem().toString());

                int selectedIndex = searchResultListView.getSelectionModel().getSelectedIndex();
                if (selectedIndex < searchResultListView.getItems().size() - 1) {
                    // Select the next item
                    searchResultListView.getSelectionModel().select(selectedIndex + 1);
                    searchResultListView.scrollTo(selectedIndex + 1);
                }
            }
            else if(event.getCode() == KeyCode.ENTER && searchResultListView.getSelectionModel().getSelectedItem() != null){
                //
            }
        });
    }
    public void refresh(){

    }


    File browsedFile;
    public File browseFile(){
        FileChooser encryptedFileChooser = new FileChooser();
        encryptedFileChooser.setInitialDirectory(new File(rootFolderPath));
        File encryptedFile = encryptedFileChooser.showSaveDialog(null);
        try {
            encryptedFile.createNewFile();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return encryptedFile;
    }
    public void encryptFileUsingAES(){
        try {
            // Generate a random AES key
            assert browsedFile != null;
            File encryptedFile = browseFile();

            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();

            // Create a random IV (Initialization Vector)
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // Initialize the AES cipher for encryption
            Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

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
    public void encryptFileUsingTripleDES(){
        try {
            assert browsedFile != null;
            File encryptedFile = browseFile();

            // Generate a random 192-bit (24-byte) secret key
            SecureRandom random = new SecureRandom();
            byte[] keyData = new byte[24];
            random.nextBytes(keyData);
            SecretKey key = new SecretKeySpec(keyData, "DESede");

            // Generate an initialization vector (IV)
            byte[] iv = new byte[8];
            random.nextBytes(iv);

            // Initialize the cipher with the key and IV in encryption mode
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            // Create input and output streams
            FileInputStream inputStream = new FileInputStream(browsedFile);
            FileOutputStream outputStream = new FileOutputStream(encryptedFile);

            // Write the IV to the output file
            outputStream.write(iv);

            // Create a buffer for data encryption
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] encryptedBytes = cipher.update(buffer, 0, bytesRead);
                outputStream.write(encryptedBytes);
            }

            byte[] finalEncryptedBytes = cipher.doFinal();
            outputStream.write(finalEncryptedBytes);

            inputStream.close();
            outputStream.close();

            System.out.println("File encrypted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void encryptFileUsingRSA(){
        try {
            assert browsedFile != null;
            File encryptedFile = browseFile();
            // Generate RSA key pair (public and private key)
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Key size
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            FileInputStream inputStream = new FileInputStream(browsedFile);
            FileOutputStream outputStream = new FileOutputStream(encryptedFile);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }

            cipherOutputStream.close();
            inputStream.close();
            outputStream.close();

            System.out.println("File encrypted successfully.");
        } catch (Exception e) {
            //
            e.printStackTrace();
        }
    }
    public void browseFolder(){

    }
    public void encryptFolder(){

    }

    @FXML
    private TextField searchField;

    @FXML
    private FlowPane searchResultFlowPane;
    @FXML
    private ListView searchResultListView;

    @FXML
    private Button listViewToggleButton;

    private String searchDirectory = "C:\\Users\\Akshat\\Downloads\\Transport Company"; // Change to the directory you want to search

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
            List<String> foundFiles = searchFiles(searchDirectory, searchTerm);
//            for(File file : foundFiles){
//                FileSystemView fileSystemView = FileSystemView.getFileSystemView();
//                Icon icon = fileSystemView.getSystemIcon(file);
//                BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
//                icon.paintIcon(null,bufferedImage.getGraphics(),0,0);
//                int dw = 64;
//                int dh = 64;
//                BufferedImage scaledImage = new BufferedImage(dw, dh,BufferedImage.TYPE_INT_ARGB);
//                Graphics2D g2d = scaledImage.createGraphics();
//                g2d.drawImage(bufferedImage, 0, 0, dw, dh, null);
//                g2d.dispose();
//
//                Image fxImage = SwingFXUtils.toFXImage(scaledImage,null);
//
//                ImageView imageView = new ImageView(fxImage);
//                imageView.setFitWidth(64);
//                imageView.setFitHeight(64);
//                imageView.setPreserveRatio(true);
//                imageView.setSmooth(true);
//                imageView.setCache(true);
//                searchResultFlowPane.getChildren().add(imageView);

//            }
//            displaySearchResults(foundFiles);
            ObservableList<String> observableList = FXCollections.observableArrayList(foundFiles);
            searchResultListView.setItems(observableList);
        } else {
           //
        }
    }

    private List<String> searchFiles(String directory, String searchQuery) {
        List<String> foundFiles = new ArrayList<>();
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
                        foundFiles.add(file.getAbsolutePath());
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
