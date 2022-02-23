package com.zaremba.phosort.tools;

public enum Rotations {
    NORMAL("(Horizontal / normal)", 0),
    ROTATE270("(Rotate 270 CW)",270),
    ROTATE90("(Rotate 90 CW)",90),
    ROTATE0("(0)",0),
    ROTATE180("(Rotate 180)",180),
    OLDNORMAL("Normal",0),
    OLD90("Rotate90", 90),
    OLD270("Rotate270", 270),
    OLD180("Rotate180", 180);

    public String fileName;
    public double rotation;

    Rotations(String fileName, double rotation) {
        this.fileName = fileName;
        this.rotation = rotation;
    }
}
