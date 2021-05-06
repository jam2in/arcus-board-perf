package com.jam2in.arcus.board.controller;

import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.service.BoardService;
import com.jam2in.arcus.board.service.LeaderBoardService;
import com.jam2in.arcus.board.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BoardController {
    @Autowired
    private BoardService boardService;

    @Autowired
    private PostService postService;

    @Autowired
    private LeaderBoardService leaderBoardService;

    @RequestMapping(path = "/")
    public String home(@RequestParam(defaultValue = "0") int likesPeriod, @RequestParam(defaultValue = "0") int viewsPeriod, @RequestParam(defaultValue = "0") int boardPeriod, Model model) {
        List<Post> noticeList = postService.selectLatestNotice(1);
        List<Board> boardList = boardService.selectAllBoard();
        List<Category> boardCategory = boardService.boardCategoryAll();

        List<Post> bestLikes = leaderBoardService.bestLikesAll(likesPeriod);
        List<Post> bestViews = leaderBoardService.bestViewsAll(viewsPeriod);
        List<Board> bestBoard = boardService.bestBoard(boardPeriod);

        model.addAttribute("noticeList", noticeList);
        model.addAttribute("boardList", boardList);
        model.addAttribute("boardCategory", boardCategory);

        model.addAttribute("likesPeriod", likesPeriod);
        model.addAttribute("viewsPeriod", viewsPeriod);
        model.addAttribute("boardPeriod", boardPeriod);

        model.addAttribute("bestLikes", bestLikes);
        model.addAttribute("bestViews", bestViews);
        model.addAttribute("bestBoard", bestBoard);

        return "home";
    }

    @RequestMapping(path = "/board", params = {"bid"})
    public String postList(@RequestParam("bid") int bid, @RequestParam(defaultValue = "1") int groupIndex, @RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "0") int likesPeriod, @RequestParam(defaultValue = "0") int viewsPeriod, Model model) {
        Board board = boardService.selectOneBoard(bid);

        Pagination pagination = new Pagination();
        pagination.setGroupSize(10);
        pagination.setPageSize(20);
        pagination.pageInfo(groupIndex, pageIndex, postService.countPost(bid));

        List<Post> postList = postService.postList(bid, pagination);
        List<Post> noticeList = postService.selectLatestNotice(bid);
        List<Board> boardList = boardService.selectAllBoard();
        List<Category> boardCategory = boardService.boardCategoryAll();
        List<Category> postCategory = postService.postCategoryAll();

        model.addAttribute("board", board);
        model.addAttribute("pagination", pagination);
        model.addAttribute("postList", postList);
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("boardList", boardList);
        model.addAttribute("boardCategory", boardCategory);
        model.addAttribute("postCategory", postCategory);

        List<Post> bestLikes = leaderBoardService.bestLikesBoard(bid, likesPeriod);
        List<Post> bestViews = leaderBoardService.bestViewsBoard(bid, viewsPeriod);

        model.addAttribute("likesPeriod", likesPeriod);
        model.addAttribute("viewsPeriod", viewsPeriod);
        model.addAttribute("bestLikes", bestLikes);
        model.addAttribute("bestViews", bestViews);

        if (bid == 1) {
            return "notice/board";
        }
        else {
            return "post/board";
        }
    }

    @RequestMapping(path = "/board", params = {"bid", "category"})
    public String postCategoryList(@RequestParam("bid") int bid, @RequestParam("category") int category, @RequestParam(defaultValue = "1") int groupIndex, @RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "0") int likesPeriod, @RequestParam(defaultValue = "0") int viewsPeriod, Model model) {
        Board board = boardService.selectOneBoard(bid);

        Pagination pagination = new Pagination();
        pagination.setGroupSize(10);
        pagination.setPageSize(20);
        pagination.pageInfo(groupIndex, pageIndex, postService.countPostCategory(bid, category));

        List<Post> postList = postService.postCategoryList(bid, category, pagination);
        List<Post> noticeList = postService.selectLatestNotice(bid);
        List<Board> boardList = boardService.selectAllBoard();
        List<Category> boardCategory = boardService.boardCategoryAll();
        List<Category> postCategory = postService.postCategoryAll();

        model.addAttribute("board", board);
        model.addAttribute("pagination", pagination);
        model.addAttribute("category", category);
        model.addAttribute("postList", postList);
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("boardList", boardList);
        model.addAttribute("boardCategory", boardCategory);
        model.addAttribute("postCategory", postCategory);

        List<Post> bestLikes = leaderBoardService.bestLikesBoard(bid, likesPeriod);
        List<Post> bestViews = leaderBoardService.bestViewsBoard(bid, viewsPeriod);

        model.addAttribute("likesPeriod", likesPeriod);
        model.addAttribute("viewsPeriod", viewsPeriod);
        model.addAttribute("bestLikes", bestLikes);
        model.addAttribute("bestViews", bestViews);

        return "post/board";
    }


}