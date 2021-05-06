package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;

    public void insertBoard(Board board) {
        boardRepository.insert(board);
    }

    public void updateBoard(Board board) {
        boardRepository.update(board);
    }

    public void deleteBoard(int id) {
        boardRepository.delete(id);
    }

    public Board selectOneBoard(int id) {
        return boardRepository.selectOne(id);
    }

    public List<Board> selectAllBoard() {
        return boardRepository.selectAll();
    }

    public List<Board> bestBoard(int period) {
        switch (period) {
            case 0 :
                return boardRepository.bestBoardRecent();
            case 1 :
                return boardRepository.bestBoardToday();
        }
        throw new IllegalArgumentException("Illegal Best Board Period");
    }

    public void increaseReq(int bid) {
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetReqRecent() {
        boardRepository.resetReqRecent();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetReqToday() {
        boardRepository.resetReqToday();
    }

    public List<Category> boardCategoryAll() {
        return boardRepository.boardCategoryAll();
    }
}
