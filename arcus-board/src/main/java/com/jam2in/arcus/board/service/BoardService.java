package com.jam2in.arcus.board.service;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.spy.memcached.ArcusClientPool;

import com.jam2in.arcus.board.configuration.ArcusConfiguration;
import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.repository.BoardRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;

    ApplicationContext context = new AnnotationConfigApplicationContext(ArcusConfiguration.class);
    ArcusClientPool arcusClient = context.getBean("arcusClient", ArcusClientPool.class);

    @Transactional(readOnly = true)
    public Board selectOneBoard(int id) {
        return boardRepository.selectOne(id);
    }

    public List<Board> selectAllBoard() {
        List<Board> boardList = null;
        Future<Object> future = arcusClient.asyncGet("BoardList");
        try {
            boardList = (List<Board>) future.get(700L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            e.printStackTrace();
        }

        if (boardList == null) {
            boardList = boardRepository.selectAll();
            arcusClient.set("BoardList", 3600, boardList);
        }

        return boardList;
    }

    public List<Board> bestBoard(int period) {
        List<Board> bestBoard = null;

        switch (period) {
            case 0 :
                Future<Object> future = arcusClient.asyncGet("BestBoardRecent");
                try {
                    bestBoard = (List<Board>) future.get(700L, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    future.cancel(true);
                    e.printStackTrace();
                }

                if (bestBoard == null) {
                    bestBoard = boardRepository.bestBoardRecent();
                    arcusClient.set("BestBoardRecent", 600, bestBoard);
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
        Future<Object> future = arcusClient.asyncGet("Category:Board");
        try {
            boardCategory = (List<Category>) future.get(1000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            e.printStackTrace();
        }

        if (boardCategory == null) {
            boardCategory = boardRepository.boardCategoryAll();
            arcusClient.set("Category:Board", 3600, boardCategory);
        }

        return boardCategory;
    }
}
