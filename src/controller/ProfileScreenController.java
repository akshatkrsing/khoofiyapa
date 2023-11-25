package controller;

import entity.FileWrapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Main;
import table.HistoriesTable;
import table.ParamsTable;
import table.SecretsTable;
import util.ExtensionUtil;
import util.FileIconUtil;
import util.GuiUtil;
import util.HashUtil;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.*;

public class ProfileScreenController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    @FXML
    MenuItem aesAlgoMenuItem;
    @FXML
    private Label folderPathLabel;
    @FXML
    private Button encryptButton;
    @FXML
    private VBox historyContainer;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Label historyAlgoLabel;
    @FXML
    private Label timeStampLabel;
    @FXML
    private Label  actionLabel;


    String rootFolderPath;
    private BooleanProperty listViewVisible;
    private Connection connection;

    public void first() throws IOException {
        searchField.setPromptText("Enter search term");
        connection = Main.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(ParamsTable.QUERY_FETCH_PARAM_VALUE);
            preparedStatement.setString(1, "rootFolderPath");
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            rootFolderPath = resultSet.getString(ParamsTable.COLUMN_PARAM_VALUE);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (rootFolderPath == null) {

            // Documents directory as default
            rootFolderPath = System.getProperty("user.home");
            System.out.println("Document Directory: " + rootFolderPath);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/setRootPathDialogFXML.fxml"));

            // open set path dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Custom Dialog");
            Stage dialogStage = (Stage) encryptButton.getScene().getWindow();
            dialog.initOwner(dialogStage);
            Region content = null;
            try {
                content = fxmlLoader.load();
            } catch (IOException e) {
                System.out.println("Exception in ProfileScreenController while loading dialog region!");
                e.printStackTrace();
            }
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);
            SetRootPathDialogController setRootPathDialogController = fxmlLoader.getController();
            setRootPathDialogController.first();
            setRootPathDialogController.rootFolderPathField.setText(rootFolderPath);
            ButtonType applyButtonType = dialog.getDialogPane().getButtonTypes().stream()
                    .filter(buttonType -> buttonType.getButtonData() == ButtonType.APPLY.getButtonData())
                    .findFirst()
                    .orElse(null);
            if (applyButtonType != null) {
                dialog.getDialogPane().lookupButton(applyButtonType).setDisable(true);
            }
            dialog.getDialogPane().lookupButton(applyButtonType).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                System.out.println("Apply button clicked");
                // check if folder exists
                // Perform actions or validations here
                rootFolderPath = setRootPathDialogController.rootFolderPathField.getText();
                if (applyButtonType != null) {
                    dialog.getDialogPane().lookupButton(applyButtonType).setDisable(true);
                }

                event.consume(); // Consume the event to prevent the dialog from closing
            });

            // Show the dialog and wait for a response
            Optional<ButtonType> result = dialog.showAndWait();
            // update param in db if not updated yet
            rootFolderPath += "\\Khoofiyapa";
            try {
                //update param in database
                PreparedStatement preparedStatement = connection.prepareStatement(ParamsTable.QUERY_UPDATE_PARAM_VALUE);
                preparedStatement.setString(1,rootFolderPath);
                preparedStatement.setString(2,"rootFolderPath");
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            // create folder
            Path folder = Paths.get(rootFolderPath);
            Files.createDirectories(folder);
        }
        // Set up a short delay to trigger search after typing
        searchDelay.getKeyFrames().add(new KeyFrame(Duration.millis(500), event -> performSearch()));
        searchDelay.setCycleCount(1);

        searchField.setOnKeyTyped(this::handleKeyTyped);
        listViewVisible = new SimpleBooleanProperty(false);
        searchResultListView.visibleProperty().bind(listViewVisible);
        listViewToggleButton.setOnAction(event -> {
            listViewVisible.set(!listViewVisible.get());
        });
//        listViewToggleButton.setDisable(true);
        searchResultListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN && searchResultListView.getSelectionModel().getSelectedItem() != null) {
                searchField.setText(searchResultListView.getSelectionModel().getSelectedItem().toString());

                int selectedIndex = searchResultListView.getSelectionModel().getSelectedIndex();
                if (selectedIndex < searchResultListView.getItems().size() - 1) {
                    // Select the next item
                    searchResultListView.getSelectionModel().select(selectedIndex + 1);
                    searchResultListView.scrollTo(selectedIndex + 1);
                }
            } else if (event.getCode() == KeyCode.ENTER && searchResultListView.getSelectionModel().getSelectedItem() != null) {
                encryptedFileLabel.setText(searchResultListView.getSelectionModel().getSelectedItem().toString());
            }
        });
        // use file wrapper
        // set tree view
        refresh();
        //
        // Add a selection listener to the TreeView
        rootFolderTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Update the Label text when a new item is selected
            if (newValue != null) {
                if(encryptTab.isSelected()) folderPathLabel.setText(newValue.getValue().getFilePath());
                if(decryptTab.isSelected()) encryptedFileLabel.setText(newValue.getValue().getFilePath());
            }
        });
        //
        browsedFileIndex = 0;
        //
        algoComboBox.setOnAction(event -> {
            // Update the Label text when a new item is selected
            if (algorithms != null) {
                String selectedItem = (String) algoComboBox.getSelectionModel().getSelectedItem();
                algorithms[browsedFileIndex] = selectedItem;
            }
        });
        fillAlgoComboBox();

    }
    @FXML
    private ProgressIndicator treeViewProgress;
    private TreeItem<FileWrapper> rootItem;
    private TreeItem<FileWrapper> folderItem;
    private Task<Void> setRootTreeViewTask(){
        return new Task<>(){
            @Override
            protected Void call() throws Exception {
                TreeItem<FileWrapper>[] result = createFileTreeItem(new File(rootFolderPath));
                rootItem = result[0];
                folderItem = result[1];
                Platform.runLater(() -> {
                    if(encryptTab.isSelected()) switchToFolderItem();
                    else switchToRootItem();
                });
                rootItem.setExpanded(true);
                folderItem.setExpanded(true);
                return null;
            }
        };
    }
    public void refresh(){
        rootFolderTreeView.setRoot(null);
        treeViewProgress.setVisible(true);
        rootItem = null;
        folderItem = null;
        Task<Void> rootTreeViewTask = setRootTreeViewTask();
        treeViewProgress.progressProperty().bind(rootTreeViewTask.progressProperty());

        // When the task is complete, unbind the progress bar and reset its value
        rootTreeViewTask.setOnSucceeded(event -> {
            treeViewProgress.progressProperty().unbind();
            treeViewProgress.setProgress(0.0);
            treeViewProgress.setVisible(false);
        });
        new Thread(rootTreeViewTask).start();

        setHistory();
    }
    private void setHistory(){
        String filename = null;
        Timestamp timeUpdated;
        String action;
        String algo;
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(HistoriesTable.QUERY_INSERT_INTO_HISTORIES_TABLE);
            System.out.println(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(HistoriesTable.QUERY_RETRIEVE_FROM_HISTORIES_TABLE);

            System.out.println(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                filename = resultSet.getString(SecretsTable.COLUMN_FILE_NAME);
                timeUpdated=resultSet.getTimestamp(HistoriesTable.COLUMN_ACTION_TIME);
                action=resultSet.getString(HistoriesTable.COLUMN_ACTION_TYPE);
                algo=resultSet.getString(SecretsTable.COLUMN_FILE_ENCRYPTION);
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/historyCardFXML.fxml"));
                try {
                    Node node = fxmlLoader.load();
                    ProfileScreenController historyCardLayoutController = fxmlLoader.getController();

                    historyCardLayoutController.fileNameLabel.setText(filename);
                    historyCardLayoutController.timeStampLabel.setText(timeUpdated.toString());
                    historyCardLayoutController.actionLabel.setText(action);
                    historyCardLayoutController.historyAlgoLabel.setText(algo);

                    historyContainer.getChildren().add(node);


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void encryptFileUsingAES(File browsedFile, String folderPath){
        try {
            // Generate a random AES key
            if(browsedFile == null) return;
            File encryptedFile = new File(folderPath+"\\"+browsedFile.getName());
            encryptedFile.createNewFile();
//            byte[] ivBytes = generateRandomIV();
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            System.out.println("Secret key: "+secretKey);
            byte[] keyBytes = secretKeyToByteArray(secretKey);
            String fileExtension = ExtensionUtil.getExtension(encryptedFile);
            //  Insert data to secrets Table
            // Initialize the AES cipher for encryption
            Cipher encryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);

            FileInputStream fis= new FileInputStream(browsedFile);
            FileOutputStream fos = new FileOutputStream(encryptedFile);
            byte[] inputBytes = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(inputBytes)) != -1) {
                byte[] outputBytes = encryptCipher.update(inputBytes, 0, bytesRead);
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
            }

            // Write the final block of encrypted data
            byte[] finalOutputBytes = encryptCipher.doFinal();
            if (finalOutputBytes != null) {
                fos.write(finalOutputBytes);
                System.out.println("File encrypted successfully using AES.");
            }
            insertToSecretsTable(encryptedFile,keyBytes,fileExtension,folderPath,"AES");
            storeHistory(encryptedFile.getAbsolutePath(),"encrypt");
        }catch (Exception e) {
            // add the file to error list for display in Alert
            e.printStackTrace();
        }
    }
    public void encryptFileUsingTripleDES(File browsedFile, String folderPath){
        try {
            if(browsedFile == null) return;
            File encryptedFile = new File(folderPath+"\\"+browsedFile.getName());
            encryptedFile.createNewFile();

            // Generate a random 192-bit (24-byte) secret key
            SecureRandom random = new SecureRandom();
            byte[] keyData = new byte[24];
            random.nextBytes(keyData);
            SecretKey secretKey = new SecretKeySpec(keyData, "DESede");
            byte[] keyBytes = secretKeyToByteArray(secretKey);
            String fileExtension = ExtensionUtil.getExtension(browsedFile);

            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Create input and output streams
            FileInputStream fis = new FileInputStream(browsedFile);
            FileOutputStream fos = new FileOutputStream(encryptedFile);

            byte[] inputBytes = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(inputBytes)) != -1) {
                byte[] outputBytes = cipher.update(inputBytes, 0, bytesRead);
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
            }

            // Write the final block of encrypted data
            byte[] finalOutputBytes = cipher.doFinal();
            if (finalOutputBytes != null) {
                fos.write(finalOutputBytes);
                System.out.println("File encrypted successfully using 3DES.");
            }
            insertToSecretsTable(encryptedFile,keyBytes,fileExtension,folderPath,"3DES");
            storeHistory(encryptedFile.getAbsolutePath(),"encrypt");
        }catch (Exception e) {
            // add the file to error list for display in Alert
            e.printStackTrace();
        }
    }
    public void encryptFileUsingRSA(File browsedFile, String folderPath){
        try {

            File encryptedFile = new File(folderPathLabel.getText()+"\\"+browsedFile.getName());
            encryptedFile.createNewFile();
            // Generate RSA key pair (public and private key)
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Key size
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            byte[] keyBytes = privateKeyToByteArray(privateKey);
            String fileExtension = ExtensionUtil.getExtension(browsedFile);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            FileInputStream fis = new FileInputStream(browsedFile);
            FileOutputStream fos = new FileOutputStream(encryptedFile);
            byte[] inputBytes = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(inputBytes)) != -1) {
                byte[] outputBytes = cipher.update(inputBytes, 0, bytesRead);
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
            }

            // Write the final block of encrypted data
            byte[] finalOutputBytes = cipher.doFinal();
            if (finalOutputBytes != null) {
                fos.write(finalOutputBytes);
                System.out.println("File encrypted successfully using RSA.");
            }
            insertToSecretsTable(encryptedFile,keyBytes,fileExtension,folderPath,"RSA");
            storeHistory(encryptedFile.getAbsolutePath(),"encrypt");
        }catch (Exception e) {
            // add the file to error list for display in Alert
            e.printStackTrace();
        }

        System.out.println("File encrypted successfully.");
    }
    public void encryptFile() throws IOException {
        if(folderPathLabel.getText().equals("")){
            GuiUtil.alert(Alert.AlertType.ERROR,"Folder not selected!");
            return;
        }
        if(!passAction()) return;
        if(browsedFiles.get(browsedFileIndex).isDirectory()){
            encryptFolder(browsedFiles.get(browsedFileIndex),folderPathLabel.getText(),algorithms[browsedFileIndex]);
        }
        else{
            if(browsedFiles != null){
                for(int fileIndex=0;fileIndex<browsedFiles.size();fileIndex++){
                    if(algorithms[fileIndex].equals("AES")){
                        encryptFileUsingAES(browsedFiles.get(fileIndex), folderPathLabel.getText());
                    }
                    else if(algorithms[fileIndex].equals("3DES")){
                        encryptFileUsingTripleDES(browsedFiles.get(fileIndex), folderPathLabel.getText());
                    }
                    else if(algorithms[fileIndex].equals("RSA")){
                        encryptFileUsingRSA(browsedFiles.get(fileIndex), folderPathLabel.getText());
                    }
//                    if(deleteOriginalFileCheckMenuItem.isSelected()) delete(browsedFiles.get(fileIndex).getAbsolutePath());
                }
            }
        }
        clearFileIconImageViews();
        folderPathLabel.setText("");
        algoComboBox.setValue("");
        browsedFiles = null;
        algorithms = null;
    }
    public void clearFileIconImageViews(){
        currentFileIconImageView.setImage(null);
        previousFileIconImageView.setImage(null);
        nextFileIconImageView.setImage(null);
    }
    public void encryptFolder(File file, String absolutePath, String algorithm) throws IOException {
        if(file.isDirectory()){
            Path folder = Paths.get(absolutePath+"\\"+file.getName());
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
                File[] children = file.listFiles();
                if(children != null){
                    for(File child : children){
                        encryptFolder(child,absolutePath+"\\"+file.getName(),algorithm);
                    }
                }
            }
        }
        else{
            if(algorithm.equals("AES")){
                encryptFileUsingAES(file,absolutePath);
            }
            else if(algorithm.equals("3DES")){
                encryptFileUsingTripleDES(file,absolutePath);
            }
            else if(algorithm.equals("RSA")){
                encryptFileUsingRSA(file,absolutePath);
            }
//            if(deleteOriginalFileCheckMenuItem.isSelected()) delete(file.getAbsolutePath());
        }
    }
    // Decrypt
    public static byte[] secretKeyToByteArray(SecretKey secretKey) {
        // Extract the encoded form of the SecretKey
        return secretKey.getEncoded();
    }
    public static byte[] privateKeyToByteArray(PrivateKey privateKey) throws Exception {
        // Get the encoded form of the private key using PKCS8EncodedKeySpec
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKey.getEncoded());

        // Use KeyFactory to generate the private key from the encoded key specification
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Replace "RSA" with your key algorithm
        PrivateKey reconstructedPrivateKey = keyFactory.generatePrivate(spec);

        // Return the byte array representation of the private key
        return reconstructedPrivateKey.getEncoded();
    }
    public static SecretKey byteArrayToAESSecretKey(byte[] keyBytes) {
        // Convert the byte array to a SecretKey using SecretKeySpec
        return new SecretKeySpec(keyBytes, "AES");
    }
    public static SecretKey byteArrayToDESSecretKey(byte[] keyBytes){
        // Create a DESKeySpec from the byte array
        return new SecretKeySpec(keyBytes, "DESede");
    }
    public static PrivateKey byteArrayToPrivateKey(byte[] privateKeyBytes) throws Exception {
        // Use KeyFactory to generate the private key from the encoded key specification
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Replace "RSA" with your key algorithm
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory.generatePrivate(spec);
    }
    public void decryptFileUsingAES(File inputFile, File outputFile, byte[] secretKeyByteArray) throws Exception {
        SecretKey key = byteArrayToAESSecretKey(secretKeyByteArray);
        // Initialize the Cipher in decryption mode
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,key);

        // Create a CipherInputStream to read the encrypted file
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] inputBytes = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(inputBytes)) != -1) {
                byte[] outputBytes = cipher.update(inputBytes, 0, bytesRead);
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
            }

            // Write the final block of decrypted data
            byte[] finalOutputBytes = cipher.doFinal();
            if (finalOutputBytes != null) {
                fos.write(finalOutputBytes);
            }
        }
        storeHistory(inputFile.getAbsolutePath(),"decrypt");
        System.out.println("File Decryted Successfully using AES");
    }

    public void decryptFileUsingTripleDES(File inputFile, File outputFile, byte[] secretKeyByteArray) throws Exception {
        SecretKey key = byteArrayToDESSecretKey(secretKeyByteArray);
        // Initialize the Cipher in decryption mode
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,key );

        // Create a CipherInputStream to read the encrypted file
        try (FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] inputBytes = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(inputBytes)) != -1) {
                byte[] outputBytes = cipher.update(inputBytes, 0, bytesRead);
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
            }

            // Write the final block of decrypted data
            byte[] finalOutputBytes = cipher.doFinal();
            if (finalOutputBytes != null) {
                fos.write(finalOutputBytes);
            }
        }
        storeHistory(inputFile.getAbsolutePath(),"decrypt");
        System.out.println("File Decrypted Successfully using 3DES");
    }
    public void decryptFileUsingRSA(File inputFile, File outputFile, byte[] secretKeyByteArray) throws Exception {
        PrivateKey key = byteArrayToPrivateKey(secretKeyByteArray);
        // Initialize the Cipher in decryption mode
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE,key);
        // Create a CipherInputStream to read the encrypted file
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] inputBytes = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(inputBytes)) != -1) {
                byte[] outputBytes = cipher.update(inputBytes, 0, bytesRead);
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
            }

            // Write the final block of decrypted data
            byte[] finalOutputBytes = cipher.doFinal();
            if (finalOutputBytes != null) {
                fos.write(finalOutputBytes);
            }
        }
        storeHistory(inputFile.getAbsolutePath(),"decrypt");
        System.out.println("File Decrypted Successfuly using RSA");
    }
    @FXML
    private Label encryptedFileLabel;
    public void decrypt() throws Exception {
        if(encryptedFileLabel.getText().equals("")){
            GuiUtil.alert(Alert.AlertType.ERROR,"No file selected!");
            return;
        }
        if(!passAction()) return;
        File file = new File(encryptedFileLabel.getText());
        if(!file.isDirectory()){
            decryptFile(file);
        }else{
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(null);
            decryptFolder(file,selectedDirectory.getAbsolutePath());
        }
        encryptedFileLabel.setText("");
    }
    public void decryptFile(File file) throws Exception {

        String filePath = file.getAbsolutePath();

        PreparedStatement preparedStatement = connection.prepareStatement(SecretsTable.QUERY_FETCH_KEY);
        preparedStatement.setString(1, filePath);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            Blob blob = resultSet.getBlob(SecretsTable.COLUMN_FILE_SECRET_KEY);
            int blobLength = (int) blob.length();
            byte[] decryptKey = blob.getBytes(1, blobLength);
            blob.free();
            String encryptionAlgo = resultSet.getString(SecretsTable.COLUMN_FILE_ENCRYPTION);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(file.getName());
            File outputFile = fileChooser.showSaveDialog(null);
            fileChooser.setTitle("Save File");
            if(outputFile == null){
                System.out.println("File not decrypted!");
                return;
            }
            outputFile.createNewFile();
            if(encryptionAlgo.equals("AES")){
                decryptFileUsingAES(file,outputFile,decryptKey);
            } else if (encryptionAlgo.equals("3DES")) {
                decryptFileUsingTripleDES(file,outputFile,decryptKey);
            }
            else{
                decryptFileUsingRSA(file,outputFile,decryptKey);
            }
        }
    }

    private void decryptFolder(File file, String destination) throws Exception {
        if (file.isDirectory()) {
            Path folder = Paths.get(destination + "\\" + file.getName());
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
                File[] children = file.listFiles();
                if (children != null) {
                    for (File child : children) {
                        decryptFolder(child,destination + "\\" + file.getName());
                    }
                }
            }
        } else {
            PreparedStatement preparedStatement = connection.prepareStatement(SecretsTable.QUERY_FETCH_KEY);
            preparedStatement.setString(1, file.getAbsolutePath());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Blob blob = resultSet.getBlob(SecretsTable.COLUMN_FILE_SECRET_KEY);
                int blobLength = (int) blob.length();
                byte[] decryptKey = blob.getBytes(1, blobLength);
                blob.free();
                String encryptionAlgo = resultSet.getString(SecretsTable.COLUMN_FILE_ENCRYPTION);
                File outputFile = new File(destination+"\\"+file.getName());
                outputFile.createNewFile();
                if(encryptionAlgo.equals("AES")){
                    decryptFileUsingAES(file,outputFile,decryptKey);
                } else if (encryptionAlgo.equals("3DES")) {
                    decryptFileUsingTripleDES(file,outputFile,decryptKey);
                }
                else{
                    decryptFileUsingRSA(file,outputFile,decryptKey);
                }
            }
        }
    }

    @FXML
    private TextField searchField;
    @FXML
    private ListView searchResultListView;
    @FXML
    private Button listViewToggleButton;

    private Timeline searchDelay = new Timeline();


    private void handleKeyTyped(KeyEvent event) {
        // Reset the timer on each key typed
        searchDelay.stop();
        searchDelay.setCycleCount(1);
        searchDelay.play();
    }
    @FXML
    private void performSearch() {
        String searchTerm = searchField.getText();
        if (searchTerm.length() > 0) {
            List<String> foundFiles = searchFiles(rootFolderPath, searchTerm);
            ObservableList<String> observableList = FXCollections.observableArrayList(foundFiles);
            if(observableList.size()>0){
                searchResultListView.setItems(observableList);
            }
            else {
                searchResultListView.getItems().clear();
            }
        } else {
            searchResultListView.getItems().clear();
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
                if (file.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                    foundFiles.add(file.getAbsolutePath());
                }
                if (file.isDirectory()) {
                    foundFiles.addAll(searchFiles(file.getAbsolutePath(), searchQuery));
                }
            }
        }
        return foundFiles;
    }
    @FXML
    private TreeView<FileWrapper> rootFolderTreeView;
    private TreeItem<FileWrapper>[] createFileTreeItem(File file){
        String fileName = file.getName();
        String filePath = file.getAbsolutePath();
        TreeItem<FileWrapper> fileTreeItem = new TreeItem<>(new FileWrapper(fileName,filePath));
        TreeItem<FileWrapper> folderTreeItem = null;
        if (file.isDirectory()) {
            // If it's a directory, recursively add its children
            folderTreeItem = new TreeItem<>(new FileWrapper(fileName,filePath));
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    TreeItem<FileWrapper>[] result = createFileTreeItem(child);
                    fileTreeItem.getChildren().add(result[0]);
                    if(result[1] != null) folderTreeItem.getChildren().add(result[1]);
                }
            }
        }
        return new TreeItem[]{fileTreeItem, folderTreeItem};
    }
    public void shareViaWhatsapp(){
        if(encryptedFileLabel.getText().equals("")) GuiUtil.alert(Alert.AlertType.ERROR,"No file selected!");
        File selectedFile = new File(encryptedFileLabel.getText());
        if(selectedFile.isDirectory()){
            //
            return;
        }
        try {
            // Checks

            // Construct the WhatsApp URL with the file path
            String whatsappUrl = "https://api.whatsapp.com/send?text=&phone=&file=" + selectedFile.toURI().toURL();

            // Open the default browser with the WhatsApp URL
            Desktop.getDesktop().browse(new URI(whatsappUrl));
        } catch(URISyntaxException | IOException e){
            GuiUtil.alert(Alert.AlertType.ERROR,"Some error occurred!");
        }
    }
    public void shareViaMail(){
        if(encryptedFileLabel.getText().equals("")) GuiUtil.alert(Alert.AlertType.ERROR,"No file selected!");
        File selectedFile = new File(encryptedFileLabel.getText());
        if(selectedFile.isDirectory()){
            //
            return;
        }
        try{
            Desktop.getDesktop().mail(selectedFile.toURI());
        }
        catch (IOException e){
            GuiUtil.alert(Alert.AlertType.ERROR,"Some error occurred!");
        }
    }
    List<File> browsedFiles;
    String[] algorithms;
    private int browsedFilesListSize;
    private int browsedFileIndex;
    public void openFileChooser(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        browsedFiles = fileChooser.showOpenMultipleDialog(null);
        browsedFilesListSize = browsedFiles.size();
        algorithms = new String[browsedFilesListSize];
        Arrays.fill(algorithms,"AES");
        setFileIconImageViews();
    }
    public void setAlgoComboBox(){
        algoComboBox.setValue(algorithms[browsedFileIndex]);
    }
    public void setFileIconImageViews(){
        if(browsedFiles == null) return;
        if(browsedFiles.size()>1){
            nextFileIconImageView.setImage(FileIconUtil.getFileIcon(browsedFiles.get((browsedFileIndex+1)%browsedFilesListSize)));
        }
        if(browsedFiles.size()>2){
            previousFileIconImageView.setImage(FileIconUtil.getFileIcon(browsedFiles.get((browsedFileIndex-1+browsedFilesListSize)%browsedFilesListSize)));
        }
        currentFileIconImageView.setImage(FileIconUtil.getFileIcon(browsedFiles.get(browsedFileIndex)));
        setAlgoComboBox();
    }
    public void openFolderChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        File file = directoryChooser.showDialog(null);
        if(file == null) return;
        browsedFiles = new ArrayList<>(1);
        browsedFiles.add(file);
        browsedFilesListSize = 1;
        algorithms = new String[browsedFilesListSize];
        Arrays.fill(algorithms,"AES");
        setFileIconImageViews();
    }
    @FXML
    private ImageView currentFileIconImageView;
    @FXML
    private ImageView nextFileIconImageView;
    @FXML
    private ImageView previousFileIconImageView;
    @FXML
    private ComboBox algoComboBox;
    public void fillAlgoComboBox(){
        algoComboBox.getItems().addAll("AES", "3DES", "RSA");
    }
    public void nextBrowsedFile(){
        browsedFileIndex = (browsedFileIndex+1)%browsedFilesListSize;
        setFileIconImageViews();
        setAlgoComboBox();
    }
    public void previousBrowsedFile(){
        browsedFileIndex = (browsedFileIndex-1+browsedFilesListSize)%browsedFilesListSize;
        setFileIconImageViews();
        setAlgoComboBox();
    }
    public void switchToRootItem(){
        if(rootItem!=null) rootFolderTreeView.setRoot(rootItem);
    }
    public void switchToFolderItem(){
        if(folderItem!=null) rootFolderTreeView.setRoot(folderItem);
    }
    public void insertToSecretsTable(File browsedFile, byte[] keyBytes,String fileExtension,String folderPath, String encryptionAlgorithm) throws SQLException {
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(SecretsTable.QUERY_REGISTER);
            preparedStatement.setString(1, browsedFile.getName());
            InputStream fis= new ByteArrayInputStream(keyBytes);
            preparedStatement.setBlob(2, fis);
            preparedStatement.setString(3, fileExtension);
            preparedStatement.setString(4, folderPath + "\\" + browsedFile.getName());
            preparedStatement.setString(5, encryptionAlgorithm);
            System.out.println(preparedStatement);
            preparedStatement.executeUpdate();
            System.out.println("Key saved in database!");
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private Tab encryptTab;
    @FXML
    private Tab decryptTab;
    @FXML
    private DialogPane verificationDialogPane;

    //to open password authentication dialog pane
    private boolean passAction() {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/Password.fxml"));
            System.out.println("passActionCalled");
            verificationDialogPane = fxmlLoader.load();
            PasswordController passwordController=fxmlLoader.getController();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(verificationDialogPane);
            dialog.setTitle("Password Authentication");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            ButtonType okButtonType = dialog.getDialogPane().getButtonTypes().stream()
                    .filter(buttonType -> buttonType.getButtonData() == ButtonType.OK.getButtonData())
                    .findFirst()
                    .orElse(null);
            dialog.getDialogPane().lookupButton(okButtonType).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if(!passwordController.verifyPassword()){
                    passwordController.wrongPasswordLabel.setText("Wrong password!");
                    event.consume();
                }
            });
            Optional<ButtonType> clickedButton = dialog.showAndWait();

            if(clickedButton.get()==ButtonType.OK){
                return true;
            }
            else{
                System.out.println("cancel");
                return false;
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    private PasswordField oldPasswordTextField;
    @FXML
    private PasswordField newPasswordTextField;
    @FXML
    private PasswordField confirmNewPasswordTextField;


    public void changePassword(ActionEvent actionEvent) {
        String oldPassword = oldPasswordTextField.getText();
        String newPassword = newPasswordTextField.getText();
        String confirmedNewPassword = confirmNewPasswordTextField.getText();
        if (newPassword.equals(confirmedNewPassword)) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(ParamsTable.QUERY_CHANGE_PASSWORD);
                preparedStatement.setString(1, HashUtil.getMd5(newPassword));
                preparedStatement.setString(2, "password");
                preparedStatement.setString(3, HashUtil.getMd5(oldPassword));
                int result = preparedStatement.executeUpdate();

                System.out.println("password changed");
                if (result > 0) {
                    JOptionPane.showMessageDialog(null, "Password changed successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "Some error occurred.");
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            JOptionPane.showMessageDialog(null, "New password fields don't match.");
        }
    }
    private void storeHistory(String filePath,String actionType){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(HistoriesTable.QUERY_INSERT_INTO_HISTORIES_TABLE);
            preparedStatement.setString(1,filePath);
            preparedStatement.setString(2,actionType);
            int result = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            GuiUtil.alert(Alert.AlertType.ERROR,"Could not connect to database!");
            e.printStackTrace();
        }

    }
    @FXML
    private CheckMenuItem deleteOriginalFileCheckMenuItem;
    public void deleteFile() throws IOException {
        delete(encryptedFileLabel.getText());
    }
    public void delete(String filePath) throws IOException{
        if(filePath.equals("")){
            GuiUtil.alert(Alert.AlertType.ERROR,"No file selected!");
            return;
        }
        if(!passAction()) return;
        File file = new File(filePath);
        if(file.isDirectory()){
            GuiUtil.alert(Alert.AlertType.ERROR,"Directory not empty!");
            return;
        }
        Path path = Paths.get(filePath);
        Files.delete(path);
    }
}
