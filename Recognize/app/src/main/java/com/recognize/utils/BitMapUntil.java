package com.recognize.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 17-11-12.
 */
public class BitMapUntil {

    // 饿汉式
    private static BitMapUntil instance = new BitMapUntil();

    public BitMapUntil(){}

    public static BitMapUntil getInstance(){
        return instance;
    }

    /*
    *    get image from network
    *    @param [String]imageURL
    *    @return [BitMapUntil]image
    */
    public Bitmap urlToBitMap(String url){
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}