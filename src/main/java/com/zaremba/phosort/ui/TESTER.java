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
import com.drew.metadata.mp4.Mp4Directory;
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
import java.util.Date;
import java.util.logging.Logger;

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
        File image = new File("IMG_2924.JPG");
        try{
            Metadata metadata = ImageMetadataReader.readMetadata(image);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
            System.out.println(date);
        }catch (ImageProcessingException e) {
            e.printStackTrace();
            System.out.println("Getting Date of file:" + image.getName());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Getting Date of file:" + image.getName());
        }
    }
}