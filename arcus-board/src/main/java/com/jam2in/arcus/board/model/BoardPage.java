package com.jam2in.arcus.board.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardPage {
	Board board;
	Pagination pagination;
	List<Post> postList;
	List<Post> noticeList;
	List<Board> boardList;
	List<Category> boardCategory;
	List<Category> postCategory;
	List<Post> bestLikes;
	List<Post> bestViews;
	List<Board> bestBoard;
}
