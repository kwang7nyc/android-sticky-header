package com.kwang7.stickyheader.model;

public class ListItem {

    public final String title;
    public final String description;
    public final boolean isHeader;

    public ListItem(String title, String description, boolean isHeader) {
        this.title = title;
        this.description = description;
        this.isHeader = isHeader;
    }
}
