package com.jam2in.arcus.board.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.jam2in.arcus.board.model.BestPost;
import com.jam2in.arcus.board.model.Post;

@Mapper
@Repository
public interface LeaderBoardRepository {
	void resetBestLikesAll();
	void resetBestLikesBoard();
	void resetBestViewsAll();
	void resetBestViewsBoard();

	void insertBestLikesAll(List<BestPost> bestPostList);
	void insertBestLikesBoard(List<BestPost> bestPostList);
	void insertBestViewsAll(List<BestPost> bestPostList);
	void insertBestViewsBoard(List<BestPost> bestPostList);

	List<Post> bestLikesAll(int period);
	List<Post> bestLikesBoard(@Param("bid") int bid,@Param("period") int period);
	List<Post> bestViewsAll(int period);
	List<Post> bestViewsBoard(@Param("bid") int bid,@Param("period") int period);
}
