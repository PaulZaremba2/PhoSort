package com.zaremba.phosort.tools;

public enum Status {
    FAVOURITE("FAVOURITE"),
    LIKE("LIKE"),
    KEEP("KEEP");

    String status;

    Status(String status){
        this.status = status;
    }
}
