package com.zaremba.phosort.ui;


import com.zaremba.imgscalr.Scalr;
import com.zaremba.phosort.tools.*;
import com.zaremba.phosort.tools.Icon;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.controlsfx.dialog.ProgressDialog;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainWindow implements Initializable {
    public MenuBar menuBar;
    public ScrollPane folderScrollPane;
    public TilePane folderTilePane;
    public ImageView mainImageView;
    public VBox root;
    public VBox vboxImageSide;
    public HBox hBox;
    public Pane imageBorderPane;
    public HBox hBoxIcons;
    public ImageView favouriteIcon;
    public ImageView likeIcon;
    public ImageView trashIcon;
    public ImageView restoreIcon;
    public ImageView finishedIcon;
    public HBox controlBox;
    public AnchorPane menuAnchor;
    public VBox asideBox;
    public AnchorPane imageAnchor;
    public DatePicker fromDate;
    public DatePicker toDate;
    public Button setDateButton;
    public Label dateLabel;
    Stage stage;
    double sceneHeight;
    double sceneWidth;
    public double width;
    DatabaseHandler handler;
    private ArrayList<Folder> folders;
    private Folder currentFolder;
    private double xOffSet = 0;
    private double yOffset = 0;
    private ArrayList<Photo> missingThumbnails;
    private ArrayList<Photo> selected;
    private boolean isPhotoView = false;
    private Photo currentPhoto;
    private ArrayList<ArrayList<Photo>> deleted;
    private ArrayList<Photo> viewingPhotos;
    private boolean isGrabMode = false;
    private boolean isSortingMode = true;
    private boolean isGrabbing = false;
    private ArrayList<Project> projects;
    private Project currentProject;
    private ArrayList<Photo> favourites;
    private ArrayList<Photo> likes;
    private ArrayList<Photo> keeps;
    int currentIndex;



    public Task
    createMissingThumbs(){
        return new Task(){
            @Override
            protected Object call(){
                for (Photo p : missingThumbnails) {
                    updateMessage("Creating thumbnail for: " + p.getName());
                    createThumbnail(p.getFile(),p.getDate());
                    updateProgress(missingThumbnails.indexOf(p),missingThumbnails.size());
                }
                missingThumbnails.clear();
                return true;
            }
        };
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater( () -> {
            stage = (Stage)root.getScene().getWindow();
            sceneHeight = root.getScene().getHeight();
            sceneWidth = root.getScene().getWidth();
            folderTilePane.setCache(true);
            folderTilePane.setCacheHint(CacheHint.SPEED);
            folderScrollPane.setFitToWidth(true);
            folderScrollPane.setFocusTraversable(false);
            folderTilePane.setHgap(10);
            folderTilePane.setVgap(10);
            mainImageView.setFitWidth(sceneWidth);
            folderTilePane.setPrefColumns(4);
            folderScrollPane.setMinWidth(525);
            imageBorderPane.setPrefHeight(vboxImageSide.getHeight() - hBoxIcons.getHeight());
            imageBorderPane.setFocusTraversable(false);
            AnchorPane.setLeftAnchor(controlBox, sceneWidth - 75);
            favouriteIcon.setFocusTraversable(false);
            vboxImageSide.setFocusTraversable(false);
            likeIcon.setFocusTraversable(false);
            trashIcon.setFocusTraversable(false);
            restoreIcon.setFocusTraversable(false);
            finishedIcon.setFocusTraversable(false);
            imageAnchor.setPrefHeight(sceneHeight - 25);
            ResizeHelper.addResizeListener(stage, controlBox);
            stage.widthProperty().addListener((observable, oldValue, newValue) -> {
                /*vboxImageSide.setPrefWidth(sceneWidth - 450);
                double width = vboxImageSide.getWidth();
                double height = vboxImageSide.getHeight();
                double hboxHeight = hBoxIcons.getHeight();
                //imageBorderPane.setPrefSize(width - (.95 * width - 40), (height - hboxHeight));
                System.out.println(imageBorderPane.getWidth());
                System.out.println(imageBorderPane.getHeight());
                mainImageView.setFitWidth(imageBorderPane.getWidth());*/
            });
            loadNoImage();
            baseScene();
            menuBar.setOnMousePressed(event -> {
                xOffSet = event.getSceneX();
                yOffset = event.getSceneY();
            });
            menuBar.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffSet);
                stage.setY(event.getScreenY() - yOffset);
            });
        });
        handler = DatabaseHandler.getHandler();
        getSettings();
        folders = new ArrayList<>();
        selected = new ArrayList<>();
        deleted = new ArrayList<>();
        viewingPhotos = new ArrayList<>();
        projects = new ArrayList<>();
        favourites = new ArrayList<>();
        likes = new ArrayList<>();
        keeps = new ArrayList<>();
        currentIndex = 0;
        toDate.setDisable(true);
        fromDate.setDisable(true);
        setDateButton.setDisable(true);
        dateLabel.setVisible(false);
    }
    /*
    Done and working
     */
    private void getSettings() {
        ResultSet rs = handler.execQuery("SELECT * FROM SETTINGS");
        try{
            while(rs.next()){
                String variable = rs.getString("VARIABLE");
                String value = rs.getString("VALUE");
                switch (variable) {
                    case "sortFolder" -> Settings.sortFolder = new File(value);
                    case "deleteFolder" -> Settings.deleteFolder = new File(value);
                    case "videoFolder" -> Settings.videoFolder = new File(value);
                    case "duplicateFolder" -> Settings.duplicateFolder = new File(value);
                    case "userPictureFolder" -> Settings.userPictureFolder = new File(value);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void baseScene() {
        if(isSortingMode) {
            isPhotoView = false;
            folderTilePane.getChildren().clear();
            root.requestFocus();
            asideBox.setPrefHeight(sceneHeight - 25);
            Image addFolder = new Image(getClass().getResourceAsStream(Icon.ADDFOLDERICON.fileName));
            VBox addFolderBox = addIcon(folderTilePane, addFolder, "New SortFolder");
            addFolderBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> addPhotoFolder());
            addFolders();
        } else if (isGrabMode) {
            isPhotoView = false;
            folderTilePane.getChildren().clear();
            root.requestFocus();
            asideBox.setPrefHeight(sceneHeight - 25);
            Image addFolder = new Image(getClass().getResourceAsStream(Icon.ADDFOLDERICON.fileName));
            VBox addFolderBox = addIcon(folderTilePane, addFolder, "New Project");
            addFolderBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> createProject());
            addProjects();
        }
    }

    private void createProject() {
        String name = "";
        do  {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Grabber");
            dialog.setHeaderText("Name of Grab Project");
            dialog.setContentText("Name:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                name = result.get();
            }
            if (name.length() < 1) {
                name = "Default" + (int) (Math.random() * 1000);
            }
        }while(handler.checkTableExists(name));
        String qu = "INSERT INTO PROJECTS (NAME, LOCATION) VALUES ('"+name+"','NONE' )";
        handler.execAction(qu);
        handler.createImageFolderTable(name);
        currentProject = new Project(name);
        openProject(currentProject);
    }

    private void openProject(Project project) {
        toDate.setDisable(false);
        fromDate.setDisable(false);
        deleted.clear();
        getPhotosFromDB();
        isGrabbing = true;
        folderTilePane.getChildren().clear();
        root.requestFocus();
        getSettings();
        Image upLevel = new Image(getClass().getResourceAsStream(Icon.UPLEVELICON.fileName));
        VBox upLevelBox = addIcon(folderTilePane, upLevel,"Back to projects");
        upLevelBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            deleted.clear();
            viewingPhotos.clear();
            isGrabbing = false;
            likes.clear();
            favourites.clear();
            keeps.clear();
            loadNoImage();
            baseScene();
            toDate.setDisable(true);
            fromDate.setDisable(true);
            selected.clear();
        });
        missingThumbnails = new ArrayList<>();
        for (Photo photo : project.getPhotos()) {
            if (!photo.getThumbnail().exists()) {
                missingThumbnails.add(photo);
            }
        }
        if(!missingThumbnails.isEmpty()){
            createMissingThumbnails();
        }
        ExecutorService executor = Executors.newFixedThreadPool(20);
        executor.submit( () ->{
            addThumbnailsToGrid(project.getPhotos(), 0);
            executor.shutdown();
        });
        root.requestFocus();
    }

    private void getPhotosFromDB() {
        ResultSet rs = handler.execQuery("SELECT * FROM FAVOURITES");
        try {
            while (rs.next()) {
                File file = new File(rs.getString("LOCATION"));
                String date = rs.getString("DATE");
                String rotation = rs.getString("ROTATION");
                Photo photo = new Photo(file, date, rotation, currentProject.getProjectName(), "FAVOURITE");
                favourites.add(photo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        rs = handler.execQuery("SELECT * FROM KEEP");
        try {
            while (rs.next()) {
                File file = new File(rs.getString("LOCATION"));
                String date = rs.getString("DATE");
                String rotation = rs.getString("ROTATION");
                Photo photo = new Photo(file, date, rotation, currentProject.getProjectName(), "KEEP");
                keeps.add(photo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        rs = handler.execQuery("SELECT * FROM LIKES");
        try {
            while (rs.next()) {
                File file = new File(rs.getString("LOCATION"));
                String date = rs.getString("DATE");
                String rotation = rs.getString("ROTATION");
                Photo photo = new Photo(file, date, rotation, currentProject.getProjectName(), "LIKE");
                likes.add(photo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addProjects() {
        ResultSet rs = handler.execQuery("SELECT * FROM PROJECTS");
        try {
            while (rs.next()) {
                String name = rs.getString("NAME");
                Image addFolder = new Image(getClass().getResourceAsStream(Icon.FOLDERICON.fileName));
                VBox addProjectBox = addIcon(folderTilePane, addFolder, name);
                projects.add(new Project(name, addProjectBox));
                addProjectBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    VBox clicked = (VBox) event.getSource();
                    currentProject = getProject(clicked);
                    openProject(currentProject);
                });

            }
            root.requestFocus();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Project getProject(VBox clicked) {
        Project returnProject = null;
        root.requestFocus();
        if (projects.isEmpty()) {
            return null;
        }
        for (Project p : projects) {
            if(p.getBox().equals(clicked)){
                returnProject = p;
                System.out.println(p);
            }
        }
        return returnProject;
    }

    private void addFolders() {
        ResultSet rs = handler.execQuery("SELECT * FROM FOLDERS");
        try {
            while (rs.next()) {
                String name = rs.getString("NAME");
                String location = rs.getString("LOCATION");
                Image addFolder = new Image(getClass().getResourceAsStream(Icon.FOLDERICON.fileName));
                VBox addFolderBox = addIcon(folderTilePane, addFolder, name);
                folders.add(new Folder(name, new File(location), addFolderBox));
                addFolderBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    VBox clicked = (VBox) event.getSource();
                    currentFolder = getFolder(clicked);
                    openFolder(currentFolder, 0);
                });

            }
            root.requestFocus();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void openFolder(Folder folder, int index){
        if (folder.getName().equals("NODATE")) {
            toDate.setDisable(false);
            setDateButton.setDisable(false);
            dateLabel.setVisible(true);
        }
        isPhotoView = true;
        deleted.clear();
        folderTilePane.getChildren().clear();
        root.requestFocus();
        Image upLevel = new Image(getClass().getResourceAsStream(Icon.UPLEVELICON.fileName));
        VBox upLevelBox = addIcon(folderTilePane, upLevel,"Back to projects");
        upLevelBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            deleted.clear();
            viewingPhotos.clear();
            isPhotoView = false;
            toDate.setDisable(true);
            setDateButton.setDisable(true);
            loadNoImage();
            baseScene();
            dateLabel.setVisible(false);
        });
        missingThumbnails = new ArrayList<>();
        for (Photo photo : folder.getPhotos()) {
            if (!photo.getThumbnail().exists()) {
                missingThumbnails.add(photo);
            }
        }
        if(!missingThumbnails.isEmpty()){
            createMissingThumbnails();
        }
        ExecutorService executor = Executors.newFixedThreadPool(20);
        executor.submit( () ->{
           addThumbnailsToGrid(folder.getPhotos(), index);
           executor.shutdown();
        });
        root.requestFocus();
    }

    private void addThumbnailsToGrid(ArrayList<Photo> photos, int index) {
        viewingPhotos.clear();
        for (Photo photo : photos) {
            if(!photo.getThumbnail().exists()){
                missingThumbnails.add(photo);
            }

        }
        if (!missingThumbnails.isEmpty()){
            createMissingThumbnails();
        }
        for (Photo photo : photos) {
            if (!photo.getStatus().equals("DELETED")) {
                viewingPhotos.add(photo);
                Image image = null;
                try {
                    FileInputStream imageStream = new FileInputStream(photo.getThumbnail());
                    image = new Image(imageStream);
                    imageStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(90);
                imageView.setCache(true);
                imageView.setCacheHint(CacheHint.SPEED);
                VBox vbox = new VBox();
                vbox.setAlignment(Pos.BOTTOM_CENTER);
                vbox.getStyleClass().add("vboxImages");
                vbox.setPadding(new Insets(7,7,7,7));
                vbox.setPrefWidth(70);
                javafx.scene.control.Label test = new javafx.scene.control.Label(photo.getName());
                test.setTextFill(Color.web("#FAFAFA"));
                vbox.getChildren().addAll(imageView,test);
                photo.setBox(vbox);
                applyRotation(imageView, photo.getRotation(), true);
                vbox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    addImageToImageView(photo, mainImageView, photo.getRotation());
                    currentPhoto = photo;
                    VBox clickedBox =(VBox)event.getSource();
                    if (!event.isControlDown() && !event.isShiftDown()) {
                        selected.clear();
                    }
                    Photo pho = getPhotoInBox(clickedBox);
                    if (event.isShiftDown()) {
                        int start = 0;
                        int end = 0;
                        Photo lastadded = selected.get(selected.size() - 1);
                        for (Photo p : viewingPhotos) {
                            if (lastadded.equals(p)) {
                                start = viewingPhotos.indexOf(p);
                            }
                            if (pho.equals(p)) {
                                end = viewingPhotos.indexOf(p);
                            }
                        }
                        if(start != end){
                            if(start > end){
                                int temp = start;
                                start = end;
                                end = temp;
                            }
                            for (int i = start; i <= end; i++) {
                                if(!selected.contains(viewingPhotos.get(i))) {
                                    selected.add(viewingPhotos.get(i));
                                }
                            }
                        }
                    }
                    if (pho != null) {
                        if(!selected.contains(pho)) {
                            selected.add(pho);
                        }
                        applySelectedStyle(selected);
                    }
                });
                Platform.runLater(() -> folderTilePane.getChildren().add(vbox));
            }

        }
        Photo selectedPhoto = viewingPhotos.get(index);
        selected.add(selectedPhoto);
        currentPhoto = selectedPhoto;
        applySelectedStyle(selected);
        addImageToImageView(selectedPhoto,mainImageView,selectedPhoto.getRotation());
        root.requestFocus();
    }

    synchronized private void applySelectedStyle(ArrayList<Photo> selected) {
        for (Photo photo : viewingPhotos) {
            photo.getBox().getStyleClass().clear();
            photo.getBox().getStyleClass().add("vboxImages");
            switch (photo.getStatus()) {
                case "FAVOURITE" -> {
                    photo.getBox().getStyleClass().clear();
                    photo.getBox().getStyleClass().add("favourite");
                }
                case "LIKE" -> {
                    photo.getBox().getStyleClass().clear();
                    photo.getBox().getStyleClass().add("like");
                }
            }
            if (selected.contains(photo)) {
                photo.getBox().getStyleClass().add("selected");
                if (photo.equals(currentPhoto)) {
                    photo.getBox().getStyleClass().add("selectedFocus");
                }
            }
            photo.getBox().getStyleClass().add("vboxImages");
        }
        root.requestFocus();
    }

    private Photo getPhotoInBox(VBox source) {
        Photo pic = null;
        for (Photo photo : viewingPhotos) {
            if (photo.getBox().equals(source)) {
                pic = photo;
            }
        }
        root.requestFocus();
        return pic;
    }

    private void createMissingThumbnails() {
        root.requestFocus();
        Task missingThumbs = createMissingThumbs();
        ProgressDialog progress = new ProgressDialog(missingThumbs);
        progress.setContentText("Creating missing thumbnails");
        progress.setTitle("PhoSort");
        new Thread(missingThumbs).start();

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
            ImageIO.write(small, "jpg", out);
            root.requestFocus();
        } catch (IIOException e) {
            System.out.println(file.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Folder getFolder(VBox folderBox){
        Folder returnFolder = null;
        root.requestFocus();
        if (folders.isEmpty()) {
            return null;
        }
        for (Folder f : folders) {
            if(f.getBox().equals(folderBox)){
                returnFolder = f;
                System.out.println(f);
            }
        }
        return returnFolder;
    }

    private void addPhotoFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Folder to Sort");
        directoryChooser.setInitialDirectory(Settings.userPictureFolder);
        File folderToAdd = directoryChooser.showDialog(stage);
        if (folderToAdd == null) {
            return;
        }
        new FolderBuilder(folderToAdd);
        addFolders();
    }

    private void loadNoImage() {
        width = mainImageView.getFitWidth();
        Image image = new Image(getClass().getResourceAsStream(Icon.NOIMAGE.fileName));
        addImageToImageView(image,mainImageView);
        mainImageView.setRotate(0);
    }

    private void addImageToImageView(Photo photo, ImageView imageView, String rotation){
        File image = photo.getFile();
        System.out.println(rotation);
        try{
            FileInputStream stream = new FileInputStream(image);
            Image im = new Image(stream);
            imageView.setImage(im);
            imageView.setPreserveRatio(true);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        applyRotation(imageView, rotation, false);
        root.requestFocus();
        currentPhoto = photo;
        if (currentFolder != null) {
            if (currentFolder.getName().equals("NODATE")) {
                Platform.runLater( () ->{
                    dateLabel.setText(currentPhoto.getDate());
                });

            }
        }
    }

    private void applyRotation(ImageView imageView, String rotation, boolean thumbs) {
        Rotations rotate = null;
        for (Rotations r : Rotations.values()) {
            if (r.fileName.equals(rotation)) {
                rotate = r;
                System.out.println(rotate);
            }
        }
        System.out.println(rotation);
        if (rotate != null) {
            imageView.setRotate(rotate.rotation);
            if(rotate.equals(Rotations.OLD270)){
                imageView.setRotate(90);
            } else if (rotate.equals(Rotations.OLD90)) {
                imageView.setRotate(270);
            }
            if ((rotate.rotation == 90 || rotate.rotation == 270)  && !thumbs) {
                mainImageView.setFitWidth(imageBorderPane.getHeight() - 30);
            }
        }
    }

    private void addImageToImageView(Image image, ImageView imageView){
        imageView.setImage(image);
        //imageView.setFitWidth(100);
        root.requestFocus();
    }

    private VBox addIcon(TilePane imageTilePane, Image image, String name){
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(82);
        imageView.setCache(true);
        imageView.setCacheHint(CacheHint.SPEED);
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.BOTTOM_CENTER);
        vbox.getStyleClass().add("vboxImages");
        vbox.setPadding(new Insets(0,0,30,0));
        javafx.scene.control.Label test = new javafx.scene.control.Label(name);
        test.setTextFill(Color.web("#FAFAFA"));
        vbox.getChildren().addAll(imageView,test);
        root.requestFocus();
        Platform.runLater(() -> imageTilePane.getChildren().add(vbox));
        return vbox;
    }

    public void favouritePressed() {
        if (isGrabbing) {
            ArrayList<Photo> grabbed = new ArrayList<>();
            if (fromDate.getValue() == null || toDate == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Select both dates");
                alert.showAndWait();
                return;
            }
            for (Photo photo : favourites) {
                Date photoDate = null;
                try {
                    photoDate = stringDateToDate(photo.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (dateLessThan(photoDate, toDate.getValue()) && dateGreaterThan(photoDate, fromDate.getValue())) {
                    if (!viewingPhotos.contains(photo)) {
                        grabbed.add(photo);
                        handler.execAction("INSERT INTO " + currentProject.getProjectName() + "(FILELOCATION, FILENAME, STATUS, ROTATION, DATE) VALUES ('"+photo.getFile().getAbsolutePath() + "','" + photo.getName() + "','" + photo.getStatus() + "','" + photo.getRotation() + "','" + photo.getDate() + "')");
                    }
                }
            }
            if(!grabbed.isEmpty()) {
                addThumbnailsToGrid(grabbed, 0);
            }
        }
        else{
            for (Photo photo : selected) {
                if(photo.getStatus().equals("FAVOURITE")){
                    String qu = "UPDATE " + currentFolder.getName() + " SET STATUS = 'KEEP' WHERE FILENAME = '" + photo.getName() + "'";
                    handler.execUpdate(qu);
                    photo.setStatus("KEEP");
                    root.requestFocus();
                }else{
                    String qu = "UPDATE " + currentFolder.getName() + " SET STATUS = 'FAVOURITE' WHERE FILENAME = '" + photo.getName() + "'";
                    handler.execUpdate(qu);
                    photo.setStatus("FAVOURITE");
                    root.requestFocus();
                }
            }
        }
        root.requestFocus();
    }

    public void likePressed() {
        if (isGrabbing) {
            ArrayList<Photo> grabbed = new ArrayList<>();
            if (fromDate.getValue() == null || toDate == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Select both dates");
                alert.showAndWait();
                return;
            }
            for (Photo photo : likes) {
                Date photoDate = null;
                try {
                    photoDate = stringDateToDate(photo.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (dateLessThan(photoDate, toDate.getValue()) && dateGreaterThan(photoDate, fromDate.getValue())) {
                    if (!viewingPhotos.contains(photo)) {
                        grabbed.add(photo);
                        handler.execAction("INSERT INTO " + currentProject.getProjectName() + "(FILELOCATION, FILENAME, STATUS, ROTATION, DATE) VALUES ('"+photo.getFile().getAbsolutePath() + "','" + photo.getName() + "','" + photo.getStatus() + "','" + photo.getRotation() + "','" + photo.getDate() + "')");
                    }
                }
            }
            if(!grabbed.isEmpty()) {
                addThumbnailsToGrid(grabbed, 0);
            }
        }
        else {
            for (Photo photo : selected) {
                if (photo.getStatus().equals("LIKE")) {
                    String qu = "UPDATE " + currentFolder.getName() + " SET STATUS = 'KEEP' WHERE FILENAME = '" + photo.getName() + "'";
                    handler.execUpdate(qu);
                    photo.setStatus("KEEP");
                } else {
                    String qu = "UPDATE " + currentFolder.getName() + " SET STATUS = 'LIKE' WHERE FILENAME = '" + photo.getName() + "'";
                    handler.execUpdate(qu);
                    photo.setStatus("LIKE");
                }
            }
        }
        root.requestFocus();
    }

    public void keepPressed() {
        if (isGrabbing) {
            ArrayList<Photo> grabbed = new ArrayList<>();
            if (fromDate.getValue() == null || toDate == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Select both dates");
                alert.showAndWait();
                return;
            }
            for (Photo photo : keeps) {
                Date photoDate = null;
                try {
                    photoDate = stringDateToDate(photo.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (dateLessThan(photoDate, toDate.getValue()) && dateGreaterThan(photoDate, fromDate.getValue())) {
                    if (!viewingPhotos.contains(photo)) {
                        grabbed.add(photo);
                        handler.execAction("INSERT INTO " + currentProject.getProjectName() + "(FILELOCATION, FILENAME, STATUS, ROTATION, DATE) VALUES ('"+photo.getFile().getAbsolutePath() + "','" + photo.getName() + "','" + photo.getStatus() + "','" + photo.getRotation() + "','" + photo.getDate() + "')");
                    }
                }
            }
            if(!grabbed.isEmpty()) {
                addThumbnailsToGrid(grabbed, 0);
            }
        }
        else {
            for (Photo photo : selected) {
                if (photo.getStatus().equals("KEEP")) {
                    String qu = "UPDATE " + currentFolder.getName() + " SET STATUS = 'KEEP' WHERE FILENAME = '" + photo.getName() + "'";
                    handler.execUpdate(qu);
                    photo.setStatus("KEEP");
                }
            }
        }

        root.requestFocus();
    }

    public void trashPressed() {
        ArrayList<Photo> deletes = new ArrayList<>();
        if (isGrabbing) {
            for (Photo p : selected) {
                deletes.add(p);
                folderTilePane.getChildren().remove(p.getBox());
                String remove = "DELETE FROM " + currentProject.getProjectName() + " WHERE FILENAME='" + p.getName() + "'";
                handler.execUpdate(remove);
                viewingPhotos.remove(p);
            }
        }
        else {
            for (Photo photo : selected) {
                deletes.add(photo);
                folderTilePane.getChildren().remove(photo.getBox());
                String update = "UPDATE " + currentFolder.getName() + " SET STATUS = 'DELETED' WHERE FILENAME = '" + photo.getName() + "'";
                handler.execUpdate(update);
                photo.setStatus("DELETED");
                viewingPhotos.remove(photo);
            }
        }
            deleted.add(deletes);
        if (viewingPhotos.isEmpty()) {
            loadNoImage();
        }
        else {
            if (viewingPhotos.get(viewingPhotos.size() - 1).equals(deletes.get(deletes.size() - 1))) {
                currentIndex = viewingPhotos.indexOf(deletes.get(0));
                selected.clear();
                selected.add(viewingPhotos.get(currentIndex));
                currentPhoto = viewingPhotos.get(currentIndex);
                addImageToImageView(currentPhoto,mainImageView,currentPhoto.getRotation());
                applySelectedStyle(selected);
                root.requestFocus();
                //photos still has deleted photos.  Messes with indexs.  may need visible list
            }
        }

    }

    public void restorePressed() {
        if (deleted.isEmpty()) {
            return;
        }
        if (isGrabbing) {
            ArrayList<Photo> deletes = deleted.get(deleted.size() - 1);
            deleted.remove(deletes);
            for (Photo photo : deletes) {
                handler.execAction("INSERT INTO " + currentProject.getProjectName() + "(FILELOCATION, FILENAME, STATUS, ROTATION, DATE) VALUES ('"+photo.getFile().getAbsolutePath() + "','" + photo.getName() + "','" + photo.getStatus() + "','" + photo.getRotation() + "','" + photo.getDate() + "')");
            }
            openProject(currentProject);
        }
        else {
            ArrayList<Photo> deletes = deleted.get(deleted.size() - 1);
            deleted.remove(deletes);
            for (Photo photo : deletes) {
                String update = "UPDATE " + currentFolder.getName() + " SET STATUS = 'KEEP' WHERE FILENAME = '" + photo.getName() + "'";
                handler.execUpdate(update);
                photo.setStatus("KEEP");
            }
            openFolder(currentFolder, viewingPhotos.indexOf(deletes.get(0)));
        }
        root.requestFocus();
        //selected.add(viewingPhotos.get(0));
        //applySelectedStyle(selected);
    }

    private Date stringDateToDate(String stringDate) throws ParseException {
        try {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.parse(stringDate);
        } catch (Exception e) {
            return new Date();
        }
    }

    public void finished() {
        if (isGrabbing) {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Copy Photos to this location");
            File destinationFolder = chooser.showDialog(stage);
            for (Photo photo : viewingPhotos) {
                File destination = new File(destinationFolder.getAbsolutePath() + "\\" + photo.getName());
                try {
                    FileUtils.copyFile(photo.getFile(), destination);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Photos Copied");
            alert.setContentText("Have a lovely day");
            alert.setHeaderText("Thank you for using phosort");
            alert.showAndWait();
        }else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("");
            alert.setHeaderText("Complete sorting folder");
            alert.setContentText("Are you sure you want to mark this folder to completed?  This will mark all photos status and send them to their appropriate folders");
            Optional<ButtonType> result = alert.showAndWait();
            root.requestFocus();
            if (result.get() == ButtonType.OK) {
                if (!isPhotoView) {
                    return;
                }

                File deleteFolder = Settings.deleteFolder;
                File sortFolder = Settings.sortFolder;
                for (Photo photo : currentFolder.getPhotos()) {
                    Date date = null;
                    try {
                        date = stringDateToDate(photo.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    String starter = sortFolder.getAbsolutePath();
                    String year = getYearString(photo.getDate());
                    String month = getMonthString(photo.getDate());
                    String destination = starter + "\\" + year + "\\" + month + "\\" + photo.getName();
                    if (photo.getStatus().equals("DELETED")) {
                        String insert = "INSERT INTO DELETED (NAME, DATE) VALUES ('" + photo.getName() + "', '" + sqlDate + "')";
                        handler.execAction(insert);
                        try {
                            FileUtils.moveFile(photo.getFile(), new File(deleteFolder.getAbsolutePath() + "\\" + photo.getName()));
                        } catch (IOException e) {
                            Alert alert1 = new Alert(Alert.AlertType.WARNING);
                            alert1.setHeaderText("Failed to move file to deletes");
                            alert1.setContentText(photo.getName() + ": was set to be deleted but failed to move");
                        }

                    } else if (photo.getStatus().equals("KEEP")) {
                        String insert = "INSERT INTO KEEP (NAME, DATE, LOCATION, ROTATION) VALUES ('" + photo.getName() + "','" + sqlDate + "','" + destination + "','" + photo.getRotation() + "')";
                        handler.execAction(insert);
                        try {
                            FileUtils.moveFile(photo.getFile(), new File(destination));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else if (photo.getStatus().equals("FAVOURITE")) {
                        String insert = "INSERT INTO FAVOURITES (NAME, DATE, LOCATION, ROTATION) VALUES ('" + photo.getName() + "','" + sqlDate + "' ,'" + destination + "','" + photo.getRotation() + "')";
                        handler.execAction(insert);
                        try {
                            FileUtils.moveFile(photo.getFile(), new File(destination));
                        } catch (FileExistsException existsException) {
                            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                            alert1.setContentText(photo.getName() + " Already Exists should not occur in final version");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else if (photo.getStatus().equals("LIKE")) {
                        String insert = "INSERT INTO LIKES (NAME, DATE, LOCATION, ROTATION) VALUES ('" + photo.getName() + "','" + sqlDate + "','" + destination + "','" + photo.getRotation() + "')";
                        handler.execAction(insert);
                        try {
                            FileUtils.moveFile(photo.getFile(), new File(destination));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }
                String drop = "DROP TABLE " + currentFolder.getName();
                handler.execAction(drop);
                String delete = "DELETE FROM FOLDERS WHERE NAME='" + currentFolder.getName() + "'";
                //System.out.println(delete);
                handler.execUpdate(delete);
                System.out.println(delete);
                deleted.clear();
                viewingPhotos.clear();
                baseScene();
                loadNoImage();
                root.requestFocus();
                isPhotoView = false;
            } else {
                return;
            }
        }
    }

    private String getMonthString(String date) {
        String value = "";
        String num = date.substring(5, 7);
        int number = Integer.parseInt(num);
        switch (number) {
            case 1:
                value = number + "-January";
                break;
            case 2:
                value = number + "-February";
                break;
            case 3:
                value = number + "-March";
                break;
            case 4:
                value = number + "-April";
                break;
            case 5:
                value = number + "-May";
                break;
            case 6:
                value = number + "-June";
                break;
            case 7:
                value = number + "-July";
                break;
            case 8:
                value = number + "-August";
                break;
            case 9:
                value = number + "-September";
                break;
            case 10:
                value = number + "-October";
                break;
            case 11:
                value = number + "-November";
                break;
            case 12:
                value = number + "-December";
                break;
        }
        return value;
    }

    private String getYearString(String date) {
        return date.substring(0, 4);
    }

    public void minimize() {
        stage.setIconified(true);
    }

    public void maximize() {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }

    public void close() {
        System.exit(0);
    }

    public void chooseSelected(KeyEvent keyEvent) {
        root.requestFocus();
        if (!isPhotoView && !isGrabbing) {
            System.out.println("NOT VIEWING");
            return;
        }
        int currentIndex = 0;
        Folder folder = currentFolder;
        currentIndex = viewingPhotos.indexOf(currentPhoto);

        if (keyEvent.getCode() == KeyCode.RIGHT) {
            if (currentIndex == viewingPhotos.size() - 1) {
                return;
            }
            else{
                Photo newCurrent = viewingPhotos.get(currentIndex + 1);
                addImageToImageView(newCurrent, mainImageView, newCurrent.getRotation());
                if(!keyEvent.isShiftDown()) {
                    selected.clear();
                }
                selected.add(newCurrent);
                applySelectedStyle(selected);
                root.requestFocus();
                currentPhoto = newCurrent;
            }

        } else if (keyEvent.getCode() == KeyCode.LEFT) {
            if (currentIndex == 0) {
                return;
            }
            else{
                Photo newCurrent = viewingPhotos.get(currentIndex -1);
                addImageToImageView(newCurrent, mainImageView, newCurrent.getRotation());
                if(!keyEvent.isShiftDown()) {
                    selected.clear();
                }
                selected.add(newCurrent);
                applySelectedStyle(selected);
                root.requestFocus();
                currentPhoto = newCurrent;
            }
        } else if (keyEvent.getCode() == KeyCode.UP) {
            if (currentIndex < 4) {
                return;
            }
            else{
                Photo newCurrent = viewingPhotos.get(currentIndex - 4);
                addImageToImageView(newCurrent, mainImageView, newCurrent.getRotation());
                if(!keyEvent.isShiftDown()) {
                    selected.clear();
                }
                selected.add(newCurrent);
                applySelectedStyle(selected);
                root.requestFocus();
                currentPhoto = newCurrent;
            }

        } else if (keyEvent.getCode() == KeyCode.DOWN) {
            if (currentIndex == viewingPhotos.size() - 4) {
                return;
            }
            else{
                Photo newCurrent = viewingPhotos.get(currentIndex + 4);
                addImageToImageView(newCurrent, mainImageView, newCurrent.getRotation());
                if(!keyEvent.isShiftDown()) {
                    selected.clear();
                }
                selected.add(newCurrent);
                applySelectedStyle(selected);
                root.requestFocus();
                currentPhoto = newCurrent;
            }

        } else if (keyEvent.getCode() == KeyCode.D) {
            trashPressed();

        } else if (keyEvent.getCode() == KeyCode.F) {
            favouritePressed();

        } else if (keyEvent.getCode() == KeyCode.L) {
            likePressed();
        }
    }

    public void savePhotoGrab(ActionEvent actionEvent) {
    }

    public void openPhotoGrab(ActionEvent actionEvent) {
    }

    public void openSettingsChanger() {
        loadWindow("/layouts/settings.fxml");
    }

    private void loadWindow(String location){
        try{
            Parent parent = FXMLLoader.load(getClass().getResource(location));

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setScene(new Scene(parent));
            stage.show();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    private boolean dateLessThan(Date imageDate, LocalDate toDate){
        if(toDate == null){
            return true;
        }
        if(imageDate == null){
            return false;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(imageDate);
        int imageYear = convertYear(imageDate.getYear());
        int toYear = toDate.getYear();
        int imageMonth = imageDate.getMonth() + 1;
        int imageDay = cal.get(Calendar.DAY_OF_MONTH);
        int toMonth = toDate.getMonthValue();
        int toDay = toDate.getDayOfMonth();
        //System.out.println("image day " + imageDay + " toDay " + toDay);
        if(imageYear > toYear){
            //System.out.println("image year greater");
            return false; //Greater than year
        }
        else if(imageYear == toYear){
            //System.out.println("image year the same");
            if(imageMonth > toMonth){
                //System.out.println("image month greater");
                return false; //Same year greather than month
            }
            else if(imageMonth == toMonth){
                //System.out.println("image month the same");
                if(imageDay > toDay){
                    //System.out.println("image day greater");
                    return false; //Same year same month greather than day
                }
            }
        }
        return true;
    }

    private int convertYear(int year){
        int actualYear = 1900 + year;
        return actualYear;
    }
    private boolean dateGreaterThan(Date imageDate, LocalDate fromDate){
        if(fromDate == null){
            return true;
        }
        if(imageDate == null){
            return false;
        }
        int imageYear = convertYear(imageDate.getYear());
        int toYear = fromDate.getYear();
        int imageMonth = imageDate.getMonth() + 1;
        Calendar cal = Calendar.getInstance();
        cal.setTime(imageDate);
        int imageDay = cal.get(Calendar.DAY_OF_MONTH);
        int toMonth = fromDate.getMonthValue();
        int toDay = fromDate.getDayOfMonth();
        if(imageYear < toYear){
            return false; //Greater than year
        }
        else if(imageYear == toYear){
            if(imageMonth < toMonth){
                return false; //Same year greather than month
            }
            else if(imageMonth == toMonth){
                if(imageDay < toDay){
                    return false; //Same year same month greather than day
                }
            }
        }
        return true;
    }

    public void keep(MouseEvent mouseEvent) {
    }
    public void grabMode() {
        if(!isPhotoView) {
            isGrabMode = true;
            isSortingMode = false;
            baseScene();
        }
    }

    public void sortMode() {
        if(!isGrabbing) {
            isSortingMode = true;
            isGrabMode = false;
            baseScene();
        }
    }

    public void setDate() {
        if (toDate.getValue() == null) {
            return;
        }
        LocalDate date = toDate.getValue();
        for (Photo p : selected) {
            handler.execUpdate("UPDATE "+ currentFolder.getName() + " SET DATE = '" + date + "' WHERE FILENAME = '" + p.getName() + "'");
            p.setDate(date.toString());
            dateLabel.setText(date.toString());
        }

    }

    public void checkDatabaseFiles() {
        ArrayList<String> missingPhotos = new ArrayList<>();
        ResultSet resultSet = handler.execQuery("SELECT * FROM FAVOURITES");
        try {
            while (resultSet.next()){
                File dbImage = new File(resultSet.getString("LOCATION"));
                if(!dbImage.exists()){
                    System.out.println("Favourites: " + dbImage.getAbsolutePath());
                    missingPhotos.add(dbImage.getAbsolutePath());
                }
            }
            System.out.println("Finished Faves");
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        resultSet = handler.execQuery("SELECT * FROM KEEP");
        try {
            while (resultSet.next()){
                File dbImage = new File(resultSet.getString("LOCATION"));
                if(!dbImage.exists()){
                    System.out.println("KEEPS: " + dbImage.getAbsolutePath());
                    missingPhotos.add(dbImage.getAbsolutePath());
                }
            }
            System.out.println("Finished Keep");
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        resultSet = handler.execQuery("SELECT * FROM LIKES");
        try {
            while (resultSet.next()){
                File dbImage = new File(resultSet.getString("LOCATION"));
                if(!dbImage.exists()){
                    System.out.println("Likes: " + dbImage.getAbsolutePath());
                    missingPhotos.add(dbImage.getAbsolutePath());
                }
            }
            System.out.println("Finished Likes");
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (missingPhotos.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Missing Photos");
            alert.setHeaderText("All photos accounted for");
            alert.showAndWait();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Missing Photos");
            alert.setHeaderText("Missing " + missingPhotos.size() + " photos");
            Date date = new Date();
            String parser = date.toString();
            String[] parser2 = parser.split(" ");
            File out = new File("Missing photos on " + parser2[0] + " " + parser2[1] + " " + parser2[2] + " " + parser2[5] + ".txt");
            System.out.println(out.getAbsolutePath());
            alert.setContentText("List of photos located at: " + out.getAbsolutePath());
            try {
                //out.createNewFile();
                FileWriter fw = new FileWriter(out);
                BufferedWriter bw = new BufferedWriter(fw);
                for (String s : missingPhotos) {
                    bw.write(s);
                    bw.newLine();
                }
                bw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            alert.showAndWait();
        }

    }

    public void deleteProject() {
        if (isGrabbing) {
            handler.execAction("DROP TABLE " + currentProject.getProjectName());
            String delete = "DELETE FROM PROJECTS WHERE NAME='" + currentProject.getProjectName() + "'";
            handler.execUpdate(delete);
            deleted.clear();
            viewingPhotos.clear();
            isGrabbing = false;
            likes.clear();
            favourites.clear();
            keeps.clear();
            loadNoImage();
            baseScene();
            toDate.setDisable(true);
            fromDate.setDisable(true);
            selected.clear();
        }
    }

    public void copyProject() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Not functioning yet.");
        alert.showAndWait();
    }
}