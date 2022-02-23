package com.zaremba.phosort.ui;

import com.zaremba.phosort.tools.DatabaseHandler;
import com.zaremba.phosort.tools.Settings;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SettingsWindow implements Initializable {

    public Label sortFolder;
    public Label deleteFolder;
    public Label videoFolder;
    public Label duplicateFolder;
    public Label userPictureFolder;
    public DatabaseHandler handler;
    public VBox root;
    public Stage primaryStage;

    public void updateSort() {
        DirectoryChooser chooser = new DirectoryChooser();
        /*System.out.println(userPictureFolder.getText());
        File file2 = new File(userPictureFolder.getText());
        System.out.println(file2.exists());
        chooser.setInitialDirectory(new File(sortFolder.getText()));*/
        File file = chooser.showDialog(primaryStage);
        handler.execUpdate("UPDATE SETTINGS SET VALUE = '" + file.getAbsolutePath() + "' WHERE VARIABLE = 'sortFolder'");
        Settings.sortFolder = file;
        setLabels();
    }

    public void updateDelete() {
        DirectoryChooser chooser = new DirectoryChooser();
        //chooser.setInitialDirectory(new File(userPictureFolder.getText()));
        File file = chooser.showDialog(primaryStage);
        handler.execUpdate("UPDATE SETTINGS SET VALUE = '" + file.getAbsolutePath() + "' WHERE VARIABLE = 'deleteFolder'");
        Settings.deleteFolder = file;
        setLabels();
    }

    public void updateVideo() {
        DirectoryChooser chooser = new DirectoryChooser();
        //chooser.setInitialDirectory(new File(videoFolder.getText()));
        File file = chooser.showDialog(primaryStage);
        handler.execUpdate("UPDATE SETTINGS SET VALUE = '" + file.getAbsolutePath() + "' WHERE VARIABLE = 'videoFolder'");
        Settings.videoFolder = file;
        setLabels();
    }

    public void updateDupes() {
        DirectoryChooser chooser = new DirectoryChooser();
        //chooser.setInitialDirectory(new File(userPictureFolder.getText()));
        File file = chooser.showDialog(primaryStage);
        handler.execUpdate("UPDATE SETTINGS SET VALUE = '" + file.getAbsolutePath() + "' WHERE VARIABLE = 'duplicateFolder'");
        Settings.duplicateFolder = file;
        setLabels();
    }

    public void updatePicutre() {
        DirectoryChooser chooser = new DirectoryChooser();
        //chooser.setInitialDirectory(new File(userPictureFolder.getText()));
        File file = chooser.showDialog(primaryStage);
        handler.execUpdate("UPDATE SETTINGS SET VALUE = '" + file.getAbsolutePath() + "' WHERE VARIABLE = 'userPictureFolder'");
        Settings.userPictureFolder = file;
        setLabels();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> primaryStage = (Stage)root.getScene().getWindow());
        handler = DatabaseHandler.getHandler();
        setLabels();
    }

    private void setLabels() {
        ResultSet rs = handler.execQuery("SELECT * FROM SETTINGS");
        try{
            System.out.println(rs.getFetchSize());
            int i = 0;
            while(rs.next()){
                switch(i){
                    case 0:
                        sortFolder.setText(rs.getString("VALUE"));
                    case 1:
                        deleteFolder.setText(rs.getString("VALUE"));
                    case 2:
                        videoFolder.setText(rs.getString("VALUE"));
                    case 3:
                        duplicateFolder.setText(rs.getString("VALUE"));
                    case 4:
                        userPictureFolder.setText(rs.getString("VALUE"));
                }
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
