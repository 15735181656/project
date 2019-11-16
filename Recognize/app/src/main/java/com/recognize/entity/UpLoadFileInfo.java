package com.recognize.entity;

public class UpLoadFileInfo {

    private byte[] fileBuf;
    private String uploadFileName;

    public UpLoadFileInfo(byte[] fileBuf, String uploadFileName) {
        this.fileBuf = fileBuf;
        this.uploadFileName = uploadFileName;
    }


    public byte[] getFileBuf() {
        return fileBuf;
    }

    public void setFileBuf(byte[] fileBuf) {
        this.fileBuf = fileBuf;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }


}
