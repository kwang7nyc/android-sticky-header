package com.kwang7.stickheader.model;


public class HeaderItem implements StickyHeaderModel {

    public final String title;
    public int color = 0xff777777;

    public HeaderItem(String title) {
        this.title = title;
    }
}
