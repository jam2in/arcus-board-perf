package com.jam2in.arcus.board.repository;

import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.Category;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface BoardRepository {
    void insert(Board board);
    void update(Board board);
    void delete(int id);
    Board selectOne(int id);
    List<Board> selectAll();

    List<Board> bestBoardRecent();
    List<Board> bestBoardToday();

    void increaseReqRecent(int bid);
    void increaseReqToday(int bid);
    void resetReqRecent();
    void resetReqToday();

    List<Category> boardCategoryAll();
}
