package com.example.vclab.howtalk2.model;

/**
 * Created by Aiden on 2018-05-01.
 */

public class NotificationModel {
    public String to;
    public Notification notification = new Notification();

    public static class Notification {
        public String title;
        public String text;
    }
}
