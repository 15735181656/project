package com.recognize.recognize;

public class SplitFriends {
//    public static void main(String[] args) {
//        String s = "0";
//        String[] split = split(s);
//        for(int i=0;i<split.length;i++)
//            System.out.println(split[i]);
//    }
    public SplitFriends(){

    }
    public String[] split(String s) {
        String[] split = s.split(",");
        System.out.println(split[0]);
        return  split;
    }
}
