package com.jam2in.arcus.board.model;


import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comment {
    private int cid;
    private int uid;
    private String userName;
    private int pid;
    private int bid;
    private String content;
    private Timestamp createdDate;
    private Timestamp updatedDate;
}
