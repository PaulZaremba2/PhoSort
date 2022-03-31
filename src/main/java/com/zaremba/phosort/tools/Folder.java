/**
 * Contains information about a folder to be sorted.
 * It has the location, list of photos,
 * VBox for its thumbnail to be selected,
 */

package com.zaremba.phosort.tools;

import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Folder {
    private String name;
    private final File location;
    private final ArrayList<Photo> photos;
    private final VBox box;
    private final DatabaseHandler handler;

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

    public ArrayList<Photo> getPhotos() {
        return photos;
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
            return other.location.equals(this.location);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Folder: " + name;
    }
}
