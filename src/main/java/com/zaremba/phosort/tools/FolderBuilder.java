package com.zaremba.phosort.tools;


import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import com.zaremba.imgscalr.Scalr;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.controlsfx.dialog.ProgressDialog;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;



public class FolderBuilder {
    private final File[] fileList;
    private final ArrayList<File> images;
    private final ArrayList<File> videos;
    private final ArrayList<File> others;
    private final DatabaseHandler handler;
    Task setup;

    public Task
    setupFolder(){
        return new Task(){
            @Override
            protected Object call(){
                updateMessage("Sorting Files");
                sortFiles();
                updateMessage("Moving Videos...");
                for(int i = 0 ; i < videos.size();i++){
                    moveVideo(videos.get(i));
                    updateProgress(i+1, videos.size());
                }
                updateTitle("Adding images to database and creating thumbnails");
                for(int i = 0; i < images.size(); i ++){
                    updateMessage("Image: " + images.get(i).getName() + ": \t" + (i+1) + "/" + images.size());
                    addImageToDatabase(images.get(i));
                    updateProgress(i + 1, images.size());
                }
                return true;
            }
        };
    }
    public FolderBuilder(File folder){
        this.fileList = folder.listFiles();
        images = new ArrayList<>();
        videos = new ArrayList<>();
        others = new ArrayList<>();
        handler = DatabaseHandler.getHandler();
        setup = setupFolder();
        ProgressDialog progress = new ProgressDialog(setup);
        progress.setContentText("Creating new working Folder");
        progress.setTitle("PhoSort");
        new Thread(setup).start();
        progress.showAndWait();
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
            File out = null;
            if (date == null) {
                out = new File(path + "\\Pictures\\" + "thumbnails" +"\\" + start + "NODATE" + end);
            }
            else{
                out = new File(path + "\\Pictures\\" + "thumbnails" +"\\" + start + date + end);
            }
            System.out.println(out.getAbsolutePath());
            out.createNewFile();
            ImageIO.write(small, "jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(file.getAbsolutePath());
        }
    }
    private void moveVideo(File file){
        String deststart = Settings.videoFolder.getAbsolutePath();
        File destinationFile;
        Mp4Directory dir;
        Date date;
        SimpleDateFormat format = new SimpleDateFormat("yyyy MMMM");
        String formattedDate;
        String[] split;
        String year;
        String month;
        Metadata metadata;
        if(isMP4(file)){
            try{
                metadata = ImageMetadataReader.readMetadata(file);
                dir = metadata.getFirstDirectoryOfType(Mp4Directory.class);
                date = dir.getDate(Mp4Directory.TAG_CREATION_TIME);
                formattedDate = format.format(date);
                split = formattedDate.split(" ");
                year = split[0];
                month = split[1];
                String destination = deststart + "\\" + year + "\\" + month + "\\" + file.getName();
                destinationFile = new File(destination);
                int num = 1;
                while (destinationFile.exists()) {
                    String extension = destination.substring(destination.lastIndexOf("."));
                    destination = destination.substring(0, destination.lastIndexOf(".")) + " - copy (" + num + ")" + extension;
                    destinationFile = new File(destination);
                    num++;
                }
                FileUtils.moveFile(file,destinationFile);
            } catch (ImageProcessingException | IOException e) {
                e.printStackTrace();
            }
        }
        else {
            String destination = deststart + "\\" + file.getName();
            destinationFile = new File(destination);
            int num = 1;
            while (destinationFile.exists()) {
                String extension = destination.substring(destination.lastIndexOf("."));
                destination = destination.substring(0, destination.lastIndexOf(".")) + " - copy (" + num + ")" + extension;
                destinationFile = new File(destination);
                num++;
            }
            try {
                FileUtils.moveFile(file, destinationFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Done should be fine
    private boolean isMP4(File f) {
        String name = f.getName();
        int index = name.lastIndexOf(".");
        String type = name.substring(index+1);
        return type.equals("mp4");
    }
    //Done tested on old program
    private boolean checkDuplicatePhoto(File image) {
        Date date = null;
        try {
            date = getDate(image);
        } catch (Exception e) {
            date = new Date();
        }
        ResultSet rs = handler.execQuery("SELECT * FROM FAVOURITES");
        try{
            while (rs.next()) {
                File photo = new File(rs.getString("LOCATION"));
                if (photo.getName().equals(image.getName())) {
                    Date phoDate = getDate(photo);
                    if (date.equals(phoDate)) {
                        System.out.println("File: " + image.getName() + " already exists in favourites");
                        return true;
                    }
                }
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        rs = handler.execQuery("SELECT * FROM DELETED");
        try{
            while (rs.next()) {
                String name = rs.getString("NAME");
                if (name.equals(image.getName())) {
                    String deletedDate = rs.getString("DATE");
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    if (deletedDate.equals(sqlDate.toString())) {
                        System.out.println("File: " + image.getName() + " already exists in Deleted");
                        return true;
                    }
                }
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        rs = handler.execQuery("SELECT * FROM KEEP");
        try{
            while (rs.next()) {
                File photo = new File(rs.getString("LOCATION"));
                if (photo.getName().equals(image.getName())) {
                    Date phoDate = getDate(photo);
                    if (date.equals(phoDate)) {
                        System.out.println("File: " + image.getName() + " already exists in Keeps");
                        return true;
                    }
                }
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        rs = handler.execQuery("SELECT * FROM LIKES");
        try{
            while (rs.next()) {
                File photo = new File(rs.getString("LOCATION"));
                if (photo.getName().equals(image.getName())) {
                    Date phoDate = getDate(photo);
                    if (date.equals(phoDate)) {
                        System.out.println("File: " + image.getName() + " already exists in LIKES");
                        return true;
                    }
                }
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ArrayList<String> folderNames = new ArrayList<>();
        rs = handler.execQuery("SELECT * FROM FOLDERS");
        try {
            while (rs.next()) {
                String name = rs.getString("NAME");
                folderNames.add(name);
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        for(String fName : folderNames){
            rs = handler.execQuery("SELECT * FROM " + fName);
            try {
                while (rs.next()) {
                    File photo = new File(rs.getString("FILELOCATION"));
                    if (photo.getName().equals(image.getName())) {
                        Date phoDate = getDate(photo);
                        if(date.equals(phoDate)){
                            System.out.println("File: " + image.getName() + " already exists in " + fName);
                            return true;
                        }
                    }
                }
                rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return false;
    }
    //Done tested on old program
    private String getDateFormatted(Date date){
        if (date == null) {
            return "error";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy MMMM");
        return format.format(date);
    }
    //Done tested on old program
    private Date getDate(File image) {
        try{
            Metadata metadata = ImageMetadataReader.readMetadata(image);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
            return date;
        }catch (ImageProcessingException e) {
            e.printStackTrace();
            System.out.println("Getting Date of file:" + image.getName());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Getting Date of file:" + image.getName());
        }
        return null;
    }


    private void addImageToDatabase(File file){
        String currentDir = System.getProperty("user.dir");
        if(checkDuplicatePhoto(file)){
            String destination = Settings.duplicateFolder.getAbsolutePath() + "\\" + file.getName();
            File filedest = new File(destination);
            try {
                if (filedest.exists()) {
                    destination = Settings.deleteFolder.getAbsolutePath() + "\\" + file.getName();
                    FileUtils.moveFile(file, new File(destination));
                } else {
                    FileUtils.moveFile(file, filedest);
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }else{
            Date date;
            try {
                date = getDate(file);
            } catch (Exception e) {
                date = null;
            }
            String yearMonth = getDateFormatted(date);
            if (yearMonth.equals("error")) {
                boolean makeTable = handler.checkTableExists("NODATE");
                if (!makeTable) {
                    handler.createImageWithNoDateTable();
                    String location = currentDir + "\\" + "SortingDir" + "\\" + "NODATE";
                    String qu = "INSERT INTO FOLDERS(NAME, LOCATION) VALUES (" + "'" + "NODATE" + "'" + "," + "'" + location + "'" +")";
                    handler.execAction(qu);
                }
                File destination = new File(currentDir + "\\" + "SortingDir" + "\\" + "NODATE" + "\\" + file.getName());
                String qu = "INSERT INTO NODATE (FILENAME, FILELOCATION, STATUS, ROTATION, DATE) VALUES ('" + file.getName() + "','" + destination.getAbsolutePath() + "', 'KEEP','" + getRotation(file) + "', 'NODATE')";
                if(handler.execAction(qu)) {
                    System.out.println("Inserted Image: " + file.getName());
                    try {
                        FileUtils.moveFile(file, destination);
                    } catch (IOException e) {
                        System.out.println("File: " + file.getName() + " exists but duplicate not detected");
                    }
                    createThumbnail(destination, "NODATE");
                }
                else System.out.println("Failed to insert image " + file.getName());
                return;
            }
            java.sql.Date sqlDate = null;
            if (date != null) {
                sqlDate = new java.sql.Date(date.getTime());
                String rotation = getRotation(file);
                String[] splitter = yearMonth.split(" ");
                String tableName = splitter[1] + "_" + splitter[0];
                if(!handler.checkTableExists(tableName)){
                    handler.createImageFolderTable(tableName);
                    String location = currentDir + "\\" + "SortingDir" + "\\" + yearMonth;
                    String qu = "INSERT INTO FOLDERS(NAME, LOCATION) VALUES ('" + tableName + "','" + location + "')";
                    handler.execAction(qu);
                }
                String destination = currentDir + "\\SortingDir\\" + yearMonth + "\\" + file.getName();
                File dest = new File(destination);
                String qu = "INSERT INTO " + tableName + "(FILENAME, FILELOCATION, STATUS, ROTATION, DATE) VALUES ('" + file.getName() + "','" + destination + "', 'KEEP', '" + rotation + "', '" + sqlDate + "')";
                if(handler.execAction(qu)) {
                    System.out.println("Inserted image: " + file.getName());
                    try{
                        FileUtils.moveFile(file, dest);
                        createThumbnail(dest, sqlDate.toString());
                    } catch (IOException e) {
                        System.out.println("File: " + file.getName() + " did not move likely destination exists.");
                    }
                }
                else System.out.println("Failed to insert image:" + file.getName());
            }
        }
    }
    //Should be working
    private String getRotation(File file){
        /*try {
            IIOImage rotated = EXIFUtilities.readWithOrientation(file);
            IIOMetadata metadata = rotated.getMetadata();
            Orientation or = EXIFUtilities.findImageOrientation(metadata);
            String rotation = or.toString();
            if (rotation.equals(null)) {
                System.out.println("null");
            }
            System.out.println(rotation);
            return rotation;
        }catch(Exception e){
            return null;
        }*/
        String rotation = null;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifIFD0Directory direct = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            ExifIFD0Descriptor descriptor = new ExifIFD0Descriptor(direct);
            rotation = descriptor.getOrientationDescription();
        } catch (ImageProcessingException e) {
            rotation = "(0)";
        } catch (IOException e) {
            rotation = "(0)";
        } catch (Exception e) {
            rotation = "(0)";
        }
        if (rotation == null) {
            rotation = "(0)";
        }
        rotation = rotation.substring(rotation.indexOf("("));
        System.out.println(rotation);
        return rotation;
    }
    //Done tested on old program
    private void sortFiles() {
        if(fileList.length == 0){
            return;
        }else{
            String extension = "";
            for(File f : fileList){
                String name = f.getName();
                int i = name.lastIndexOf(".");
                if(i >= 0){
                    extension = name.substring(i + 1);
                    if(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("JPG")){
                        images.add(f);
                    }else if(extension.equals("avi") || extension.equals("mp4") || extension.equals("mov")){
                        videos.add(f);
                    }else{
                        others.add(f);
                    }
                }
            }
        }
    }
    //Done tested on old program
    private boolean checkIfImage(String fileName){
        String extension = "";
        int i = fileName.lastIndexOf(".");
        if( i >= 0) {
            extension = fileName.substring(i+1);
            extension.toLowerCase();
            if(extension.equals("JPG")||extension.equals("jpg")||extension.equals("png")||extension.equals("jpeg")){
                return true;
            }
        }
        return false;
    }
}
