package com.zaremba.phosort.tools;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.apache.commons.io.FileUtils;
import org.controlsfx.dialog.ProgressDialog;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MigrateTable {
    private final DatabaseHandler handler;
    private final String tableName;
    private final String newSortPath;
    private final Task migrate;

    public MigrateTable(DatabaseHandler handler, String tableName, String newSortPath) {
        this.handler = handler;
        this.tableName = tableName;
        this.newSortPath = newSortPath;
        migrate = migrateTable();
        ProgressDialog progress = new ProgressDialog(migrate);
        progress.setContentText("Creating new working Folder");
        progress.setTitle("PhoSort");
        new Thread(migrate).start();
        progress.showAndWait();
    }

    public Task
    migrateTable(){
        return new Task(){
            @Override
            protected Object call() {
                try {
                    updateMessage("Migrating Table: " + tableName);
                    updateMessage("Moving Photos and updating database");
                    ResultSet rs = handler.execQuery("SELECT * FROM " + tableName);
                    String month;
                    String year;
                    int pos = 1;
                    int size = rs.getFetchSize();
                    while (rs.next()) {
                        String date = rs.getString("DATE");
                        String oldPath = rs.getString("LOCATION");
                        String[] splitter = date.split("-");
                        month = splitter[1];
                        year = splitter[0];
                        String monthName = getMonth(month);
                        if (monthName.equals("error")) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText("Something wrong with dates aborting process move images back to original folder: " + rs.getString("NAME"));
                            alert.showAndWait();
                            break;
                        }
                        int index = oldPath.indexOf(year + "\\" + getMonth(month));
                        String newpath = newSortPath + oldPath.substring(index - 1 );
                        File old = new File(oldPath);
                        File newFil = new File(newpath);
                        FileUtils.moveFile(old,newFil);
                        updateProgress(pos, size);
                        pos++;
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        };
    }

    private String getMonth(String month) {
        return switch (month) {
            case "01" -> "1-January";
            case "02" -> "2-February";
            case "03" -> "3-March";
            case "04" -> "4-April";
            case "05" -> "5-May";
            case "06" -> "6-June";
            case "07" -> "7-July";
            case "08" -> "8-August";
            case "09" -> "9-September";
            case "10" -> "10-October";
            case "11" -> "11-November";
            case "12" -> "12-December";
            default -> "error";
        };
    }

}
