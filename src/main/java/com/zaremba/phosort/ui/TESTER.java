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
import org.apache.commons.io.FileUtils;

import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;
import java.io.File;
import java.io.IOException;
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
        String path = System.getProperty("user.home");
        System.out.println(path);
    }
}