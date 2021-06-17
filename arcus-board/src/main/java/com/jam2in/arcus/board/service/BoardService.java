package com.jam2in.arcus.board.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.spy.memcached.ArcusClientPool;

import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.repository.BoardRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ArcusClientPool arcusClient;

    @Transactional(readOnly = true)
    public Board selectOneBoard(int id) {
        return selectAllBoard().get(id-1);
    }

    public List<Board> selectAllBoard() {
        List<Board> boardList = null;
        Future<Object> future = null;
        try {
            future = arcusClient.asyncGet("BoardList");
            boardList = (List<Board>)future.get(700L, TimeUnit.MILLISECONDS);
            if (boardList == null) {
                boardList = boardRepository.selectAll();
                arcusClient.set("BoardList", 3600, boardList);
            }
        } catch (Exception e) {
            Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
            log.error(e.getMessage(), e);
        }

        return boardList;
    }

    public List<Board> bestBoard(int period) {
        List<Board> bestBoard = null;
        switch (period) {
            case 0 :
                Future<Object> future = null;
                try {
                    future = arcusClient.asyncGet("BestBoardRecent");
                    bestBoard = (List<Board>)future.get(700L, TimeUnit.MILLISECONDS);
                    if (bestBoard == null) {
                        bestBoard = boardRepository.bestBoardRecent();
                        arcusClient.set("BestBoardRecent", 600, bestBoard);
                    }
                } catch (Exception e) {
                    Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
                    log.error(e.getMessage(), e);
                }
                break;
            case 1 :
                bestBoard = boardRepository.bestBoardToday();
                break;
        }

        return bestBoard;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void resetReq() {
        boardRepository.resetReqRecent();
        boardRepository.resetReqToday();
    }

    public List<Category> boardCategoryAll() {
        List<Category> boardCategory = null;
        Future<Object> future = null;
        try {
            future = arcusClient.asyncGet("Category:Board");
            boardCategory = (List<Category>)future.get(1000L, TimeUnit.MILLISECONDS);
            if (boardCategory == null) {
                boardCategory = boardRepository.boardCategoryAll();
                arcusClient.set("Category:Board", 3600, boardCategory);
            }
        } catch (Exception e) {
            Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
            log.error(e.getMessage(), e);
        }

        return boardCategory;
    }
}
