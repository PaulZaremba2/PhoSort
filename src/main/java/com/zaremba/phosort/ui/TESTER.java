package com.zaremba.phosort.ui;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.twelvemonkeys.contrib.exif.EXIFUtilities;
import com.twelvemonkeys.contrib.exif.Orientation;
import com.zaremba.phosort.tools.DatabaseHandler;
import org.apache.commons.io.FileUtils;

import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TESTER {
    public static ArrayList<String> rotations = new ArrayList<>();

    private static String getRotation(File file){
        String rotation = null;
        try{
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifIFD0Directory direct = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            ExifIFD0Descriptor descriptor = new ExifIFD0Descriptor(direct);
            rotation = descriptor.getOrientationDescription();
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotation;
    }

    public static void main(String[] args) {
        /*File[] files;
        File file = new File("C:\\Users\\pzare\\Pictures\\SortingDir\\2018 May");
        files = file.listFiles();
        for (File f : files) {
            String rot = getRotation(f);
            String databaseRot = rot.substring(rot.indexOf("("));
            System.out.println(databaseRot);
        }*/
        DatabaseHandler handler = DatabaseHandler.getHandler();
        ResultSet rs = handler.execQuery("SELECT * FROM KEEP");
        String month = "";
        String year = "";
        String newPath = "c:\\path\\to\\myfile\\";
        String file1 = "C:\\Users\\pzaremba\\Desktop\\testing\\Sorted\\2020\\6-June\\Screenshot_20220329-122642_Chrome.jpg";
        String date1 = "2020-06-10";
        String file2 = "C:\\Users\\pzaremba\\Desktop\\testing\\Sorted\\2022\\3-March\\20220326_145645.jpg";
        String date2 = "2022-03-26";
        int count = 0;
        try{
            while (count < 2) {
                if (count == 0) {
                    String date = date1;
                    String oldPath = file1;
                    String[] splitter = date.split("-");
                    month = splitter[1];
                    year = splitter[0];
                    int index = oldPath.indexOf(year + "\\" + getMonth(month));
                    System.out.println(newPath + oldPath.substring(index));

                } else if (count == 1) {
                    String date = date2;
                    String oldPath = file2;
                    String[] splitter = date.split("-");
                    month = splitter[1];
                    year = splitter[0];
                    int index = oldPath.indexOf(year + "\\" + getMonth(month));
                    System.out.println(newPath + oldPath.substring(index));
                }
                count++;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static String getMonth(String month) {
        if (month.equals("06")) {
            return "6-June";
        } else if (month.equals("03")) {
            return "3-March";
        }
        return "error";
    }
}