package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import util.GuiUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.SecureRandom;
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
}
