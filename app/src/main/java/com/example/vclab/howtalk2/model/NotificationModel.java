package com.example.vclab.howtalk2.model;

/**
 * Created by Aiden on 2018-05-01.
 */

public class NotificationModel {
    public String to;
    public Notification notification = new Notification();
    public  Data data = new Data();

    public static class Notification { // 백그라운드
        public String title;
        public String text;
    }

    public static class Data { // 포그라운드
        public String title;
        public String text;
    }
}
