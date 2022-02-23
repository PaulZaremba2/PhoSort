package com.zaremba.phosort.tools;

import javafx.scene.control.Alert;

import javax.swing.*;
import java.sql.*;

public class DatabaseHandler {
    private static final String DB_url = "jdbc:derby:database/phosort;create=true";
    private static Connection conn = null;
    private static Statement stmt = null;
    public static DatabaseHandler handler;

    public static DatabaseHandler getHandler(){
        if(handler == null){
            handler = new DatabaseHandler();
            return handler;
        }else{
            return handler;
        }
    }

    public DatabaseHandler() {

        createConnection();
        setupDataBase();
    }

    private void setupDataBase() {
        String folders = "FOLDERS";
        String keep = "KEEP";
        String settings = "SETTINGS";
        String favourites = "FAVOURITES";
        String like = "LIKES";
        String deleted = "DELETED";
        String projects = "PROJECTS";
        DatabaseMetaData dmn;
        try {
            stmt = conn.createStatement();
            dmn = conn.getMetaData();
            ResultSet tables = dmn.getTables(null, null, projects, null);
            if (tables.next()) {
                System.out.println("Table " + projects + " already exists");
            } else {
                String statement = "CREATE TABLE " + projects + "("
                        + "name varchar(200), \n"
                        + "location varchar(200))";
                System.out.println(statement);
                stmt.execute(statement);
                tables.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " setting up table");
        }
        try {
            stmt = conn.createStatement();
            dmn = conn.getMetaData();
            ResultSet tables = dmn.getTables(null, null, folders, null);
            if (tables.next()) {
                System.out.println("Table " + folders + " already exists");
            } else {
                String statement = "CREATE TABLE " + folders + "("
                        + "name varchar(200), \n"
                        + "location varchar(200))";
                System.out.println(statement);
                stmt.execute(statement);
                tables.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " setting up table");
        }
        try {
            stmt = conn.createStatement();
            dmn = conn.getMetaData();
            ResultSet tables = dmn.getTables(null, null, deleted, null);
            if (tables.next()) {
                System.out.println("Table " + deleted + " already exists");
            } else {
                String statement = "CREATE TABLE " + deleted + "("
                        + "name varchar(200),\n"
                        + "DATE date)";
                System.out.println(statement);
                stmt.execute(statement);
                tables.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " setting up table");
        }
        try {
            stmt = conn.createStatement();
            dmn = conn.getMetaData();
            ResultSet tables = dmn.getTables(null, null, keep, null);
            if (tables.next()) {
                System.out.println("Table " + keep + " already exists");
            } else {
                String statement = "CREATE TABLE " + keep + "("
                        + "name varchar(200), \n"
                        + "DATE date, \n"
                        + "location varchar(200),"
                        + "rotation varchar(255))";
                System.out.println(statement);
                stmt.execute(statement);
                tables.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " setting up table");
        }
        try {
            stmt = conn.createStatement();
            dmn = conn.getMetaData();
            ResultSet tables = dmn.getTables(null, null, settings, null);
            if (tables.next()) {
                System.out.println("Table " + settings + " already exists");
            } else {
                String statement = "CREATE TABLE " + settings + "("
                        + "variable varchar(200), \n"
                        + "value varchar(200))";
                System.out.println(statement);
                stmt.execute(statement);
                tables.close();
                String userDir = System.getProperty("user.home");
                execAction("INSERT INTO SETTINGS (VARIABLE, VALUE) VALUES ('sortFolder','" + userDir + "/Pictures/PhoSort/Sorted')" );
                execAction("INSERT INTO SETTINGS (VARIABLE, VALUE) VALUES ('deleteFolder','" + userDir + "/Pictures/PhoSort/Deleted')" );
                execAction("INSERT INTO SETTINGS (VARIABLE, VALUE) VALUES ('videoFolder','" + userDir + "/Videos')" );
                execAction("INSERT INTO SETTINGS (VARIABLE, VALUE) VALUES ('duplicateFolder','" + userDir + "/Pictures/PhoSort/Duplicates')" );
                execAction("INSERT INTO SETTINGS (VARIABLE, VALUE) VALUES ('userPictureFolder','" + userDir + "/Pictures')" );
                execAction("INSERT INTO SETTINGS (VARIABLE, VALUE) VALUES ('firstTime','true')" );
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " setting up table");
        }
        try {
            stmt = conn.createStatement();
            dmn = conn.getMetaData();
            ResultSet tables = dmn.getTables(null, null, favourites, null);
            if (tables.next()) {
                System.out.println("Table " + favourites + " already exists");
            } else {
                String statement = "CREATE TABLE " + favourites + "("
                        + "name varchar(200), \n"
                        + "DATE date, \n"
                        + "location varchar(200),"
                        + "rotation varchar(255))";
                System.out.println(statement);
                stmt.execute(statement);
                tables.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " setting up table");
        }

        try {
            stmt = conn.createStatement();
            dmn = conn.getMetaData();
            ResultSet tables = dmn.getTables(null, null, "LIKES", null);
            if (tables.next()) {
                System.out.println("Table " + "LIKES" + " already exists");
            } else {
                String statement = "CREATE TABLE " + like + "("
                        + "name varchar(200), \n"
                        + "DATE date, \n"
                        + "location varchar(200),"
                        + "rotation varchar(255))";
                System.out.println(statement);
                stmt.execute(statement);
                tables.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " setting up table");
        }

    }

    public boolean createImageFolderTable(String tableName) {
        String TABLE_NAME = tableName;
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dmn = conn.getMetaData();
            ResultSet tables = dmn.getTables(null, null, TABLE_NAME, null);
            if (tables.next()) {
                System.out.println("Table " + TABLE_NAME + " already exists");
                return true;
            } else {
                String statement = "CREATE TABLE " + TABLE_NAME + "("
                        + "fileLocation varchar(200), \n"
                        + "fileName varchar(200), \n"
                        + "status varchar(200),"
                        + "rotation varchar(255),"
                        + "DATE date)";
                System.out.println(statement);
                stmt.execute(statement);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " setting up table");
        }
        return false;
    }

    public void createImageWithNoDateTable() {
        String TABLE_NAME = "NODATE";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dmn = conn.getMetaData();
            ResultSet tables = dmn.getTables(null, null, TABLE_NAME, null);
            if (tables.next()) {
                System.out.println("Table " + TABLE_NAME + " already exists");
                return;
            } else {
                String statement = "CREATE TABLE " + TABLE_NAME + "("
                        + "fileLocation varchar(200), \n"
                        + "fileName varchar(200), \n"
                        + "status varchar(200),"
                        + "rotation varchar(255),"
                        + "DATE varchar(255))";
                System.out.println(statement);
                stmt.execute(statement);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " setting up table");
        }
    }

    public boolean checkTableExists(String tableName){
        String TABLE_NAME = tableName;
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dmn = conn.getMetaData();
            ResultSet tables = dmn.getTables(null, null, TABLE_NAME.toUpperCase(), null);
            if (tables.next()) {
                System.out.println("Table " + TABLE_NAME + " already exists");
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " checking table exists");
        }
        return false;
    }

    private void createConnection() {
        try {
            //Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            conn = DriverManager.getConnection(DB_url);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.exit(1);
        }
    }

    public boolean execAction(String qu) {
        try {
            stmt = conn.createStatement();
            stmt.execute(qu);
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "ERROR: " + e.getMessage());
            return false;
        }
    }

    public boolean execUpdate(String qu){
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(qu);
            System.out.println(qu);
            return true;
        } catch (SQLSyntaxErrorException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Database update Failed: \n" + qu + "\n");
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Database update Failed: \n" + qu + "\n");
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet execQuery (String query){
        ResultSet resultSet;
        try{
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);
        }catch(SQLException e){
            System.out.println("Exception at execute query");
            System.out.println(e.getErrorCode());
            System.out.println(query);
            e.printStackTrace();
            return null;
        }
        return resultSet;
    }
}
