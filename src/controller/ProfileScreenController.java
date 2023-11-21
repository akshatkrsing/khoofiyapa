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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Main;
import table.ParamsTable;
import util.FileIconUtil;
import util.GuiUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class ProfileScreenController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    @FXML
    private MenuItem fileMenuItem;
    @FXML
    private MenuItem encryptionSettingMenuItem;
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
    private Label folderPathLabel;
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
    String rootFolderPath;
    private BooleanProperty listViewVisible;
    private Connection connection;
    public void first(){
        searchField.setPromptText("Enter search term");
        connection = Main.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(ParamsTable.QUERY_FETCH_PARAM_VALUE);
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
            FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("../fxml/setRootPathDialogFXML.fxml"));
            // open dialog
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
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY,ButtonType.OK,ButtonType.CANCEL);
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
                //update param in db

                event.consume(); // Consume the event to prevent the dialog from closing
            });

            // Show the dialog and wait for a response
            Optional<ButtonType> result = dialog.showAndWait();
            // update param in db if not updated yet

            // create folder

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
        // use file wrapper
        Task<Void> rootTreeViewTask = setRootTreeViewTask();
        treeViewProgress.progressProperty().bind(rootTreeViewTask.progressProperty());

        // When the task is complete, unbind the progress bar and reset its value
        rootTreeViewTask.setOnSucceeded(event -> {
            treeViewProgress.progressProperty().unbind();
            treeViewProgress.setProgress(0.0);
            treeViewProgress.setVisible(false);
        });
        new Thread(rootTreeViewTask).start();
        //
        // Add a selection listener to the TreeView
        rootFolderTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Update the Label text when a new item is selected
            if (newValue != null) {
                folderPathLabel.setText(newValue.getValue().getFilePath());
            }
        });
        //
        browsedFileIndex = 0;
        //
        algoComboBox.setOnAction(event -> {
            // Update the Label text when a new item is selected
            String selectedItem = (String) algoComboBox.getSelectionModel().getSelectedItem();
            algorithms[browsedFileIndex] = selectedItem;
        });
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
                Platform.runLater(() -> switchToFolderItem());
                rootItem.setExpanded(true);
                folderItem.setExpanded(true);
//                folderItem = createFolderTreeItem(new File(rootFolderPath));
                return null;
            }
        };
    }
    public void refresh(){

    }


    File browsedEncryptedFile;
    public File browseEncryptedFile(){
        FileChooser encryptedFileChooser = new FileChooser();
        encryptedFileChooser.setInitialDirectory(new File(rootFolderPath));
        File encryptedFile = encryptedFileChooser.showSaveDialog(null);
        try {
            if(!encryptedFile.exists()) encryptedFile.createNewFile();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return encryptedFile;
    }
    public void encryptFileUsingAES(File browsedFile){
        try {
            // Generate a random AES key
            assert browsedFile != null;
            if(folderPathLabel.getText().equals("")){
                GuiUtil.alert(Alert.AlertType.ERROR,"Folder not selected!");
                return;
            }
            File encryptedFile = new File(folderPathLabel.getText()+"/"+browsedFile.getName());
            encryptedFile.createNewFile();
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
            System.out.println("File encrypted successfully.");
            //
            folderPathLabel.setText("");
        } catch(AssertionError e){
            GuiUtil.alert(Alert.AlertType.ERROR,"No files or folder selected!");
            e.printStackTrace();
        }catch (Exception e) {
            // add the file to error list for display in Alert
            e.printStackTrace();
        }
    }
    public void encryptFileUsingTripleDES(){
        try {
            assert browsedEncryptedFile != null;
            File encryptedFile = browseEncryptedFile();

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
            FileInputStream inputStream = new FileInputStream(browsedEncryptedFile);
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
            assert browsedEncryptedFile != null;
            File encryptedFile = browseEncryptedFile();
            // Generate RSA key pair (public and private key)
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Key size
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            FileInputStream inputStream = new FileInputStream(browsedEncryptedFile);
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
    public void encryptFile(){
        if(browsedFiles != null){
            for(int fileIndex=0;fileIndex<browsedFiles.size();fileIndex++){
                if(algorithms[fileIndex].equals("AES")){
                    encryptFileUsingAES(browsedFiles.get(fileIndex));
                }
                else if(algorithms[fileIndex].equals("3DES")){

                }
                else if(algorithms[fileIndex].equals("RSA")){

                }
            }
        }
    }
    public void clearFileIconImageViews(){
    }
    public void encryptFolder(){

    }
    public void setAlgorithmMenuItems(){

    }

    @FXML
    private TextField searchField;

//    @FXML
//    private FlowPane searchResultFlowPane;
    @FXML
    private ListView searchResultListView;
    private String searchDirectory = "D:\\"; // Change to the directory you want to search
    @FXML
    private Button listViewToggleButton;

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
//        searchResultFlowPane.getChildren().clear();
        if (searchTerm.length() > 0) {
            List<String> foundFiles = searchFiles(rootFolderPath, searchTerm);
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
            if(observableList.size()>0){
//                listViewToggleButton.setDisable(false);
                searchResultListView.setItems(observableList);
            }
            else {
//                listViewToggleButton.setDisable(true);
                searchResultListView.getItems().clear();
            }
        } else {
//            listViewToggleButton.setDisable(true);
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
//    @FXML
//    private ScrollPane treeViewScrollPane;
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
//        filePathToTreeItemMap.put(filePath,fileTreeItem);
        return new TreeItem[]{fileTreeItem, folderTreeItem};
    }
//    private TreeItem<FileWrapper> createFolderTreeItem(File file){
//        TreeItem<FileWrapper> fileTreeItem = null;
//        if (file.isDirectory()) {
//            fileTreeItem = new TreeItem<>(new FileWrapper(file.getName(),file.getAbsolutePath()));
//            // If it's a directory, recursively add its children
//            File[] children = file.listFiles();
//            if (children != null) {
//                for (File child : children) {
//                    fileTreeItem.getChildren().add(createFileTreeItem(child));
//                }
//            }
//        }
//        return fileTreeItem;
//    }
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
    @FXML
    private Button shareDecryptedFileButton;
    @FXML
    private Button shareViaWhatsappButton;
    @FXML
    private Button shareViaMailButton;
    @FXML
    private AnchorPane shareOptionAnchorPane;
    private File selectedFile;

    public void shareViaWhatsapp(){
        try {
            assert selectedFile != null;
            // Construct the WhatsApp URL with the file path
            String whatsappUrl = "https://api.whatsapp.com/send?text=&phone=&file=" + selectedFile.toURI().toURL();

            // Open the default browser with the WhatsApp URL
            Desktop.getDesktop().browse(new URI(whatsappUrl));
        } catch(URISyntaxException e){

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void shareViaMail(){
        try{
            assert selectedFile != null;
            Desktop.getDesktop().mail(selectedFile.toURI());
        }
        catch (AssertionError | IOException e){

        }
    }
    public void toggleShareOptionAnchorPane(){
        shareOptionAnchorPane.setVisible(!shareOptionAnchorPane.isVisible());
    }
    List<File> browsedFiles;
    String[] algorithms;
    private int browsedFilesListSize;
    private int browsedFileIndex;
    @FXML
    private Button browseFilesButton;
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
        if(browsedFiles.size()>1){
            nextFileIconImageView.setImage(FileIconUtil.getFileIcon(browsedFiles.get((browsedFileIndex+1)%browsedFilesListSize)));
        }
        if(browsedFiles.size()>2){
            previousFileIconImageView.setImage(FileIconUtil.getFileIcon(browsedFiles.get((browsedFileIndex-1+browsedFilesListSize)%browsedFilesListSize)));
        }
        currentFileIconImageView.setImage(FileIconUtil.getFileIcon(browsedFiles.get(browsedFileIndex)));
        setAlgoComboBox();
    }
    File browsedFolder;
    @FXML
    private Button browseFolderButton;
    public void openFolderChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        browsedFolder = directoryChooser.showDialog(null);
    }
    @FXML
    private ImageView currentFileIconImageView;
    @FXML
    private ImageView nextFileIconImageView;
    @FXML
    private ImageView previousFileIconImageView;
    @FXML
    private Button previousIconButton;
    @FXML
    private Button nextIconButton;
    @FXML
    private ComboBox algoComboBox;
    public void nextBrowsedFile() throws FileNotFoundException {
        browsedFileIndex = (browsedFileIndex+1)%browsedFilesListSize;
        setFileIconImageViews();
        setAlgoComboBox();
    }
    public void previousBrowsedFile()throws FileNotFoundException {
        browsedFileIndex = (browsedFileIndex-1+browsedFilesListSize)%browsedFilesListSize;
        setFileIconImageViews();
        setAlgoComboBox();
    }
//    private Map<String,TreeItem<FileWrapper>> filePathToTreeItemMap;
    public void switchToRootItem(){
        if(rootItem!=null) rootFolderTreeView.setRoot(rootItem);
    }
    public void switchToFolderItem(){
        if(folderItem!=null) rootFolderTreeView.setRoot(folderItem);
    }
}
