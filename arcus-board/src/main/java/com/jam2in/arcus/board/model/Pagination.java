package com.jam2in.arcus.board.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Pagination {

    private int listCnt;
    private int pageSize;
    private int groupSize;
    private int groupIndex;
    private int pageIndex;
    private int startRow;
    private int endRow;
    private int startList;
    private boolean prev;
    private boolean next;
    private int pageCnt;

    public void pageInfo(int groupIndex, int pageIndex, int listCnt) {
        this.groupIndex = groupIndex;
        this.pageIndex = pageIndex;
        this.listCnt = listCnt;

        //total number of page
        this.pageCnt = (int) Math.ceil((double) listCnt / (double) pageSize);

        // first, last index of the page
        this.startRow = (groupIndex - 1) * groupSize + 1;
        this.endRow = groupIndex * groupSize;

        //starting index of the post
        this.startList = (pageIndex - 1) * pageSize + 1;

        //Previous Button
        this.prev = (groupIndex != 1);

        //Next Button
        this.next = (endRow < pageCnt);
        if (this.endRow > this.pageCnt) {
            if (pageCnt == 0) this.endRow = this.startRow;
            else this.endRow = this.pageCnt;
        }
    }
}