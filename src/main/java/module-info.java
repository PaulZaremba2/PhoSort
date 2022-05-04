module com.zaremba.phosort.phosort {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.sql;
    requires java.desktop;
    requires metadata.extractor;
    requires org.apache.commons.io;
    requires com.twelvemonkeys.common.image;
    requires com.twelvemonkeys.common.io;
    requires com.twelvemonkeys.common.lang;
    requires com.twelvemonkeys.contrib;
    requires com.twelvemonkeys.imageio.core;
    requires com.twelvemonkeys.imageio.metadata;
    requires org.apache.derby.commons;
    requires org.apache.derby.engine;


    exports com.zaremba.phosort.ui;
    opens com.zaremba.phosort.ui to javafx.fxml;
    opens com.zaremba.phosort.tools;
}