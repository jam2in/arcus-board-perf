package com.jam2in.arcus.board.repository;

import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.model.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PostRepository {
    void insert(Post post);
    void update(Post post);
    void delete(int pid);

    Post selectOne(int pid);
    List<Post> selectAll(@Param("bid") int bid, @Param("startList") int startList, @Param("pageSize") int pageSize);
    List<Post> selectCategory(@Param("bid") int bid, @Param("category") int category, @Param("startList") int startList, @Param("pageSize") int pageSize);

    List<Integer> bestLikesAll();
    List<Integer> bestLikesMonth();
    List<Integer> bestLikesToday();
    List<Integer> bestViewsAll();
    List<Integer> bestViewsMonth();
    List<Integer> bestViewsToday();
    List<Integer> bestLikesAllBoard(int bid);
    List<Integer> bestLikesMonthBoard(int bid);
    List<Integer> bestLikesTodayBoard(int bid);
    List<Integer> bestViewsAllBoard(int bid);
    List<Integer> bestViewsMonthBoard(int bid);
    List<Integer> bestViewsTodayBoard(int bid);

    int countPost(int bid);
    int countPostCategory(@Param("bid") int bid, @Param("category") int category);

    void increaseCmt(int pid);
    void decreaseCmt(int pid);

    void increaseViews(int pid);
    void likePost(int pid);

    List<Category> postCategoryAll();
}
