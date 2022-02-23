package com.zaremba.phosort.tools;

import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Project {
    ArrayList<Photo> photos;
    String projectName;
    VBox box;
    DatabaseHandler handler;

    public Project(String projectName, VBox box) {
        photos = new ArrayList<>();
        this.projectName = projectName;
        this.box = box;
        handler = DatabaseHandler.getHandler();
        addPhotos();
    }
    public Project(String projectName) {
        photos = new ArrayList<>();
        this.projectName = projectName;
        handler = DatabaseHandler.getHandler();
        addPhotos();
    }
    public String getProjectName() {
        return projectName;
    }
    private void addPhotos() {
        ResultSet rs = handler.execQuery("SELECT * FROM " + projectName);
        try{
            while (rs.next()) {
                File location = new File(rs.getString("FILELOCATION"));
                String date = rs.getString("DATE");
                String rotation = rs.getString("ROTATION");
                String status = rs.getString("STATUS");
                photos.add(new Photo(location, date, rotation, projectName, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public VBox getBox() {
        return box;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }
}
