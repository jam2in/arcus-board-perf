package com.jam2in.arcus.board.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostPage {
	Post post;
	Board board;
	List<Board> boardList;
	List<Category> boardCategory;
	List<Category> postCategory;
	Pagination pagination;
	List<Comment> cmtList;
	Comment comment;
}
