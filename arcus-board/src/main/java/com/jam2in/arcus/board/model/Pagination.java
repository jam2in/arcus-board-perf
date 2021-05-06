package com.jam2in.arcus.board.model;
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

    public int getListCnt() {
        return listCnt;
    }

    public void setListCnt(int listCnt) {
        this.listCnt = listCnt;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getStartList() {
        return startList;
    }

    public void setStartList(int startList) {
        this.startList = startList;
    }

    public boolean isPrev() {
        return prev;
    }

    public void setPrev(boolean prev) {
        this.prev = prev;
    }

    public boolean isNext() {
        return next;
    }

    public void setNext(boolean next) {
        this.next = next;
    }

    public int getPageCnt() {
        return pageCnt;
    }

    public void setPageCnt(int pageCnt) {
        this.pageCnt = pageCnt;
    }

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