package com.recognize.entity;

public class Information {
    private String url;
    private String name;
    private boolean isFocus;

    public Information(String name, String url, boolean isFocus){
        this.name = name;
        this.url = url;
        this.isFocus = isFocus;
    }

    public String getName(){
        return name;
    }

    public boolean getIsFocus(){
        return isFocus;
    }

    public void setIsFocus(boolean isFocus){
        this.isFocus = isFocus;
    }

    public String getUrl() {
        return url;
    }
}
