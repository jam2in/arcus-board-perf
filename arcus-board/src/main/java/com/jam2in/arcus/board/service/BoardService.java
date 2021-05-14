package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;

    @Transactional(readOnly = true)
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

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void resetReq() {
        boardRepository.resetReqRecent();
        boardRepository.resetReqToday();
    }

    public List<Category> boardCategoryAll() {
        return boardRepository.boardCategoryAll();
    }
}
