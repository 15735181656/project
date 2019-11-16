package com.recognize.utils;


import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeGetUntil {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); //创建对象制定日期格式
        String date = format.format(new Date());

        return date;
    }
}
