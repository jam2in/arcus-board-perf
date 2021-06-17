package com.jam2in.arcus.board.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.Category;

@Mapper
@Repository
public interface BoardRepository {
    Board selectOne(int id);
    List<Board> selectAll();

    List<Board> bestBoardRecent();
    List<Board> bestBoardToday();

    void increaseReqRecent(int bid);
    void increaseReqToday(int bid);
    void updateReqRecent(@Param("bid") int bid, @Param("count") int count);
    void updateReqToday(@Param("bid") int bid, @Param("count") int count);
    void resetReqRecent();
    void resetReqToday();

    List<Category> boardCategoryAll();
}
