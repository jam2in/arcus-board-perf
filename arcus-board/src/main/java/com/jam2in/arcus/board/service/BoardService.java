package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.BoardPage;
import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.repository.BoardRepository;
import com.jam2in.arcus.board.repository.LeaderBoardRepository;
import com.jam2in.arcus.board.repository.PostRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private LeaderBoardRepository leaderBoardRepository;

    public BoardPage homePage(int likesPeriod, int viewsPeriod, int boardPeriod) {
        List<Board> bestBoard;
        switch (boardPeriod) {
            case 0 :
                bestBoard = boardRepository.bestBoardRecent();
                break;
            case 1 :
                bestBoard = boardRepository.bestBoardToday();
                break;
            default :
                throw new IllegalArgumentException("Illegal Best Board Period");
        }

        BoardPage homePage = BoardPage.builder()
            .noticeList(postRepository.selectLatestNotice(1))
            .boardList(boardRepository.selectAll())
            .boardCategory(boardRepository.boardCategoryAll())
            .bestLikes(leaderBoardRepository.bestLikesAll(likesPeriod))
            .bestViews(leaderBoardRepository.bestViewsAll(viewsPeriod))
            .bestBoard(bestBoard)
            .build();

        return homePage;
    }

    public BoardPage boardPage(int bid, int groupIndex, int pageIndex, int likesPeriod, int viewsPeriod) {
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);

        Pagination pagination = Pagination.builder().groupSize(10).pageSize(20).build();
        pagination.pageInfo(groupIndex, pageIndex, postRepository.countPost(bid));

        BoardPage boardPage = BoardPage.builder()
            .board(boardRepository.selectOne(bid))
            .pagination(pagination)
            .postList(postRepository.selectAll(bid, pagination.getStartList()-1, pagination.getPageSize()))
            .noticeList(postRepository.selectLatestNotice(bid))
            .boardList(boardRepository.selectAll())
            .boardCategory(boardRepository.boardCategoryAll())
            .postCategory(postRepository.postCategoryAll())
            .bestLikes(leaderBoardRepository.bestLikesBoard(bid, likesPeriod))
            .bestViews(leaderBoardRepository.bestViewsBoard(bid, viewsPeriod))
            .build();

        return boardPage;
    }

    public BoardPage boardPage(int bid, int category, int groupIndex, int pageIndex, int likesPeriod, int viewsPeriod) {
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);

        Pagination pagination = Pagination.builder().groupSize(10).pageSize(20).build();
        pagination.pageInfo(groupIndex, pageIndex, postRepository.countPostCategory(bid, category));

        BoardPage boardPage = BoardPage.builder()
            .board(boardRepository.selectOne(bid))
            .pagination(pagination)
            .postList(postRepository.selectCategory(bid, category, pagination.getStartList()-1, pagination.getPageSize()))
            .noticeList(postRepository.selectLatestNotice(bid))
            .boardList(boardRepository.selectAll())
            .boardCategory(boardRepository.boardCategoryAll())
            .postCategory(postRepository.postCategoryAll())
            .bestLikes(leaderBoardRepository.bestLikesBoard(bid, likesPeriod))
            .bestViews(leaderBoardRepository.bestViewsBoard(bid, viewsPeriod))
            .build();

        return boardPage;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetReq() {
        boardRepository.resetReqRecent();
        boardRepository.resetReqToday();
    }
}
