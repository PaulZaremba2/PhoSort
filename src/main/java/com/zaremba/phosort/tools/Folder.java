package com.zaremba.phosort.tools;

import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Folder {
    private String name;
    private File location;
    private ArrayList<Photo> photos;
    private VBox box;
    private DatabaseHandler handler;

    public Folder(String name, File location, VBox box){
        this.name = name;
        this.location = location;
        photos = new ArrayList<>();
        this.box = box;
        handler = DatabaseHandler.getHandler();
        populatePhotos();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getLocation() {
        return location;
    }

    public void setLocation(File location) {
        this.location = location;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    public void setBox(VBox box) {
        this.box = box;
    }

    public DatabaseHandler getHandler() {
        return handler;
    }

    public void setHandler(DatabaseHandler handler) {
        this.handler = handler;
    }



    private void populatePhotos(){
        String qu = "SELECT * FROM " + name;
        ResultSet rs = handler.execQuery(qu);
        try{
            while(rs.next()){
                File file = new File(rs.getString("FILELOCATION"));
                String date = rs.getString("DATE");
                String rotation = rs.getString("ROTATION");
                String status = rs.getString("STATUS");
                photos.add(new Photo(file, date, rotation, name, status));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public VBox getBox(){
        return box;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Folder){
            Folder other = (Folder)obj;
            if (other.location.equals(this.location)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Folder: " + name;
    }
}
