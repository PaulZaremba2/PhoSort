/**
 * @version I dont know how version work and I am too afraid to ask
 * @author Paul Zaremba
 *
 * PhoSort has 2 modes.  Sort and Grab.  Sort mode is used by choosing a folder full of
 * images and videos.  The program then sorts the videos by date and moves them
 * into the users video folder.  Then it sorts the pictures by date and stores them in a
 * temporary sorting directory.  It reads the metadata and creates thumbnails at the same time.
 * Working folders will then appear on the left scrollpane.  Select a month to sort.
 * You can choose to like, favourite, keep or delete a photo.  Once you have labeled
 * all the photos hit the check mark.  It will mark them all appropriately move them
 * to the sorted photos folder and record them in a database for future grabbing.
 * Grab mode goes through the sorted photos database and pulls photos out by date and
 * label. (favourite, like or keep)
 * You can edit which folders are in the grab project and once you have the photos you want
 * simply hit the check mark and copy them to any folder of your choosing.
 * Used for printing, making albums or slide shows etc.
 *
 * @Copyright &copy; 2022 Zaremba Programming. All rights reserved.
 */


package com.zaremba.phosort.ui;


import com.zaremba.imgscalr.Scalr;
import com.zaremba.phosort.tools.*;
import com.zaremba.phosort.tools.Icon;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
    //JavaFX containers
    //Top Menu Items
    public VBox root; //Main VBox container everything is in
    public AnchorPane menuAnchor;//Anchor pane so menu and open close icons work
    public MenuBar menuBar;
    public HBox controlBox;//Holds minimize maximize and close icons
    //Thumbnail viewing stuff
    public HBox hBox; //HBox below anchorpane containing thumbnail viewer and photoviewer
    public VBox asideBox;//Box holding thumbnail view items
    public AnchorPane imageAnchor;//AnchorPane to make thumbnail viewer stretch (I think)
    public ScrollPane folderScrollPane;//Scrollpane to show thumbnails
    public TilePane folderTilePane;//Inside scrollpane to create a tile of thumbnails

    //ImageView and Sorting stuff
    public VBox vboxImageSide;//VBox containing elements for viewing and sorting images
    public HBox hBoxIcons;//Contains the status selectors of photos
    public ImageView likeIcon;
    public ImageView trashIcon;
    public ImageView restoreIcon;
    public ImageView finishedIcon;
    public ImageView favouriteIcon;
    //Main ImageView nodes
    public Pane imageBorderPane;
    public ImageView mainImageView;
    //Setting and selecting dates nodes
    public DatePicker fromDate;
    public DatePicker toDate;
    public Button setDateButton;
    public Label dateLabel;
    //Stage object grabbed in init method
    Stage stage;
    //Lists
    private ArrayList<Folder> folders; //Folders to be sorted retreived from Database table folders
    private ArrayList<Photo> missingThumbnails; //Used when creating thumbnails, if a photo is missing thumbnails add them to this list to be created
    private ArrayList<Photo> selected; //Holds currently selected photos
    private ArrayList<ArrayList<Photo>> deleted;//List of list of deleted photos
    private ArrayList<Photo> viewingPhotos;//Photos currently being viewed
    private ArrayList<Project> projects; //Projects grabbed from database
    private ArrayList<Photo> favourites; //Favourites in Grab mode
    private ArrayList<Photo> likes; //Likes in Grab Mode
    private ArrayList<Photo> keeps; //Keeps in Grab mode
    //Info used for layout purposes
    double sceneHeight;
    double sceneWidth;
    public double width;
    private double xOffSet = 0;
    private double yOffset = 0;

    DatabaseHandler handler;
    private Photo currentPhoto;//Current photo in mainImageView
    private Folder currentFolder;//Current folder being
    private Project currentProject;
    //States
    private boolean isPhotoView = false;
    private boolean isGrabMode = false;
    private boolean isSortingMode = true;
    private boolean isGrabbing = false;
    int currentIndex;

    /**
     * This is used to create a progress bar showing how many missing thumbnails in a
     * folder or a project.  Shows how many are left and the name of the files that
     * needed thumbnails.
     * @return returns a task that does stuff
     */
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

    /**
     * Inside the Platform.runLater portion:
     * Get the main stage, needed for several layout items
     * Get measurements of window
     * Setup the thumbnail and project viewer
     * Setup the main image viewer
     * Set items to not be traversable to stop messing with arrows keys
     * Add Resize listener I dont think this works perfect but good enough so far
     * load base scene and no image
     * add listeners to drag menu bar around
     *
     * Outside of Platform.runlater:
     * get database handler
     * Initialize my lists
     * disable date pickers
     * @param location unused
     * @param resources unused
     */
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
            //Too affraid to delete this for some unknown reason.
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

    /**
     * This method retreives information from the database where all the key
     * folders to be used are located.  Ex.  Where to move videos, deleted photos
     * duplicate photos, sort photos etc.
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

    /**
     * The base scene checks if we are sorting or grabbing.  It will give user
     * ability to add a new sort folder or create a grab project depending on
     * their mode.
     */
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

    /**
     * Projects refer to the grab projects of looking through sorted photos
     * It creates a project, initializes a table in the database. Then it
     * opens up the project allowing the user to start grabbing photos.
     */
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

    /**
     * This removes the base scene in the scroll pane and sets up the program
     * to allow the user to select dates and grab photos.  If photos have already
     * been grabbed it will add their thumbnails to the scrollpane. It also
     * creates a method that returns the user to the base scene and erases the
     * lists and other data that was used in the project mode.
     * @param project Project object that contains information of the
     *                photos in the project.
     */
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

    /**
     * This method is called in the openProject method.  It goes through the
     * database and selects all the favourite, liked and kept images.  It stores
     * them in a list to be retrieved using the date selector and like,favourite and keep
     * icons.
     */
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

    /**
     * This is called in the baseScene method if in grab mode.  It looks att he database
     * for current projects and displays them for the user to select which project
     * to work on.  Creates a listener on the thumbnails of projects when clicked
     * executes open project method.
     */
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

    /**
     * Searches through a list of projects and finds the project the user
     * clicked on.
     * @param clicked VBox that is holding an image of a project name
     * @return The project that the user clicked on
     */
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

    /**
     * Looks through the database table Folders.  Finds all the current
     * sorting folders that still need to be sorted.  Adds them to the thumbnail
     * scroll pane and creates a listener when they are clicked on to open
     * the selected sort folder
     */
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

    /**
     * This is used to show the list of images in a sort folder.  If the folder
     * consists of images that do not have a date it unlocks date picker so
     * the user can add dates.
     * It goes through the folder object and adds all the image thumbnails to
     * the thumbnail viewport.  It creates a listener of what happens when the back
     * button clicked to return to base scene.
     * Clears the tilepane and adds photo thumbnails.  This method is also used when
     * restoring deleted images.It checks for missing thumbnails and if any are missing it
     * creates them.
     * @param folder Folder of images to be sorted
     * @param index Index of current position, 0 if entering folder, integer
     *              less than size of viewing photos if restoring deleted images.
     */
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
            folderScrollPane.setVmax(folderTilePane.getHeight() - folderScrollPane.getHeight());
           executor.shutdown();
        });
        root.requestFocus();
    }

    /**
     * Adds all thumbnails from arraylist to the thumbnail viewport.  It
     * adds listeners to handle when the thumbnail is clicked on.  The
     * listener adds the photo to selected list and displays it in the
     * mainImageView.  It allows for multiple images being selected.  If a photo
     * is marked as deleted it will not add it to the viewing photos list.
     * It then adds an image to the selected list and displays it.
     * @param photos List of photos from folder object.
     * @param index currently selected index position
     */
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

    /**
     * I do not remember why I synchronized this and maybe I could remove it.
     * But if aint broke do not touch it.
     * It applys the style to display which photos are currently selected, and
     * there status of favourite, like or delete.
     * @param selected Current list of selected photos
     */
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

    /**
     * Help method to find the photo that is stored inside of a VBox.
     * Searches the list of viewingPhotos and finds the matching VBox.
     * @param source VBox selected
     * @return The Photo object that contains the VBox from source
     */
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

    /**
     * starts the task of creating missing thumbnails.  Opens up the
     * progress bar and starts making them.
     */
    private void createMissingThumbnails() {
        root.requestFocus();
        Task missingThumbs = createMissingThumbs();
        ProgressDialog progress = new ProgressDialog(missingThumbs);
        progress.setContentText("Creating missing thumbnails");
        progress.setTitle("PhoSort");
        new Thread(missingThumbs).start();
        progress.showAndWait();
    }

    /**
     * Used to create any missing thumbnails that have been lost from intial
     * creation of the sorting projects.  It stores them in a folder called thumbnails
     * the thumbnail name is the name of the image plus the date.
     * @param file Image file to create a thumbnail
     * @param date Date when the photo was captured from either metadata or database
     */
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
            File out;
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

    /**
     * Searches through all the possible folders and finds the folder
     * containing the VBox.
     * @param folderBox selected VBox
     * @return Folder assoiciated with that VBox
     */
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

    /**
     * Allows the user to select a folder of images to start a sort folder.
     * It opens a directory chooser and then adds the directory to the
     * FolderBuilder object which creates the folders projects.
     * Then we run the addfolders method to add the folders to
     * the thumbnail viewport.
     */
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

    /**
     * When no image is selected to be loaded we load this image as place holder
     */
    private void loadNoImage() {
        width = mainImageView.getFitWidth();
        Image image = new Image(getClass().getResourceAsStream(Icon.NOIMAGE.fileName));
        addImageToImageView(image,mainImageView);
        mainImageView.setRotate(0);
    }

    /**
     * Takes the currently selected image and adds it the large image view port
     * This also runs the checkVisible method.
     * @param photo currently selected photo
     * @param imageView uneeded only on mainImageview likely could get rid of
     * @param rotation Applys rotation stored in the database of the photo.
     */
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
                Platform.runLater( () -> dateLabel.setText(currentPhoto.getDate()));

            }
        }
        VBox box = currentPhoto.getBox();
        checkIsVisible(box);
    }

    /**
     * Checks to see if the currently selected photo is in the scroll pane view port
     * If it is not in theviewport adjust the viewport so it is visible
     * @param box VBox containing the currently selected photo
     */
    private void checkIsVisible(VBox box) {
        double top =  folderScrollPane.getVvalue();
        double bottom = folderScrollPane.getVvalue() + folderScrollPane.getHeight();
        if(box.getLayoutY() < top){
            folderScrollPane.setVvalue(folderScrollPane.getVvalue() - (top - box.getLayoutY()));
        }
        else if(box.getLayoutY() + box.getHeight() > bottom){
            folderScrollPane.setVvalue(box.getLayoutY() + box.getHeight() - folderScrollPane.getHeight());
        }
    }

    /**
     * Applies the rotation of an image to the imageview.  This method is
     * a little odd as it accounts for some previous version of this program
     * that stored rotations differently.
     * @param imageView Imageview the image is in
     * @param rotation rotation grabbed from Photo object
     * @param thumbs Whether it is a thumbnail or not
     */
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

    /**
     * Primarily used to add no photo image to imageview.  Original idea was
     * to have it be smaller width so it was not so pixelated.  This messed
     * up the Photos when added to main image view so I commented out.
     * @param image Likely just the noImage image
     * @param imageView mainImageView
     */
    private void addImageToImageView(Image image, ImageView imageView){
        imageView.setImage(image);
        //imageView.setFitWidth(100);
        root.requestFocus();
    }

    /**
     * This adds an icon into the VBox's that are in the tilepane inside the scroll pane
     * The VBox's are used as there is an image and a label.  Sets the size and
     * ensures that they are 4 across.
     * @param imageTilePane Tilepane inside Scrollpane
     * @param image The image to add
     * @param name The name of the file, folder or project
     * @return Returns the VBox of the icons being added
     */
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

    /**
     * If in grab mode.  Searches through the favourites arraylist pulled from
     * the database.  Checks if it meets the dates requirements and adds it to
     * the current working project.
     * If in sort mode. It adds all the selected photos to be marked as favourited
     * If they are already marked favourited it changes them back to keep
     */
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

    /**
     * If in grab mode.  Searches through the Likes arraylist pulled from
     * the database.  Checks if it meets the dates requirements and adds it to
     * the current working project.
     * If in sort mode. It adds all the selected photos to be marked as liked
     * If they are already marked liked it changes them back to keep
     */
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
    /**
     * If in grab mode.  Searches through the Keeps arraylist pulled from
     * the database.  Checks if it meets the dates requirements and adds it to
     * the current working project.
     * If in sort mode. It adds all the selected photos to be marked as Keep
     */
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

    /**
     * if in grab mode removes the photo from the current project. Resets the thumbnails.
     * if in sort mode sets the status to deleted and removes it from the
     * viewing photos list.
     * In both modes it tracks the deleted items in a temporary list that
     * will disapear if leaving the project or folder.
     * This allows users to undue deleted items.
     */
    public void trashPressed() {
        ArrayList<Photo> deletes = new ArrayList<>();
        currentIndex = viewingPhotos.indexOf(currentPhoto);
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
            selected.clear();
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
            if (currentIndex > viewingPhotos.size() - 1) {
                currentIndex = viewingPhotos.size() - 1;
            }
            currentPhoto = viewingPhotos.get(currentIndex);
            selected.clear();
            selected.add(currentPhoto);
            applySelectedStyle(selected);
            addImageToImageView(currentPhoto, mainImageView, currentPhoto.getRotation());

        }
    }

    /**
     * If photos have been deleted it will restore them.  This only works if the
     * photos have been deleted in the current session.
     * If the user leaves the sort folder or project the lists of deleted
     * folders are wiped
     */
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

    /**
     * Takes the date as a string likely from the database and returns
     * a date object
     * @param stringDate String holding the date in the form yyyy-MM-dd
     * @return A date object from the given string
     * @throws ParseException Should not occur as users never enter strings
     */
    private Date stringDateToDate(String stringDate) throws ParseException {
        try {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.parse(stringDate);
        } catch (Exception e) {
            return new Date();
        }
    }

    /**
     * In grab mode it allows the user to choose a directory to save all the
     * selected images to.  Does not delete the project.  Allows users
     * to continue to open up and copy images.
     *
     * In sort mode it checks the status of every image in the sort folder.
     * It then copies them into sorted directories gotten from the settings.
     * The folders will be SettingsFolder/Year/Month
     * It then adds the photo to 1 of 4 databases: Favourite, Like, Keep, Deletes
     * It deletes the sort folder so it will no longer be visible in the basescene
     */
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
                    switch (photo.getStatus()) {
                        case "DELETED" -> {
                            String insert = "INSERT INTO DELETED (NAME, DATE) VALUES ('" + photo.getName() + "', '" + sqlDate + "')";
                            handler.execAction(insert);
                            try {
                                FileUtils.moveFile(photo.getFile(), new File(deleteFolder.getAbsolutePath() + "\\" + photo.getName()));
                            } catch (IOException e) {
                                Alert alert1 = new Alert(Alert.AlertType.WARNING);
                                alert1.setHeaderText("Failed to move file to deletes");
                                alert1.setContentText(photo.getName() + ": was set to be deleted but failed to move");
                            }

                        }
                        case "KEEP" -> {
                            String insert = "INSERT INTO KEEP (NAME, DATE, LOCATION, ROTATION) VALUES ('" + photo.getName() + "','" + sqlDate + "','" + destination + "','" + photo.getRotation() + "')";
                            handler.execAction(insert);
                            try {
                                FileUtils.moveFile(photo.getFile(), new File(destination));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        case "FAVOURITE" -> {
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

                        }
                        case "LIKE" -> {
                            String insert = "INSERT INTO LIKES (NAME, DATE, LOCATION, ROTATION) VALUES ('" + photo.getName() + "','" + sqlDate + "','" + destination + "','" + photo.getRotation() + "')";
                            handler.execAction(insert);
                            try {
                                FileUtils.moveFile(photo.getFile(), new File(destination));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

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

    /**
     * Used to keep sort folders in order.  WHen viewing folders in FileExplorer
     * they are alphabetical.  I prefered to be by date of the month.  This adds
     * a number to the beggining of the month folder so they will be arranged
     * chronoligically in File Explorer.
     * @param date Date the photo was taken
     * @return Month in the form #-month. Ex. 1-January
     */
    private String getMonthString(String date) {
        String value = "";
        String num = date.substring(5, 7);
        int number = Integer.parseInt(num);
        switch (number) {
            case 1 -> value = number + "-January";
            case 2 -> value = number + "-February";
            case 3 -> value = number + "-March";
            case 4 -> value = number + "-April";
            case 5 -> value = number + "-May";
            case 6 -> value = number + "-June";
            case 7 -> value = number + "-July";
            case 8 -> value = number + "-August";
            case 9 -> value = number + "-September";
            case 10 -> value = number + "-October";
            case 11 -> value = number + "-November";
            case 12 -> value = number + "-December";
        }
        return value;
    }

    /**
     * Used to get the year for making sort folders
     * @param date date of photo being sorted
     * @return The year in the form of a 4 letter string
     */
    private String getYearString(String date) {
        return date.substring(0, 4);
    }

    /**
     * Minimize to tray.
     */
    public void minimize() {
        stage.setIconified(true);
    }

    /**
     * Maximize to screen.
     */
    public void maximize() {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }

    /**
     * Closes window and exits program.
     */
    public void close() {
        System.exit(0);
    }

    /**
     * Detects key inputs for various tasks including:
     * navigating thumbnails with arrow keys
     * Multiple selection with shift and ctrl (similiar to Windows multiple
     * selection).
     * Can favourite, keep, like, delete.  F, K, L ,D respectively
     * @param keyEvent Which key was pressed
     */
    public void chooseSelected(KeyEvent keyEvent) {
        root.requestFocus();
        if (!isPhotoView && !isGrabbing) {
            System.out.println("NOT VIEWING");
            return;
        }
        int currentIndex;
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
                System.out.println(folderTilePane.getHeight());
                System.out.println(folderScrollPane.getVmax());
                System.out.println(folderScrollPane.getVvalue());
                System.out.println(folderScrollPane.getHeight());
                System.out.println(newCurrent.getBox().getLayoutY());
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
            if (currentIndex > viewingPhotos.size() - 4) {
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

    /**
     * Open settings window to change the settings
     */
    public void openSettingsChanger() {
        loadWindow("/layouts/settings.fxml");
    }

    /**
     * Used to open new windows, currently only settings window is used
     * @param location url of FXML file
     */
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

    /**
     * Compares date of a photo to date of the date picker.
     * @param imageDate Date of image
     * @param toDate Date on date picker
     * @return true if the imageDate is earlier than the toDate
     */
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
                //System.out.println("image day greater");
                return imageDay <= toDay; //Same year same month greather than day
            }
        }
        return true;
    }

    /**
     * Have to convert the year of the datepicker object for some annoying reason.
     * @param year Weird integer from datepicker year
     * @return The proper year to it should be
     */
    private int convertYear(int year){
        return 1900 + year;
    }

    /**
     * Compares the date of an image to the date of the datepicker
     * @param imageDate Date of image
     * @param fromDate Date of fromDate picker
     * @return true if the imageDate is later than the fromDate
     */
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
                return imageDay >= toDay; //Same year same month greather than day
            }
        }
        return true;
    }

    /**
     * surprised this is not needed my be issue.  TODO Check this
     */
    public void keep() {
    }

    /**
     * Selected from menu to enter grab mode.
     */
    public void grabMode() {
        if(!isPhotoView) {
            isGrabMode = true;
            isSortingMode = false;
            baseScene();
        }
    }

    /**
     * Selected fro menu to enter sortMode
     */
    public void sortMode() {
        if(!isGrabbing) {
            isSortingMode = true;
            isGrabMode = false;
            baseScene();
        }
    }

    /**
     * Sets the date of an image that has no metadata of date.
     * IT allows the user to keep a date in the database so the image can
     * be pulled later.
     */
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

    /**
     * It searches through the entire database and makes a list of any files
     * that are not in the location according to the database.
     */
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

    /**
     * If a project is no longer needed it can be deleted.  Photos are not
     * deleted just the database table.
     */
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

    /**
     * Allows user to have a starting point of a project.
     */
    public void copyProject() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Not functioning yet.");
        alert.showAndWait();
    }

    /**
     * TODO
     * Ability to migrate entire database and images.  This may be needed
     * if the current location of the database is getting full.  Or wants to
     * stored externally.
     */
    public void migrateDatabase() {

    }
}