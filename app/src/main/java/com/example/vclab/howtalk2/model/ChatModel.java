package com.example.vclab.howtalk2.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aiden on 2018-04-28.
 */

public class ChatModel {
    public Map<String, Boolean> users = new HashMap<>();     // 채팅방 유저들
    public Map<String, Comment> comments = new HashMap<>(); // 채팅방의 대화내용

    public static class Comment{
        public String uid;
        public String message;
    }

}
