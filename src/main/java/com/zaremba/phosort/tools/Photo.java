package com.zaremba.phosort.tools;

import com.zaremba.imgscalr.Scalr;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Date;

public class Photo {
    private File file;
    private String date;
    private String rotation;
    private String table;
    private String status;
    private String name;
    private File thumbnail;
    private VBox box;

    public File getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(File thumbnail) {
        this.thumbnail = thumbnail;
    }

    public VBox getBox() {
        return box;
    }

    public void setBox(VBox box) {
        this.box = box;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Photo(File file, String date, String rotation, String table, String status){
        this.file = file;
        this.date = date;
        this.rotation = rotation;
        this.table = table;
        this.status = status;
        name = file.getName();
        String path = System.getProperty("user.home");
        String start = file.getName().substring(0,file.getName().lastIndexOf("."));
        String end = file.getName().substring(file.getName().lastIndexOf("."));
        thumbnail = new File(path + "\\Pictures\\" + "thumbnails" +"\\" + start + date + end);
    }

    private void createThumbnail(File file, String date) {
        try {
            BufferedImage image = ImageIO.read(file);
            BufferedImage small = Scalr.resize(image, Scalr.Method.SPEED, 150, 150, Scalr.OP_ANTIALIAS);
            String path = System.getProperty("user.home");
            String start = file.getName().substring(0,file.getName().lastIndexOf("."));
            String end = file.getName().substring(file.getName().lastIndexOf("."));
            File directory = new File(path + "\\Pictures\\" + "thumbnails");
            if (!directory.exists()) {
                FileUtils.forceMkdir(directory);
            }
            File out = new File(path + "\\Pictures\\" + "thumbnails" +"\\" + start + date + end);
            System.out.println(out.getAbsolutePath());
            out.createNewFile();
            ImageIO.write(small, "jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(file.getAbsolutePath());
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return file.getAbsolutePath().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Photo){
            Photo other = (Photo)obj;
            if(other.file.equals(this.file)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
