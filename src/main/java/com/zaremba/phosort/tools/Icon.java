package com.zaremba.phosort.tools;

public enum Icon {
    ADDFOLDERICON("/icons/addFolderIcon.png"),
    RESTOREICON("/icons/restoreIcon.png"),
    CALENDARICON("/icons/calendarIcon.png"),
    FAVOURITEICON("/icons/favouriteIcon.png"),
    FAVOURITEICONSELECTED("/icons/favouriteIconSelected.png"),
    FOLDERICON("/icons/folderIcon.png"),
    LIKEICON("/icons/likeIcon.png"),
    LIKEICONSELECTED("/icons/likeIconSelected.png"),
    FINISHEDICON("/icons/finishedIcon.png"),
    NOIMAGE("/icons/noImage.png"),
    TRASHICON("/icons/trashIcon.png"),
    GRAB("/icons/grab.png"),
    UPLEVELICON("/icons/uplevelicon.png");

    public String fileName;

    Icon(String fileName){
        this.fileName = fileName;
    }
}
