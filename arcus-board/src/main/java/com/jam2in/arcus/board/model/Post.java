package com.jam2in.arcus.board.model;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Post implements Serializable {
    private int pid;
    private int uid;
    private String userName;
    private int bid;
    private int category;
    private String categoryName;
    private String title;
    private String content;
    private int views;
    private int likes;
    private int cmtCnt;
    private Timestamp createdDate;
    private Timestamp updatedDate;
}
