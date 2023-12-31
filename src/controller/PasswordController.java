package controller;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import main.Main;
import table.ParamsTable;
import util.HashUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordController {
    @FXML
    Label wrongPasswordLabel;
    private Connection connection;


    //verifying entered password
    @FXML
    private PasswordField passwordVerifyField;
    public boolean verifyPassword() {
        String enteredPassword = passwordVerifyField.getText();
        if(enteredPassword==null) {
            System.out.println("true");
            return false;
        }
        connection = Main.getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(ParamsTable.QUERY_VERIFY);
            preparedStatement.setString(1,"password");
            preparedStatement.setString(2,  HashUtil.getMd5(passwordVerifyField.getText()));
            System.out.println(preparedStatement);
            ResultSet resultSet=preparedStatement.executeQuery();

            if(resultSet.next()){
                System.out.println("true");
                return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
