package com.jam2in.arcus.board.model;

import java.io.Serializable;

import lombok.Getter;

@Getter
public class Board implements Serializable {
    private int bid;
    private String name;
    private int category;
}
