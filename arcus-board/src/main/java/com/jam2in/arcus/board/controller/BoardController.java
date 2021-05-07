package com.jam2in.arcus.board.controller;

import com.jam2in.arcus.board.model.BoardPage;
import com.jam2in.arcus.board.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BoardController {
    @Autowired
    private BoardService boardService;

    @RequestMapping(path = "/")
    public String home(@RequestParam(defaultValue = "0") int likesPeriod, @RequestParam(defaultValue = "0") int viewsPeriod, @RequestParam(defaultValue = "0") int boardPeriod, Model model) {
        BoardPage homePage = boardService.homePage(likesPeriod, viewsPeriod, boardPeriod);

        model.addAttribute("noticeList", homePage.getNoticeList());
        model.addAttribute("boardList", homePage.getBoardList());
        model.addAttribute("boardCategory", homePage.getBoardCategory());
        model.addAttribute("bestLikes", homePage.getBestLikes());
        model.addAttribute("bestViews", homePage.getBestViews());
        model.addAttribute("bestBoard", homePage.getBestBoard());

        model.addAttribute("likesPeriod", likesPeriod);
        model.addAttribute("viewsPeriod", viewsPeriod);
        model.addAttribute("boardPeriod", boardPeriod);

        return "home";
    }

    @RequestMapping(path = "/board", params = {"bid"})
    public String board(@RequestParam("bid") int bid, @RequestParam(defaultValue = "1") int groupIndex, @RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "0") int likesPeriod, @RequestParam(defaultValue = "0") int viewsPeriod, Model model) {
        BoardPage boardPage = boardService.boardPage(bid, groupIndex, pageIndex, likesPeriod, viewsPeriod);

        model.addAttribute("board", boardPage.getBoard());
        model.addAttribute("pagination", boardPage.getPagination());
        model.addAttribute("postList", boardPage.getPostList());
        model.addAttribute("noticeList", boardPage.getNoticeList());
        model.addAttribute("boardList", boardPage.getBoardList());
        model.addAttribute("boardCategory", boardPage.getBoardCategory());
        model.addAttribute("postCategory", boardPage.getPostCategory());
        model.addAttribute("bestLikes", boardPage.getBestLikes());
        model.addAttribute("bestViews", boardPage.getBestViews());

        model.addAttribute("likesPeriod", likesPeriod);
        model.addAttribute("viewsPeriod", viewsPeriod);

        if (bid == 1) {
            return "notice/board";
        }
        else {
            return "post/board";
        }
    }

    @RequestMapping(path = "/board", params = {"bid", "category"})
    public String board(@RequestParam("bid") int bid, @RequestParam("category") int category, @RequestParam(defaultValue = "1") int groupIndex, @RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "0") int likesPeriod, @RequestParam(defaultValue = "0") int viewsPeriod, Model model) {
        BoardPage boardPage = boardService.boardPage(bid, category, groupIndex, pageIndex, likesPeriod, viewsPeriod);

        model.addAttribute("board", boardPage.getBoard());
        model.addAttribute("pagination", boardPage.getPagination());
        model.addAttribute("postList", boardPage.getPostList());
        model.addAttribute("noticeList", boardPage.getNoticeList());
        model.addAttribute("boardList", boardPage.getBoardList());
        model.addAttribute("boardCategory", boardPage.getBoardCategory());
        model.addAttribute("postCategory", boardPage.getPostCategory());
        model.addAttribute("bestLikes", boardPage.getBestLikes());
        model.addAttribute("bestViews", boardPage.getBestViews());

        model.addAttribute("category", category);
        model.addAttribute("likesPeriod", likesPeriod);
        model.addAttribute("viewsPeriod", viewsPeriod);

        return "post/board";
    }


}