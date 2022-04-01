package com.kwang7.stickheader.model;


public class SectionItem implements SectionHeader {

    public final String title;
    public int color = 0xff777777;

    public SectionItem(String title) {
        this.title = title;
    }
}
