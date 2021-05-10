package com.jam2in.arcus.board.controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jam2in.arcus.board.model.BoardPage;
import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.model.PostPage;
import com.jam2in.arcus.board.service.BoardService;
import com.jam2in.arcus.board.service.CommentService;
import com.jam2in.arcus.board.service.PostService;
import com.jam2in.arcus.board.service.TestService;

@Controller
public class TestController {
	@Autowired
	PostService postService;
	@Autowired
	CommentService commentService;
	@Autowired
	BoardService boardService;
	@Autowired
	TestService testService;

	@RequestMapping(path = "/test/reset")
	public String resetData(Model model) throws SQLException {
		testService.resetData();

		BoardPage homePage = boardService.homePage(0,0,0);

		model.addAttribute("noticeList", homePage.getNoticeList());
		model.addAttribute("boardList", homePage.getBoardList());
		model.addAttribute("boardCategory", homePage.getBoardCategory());
		model.addAttribute("bestLikes", homePage.getBestLikes());
		model.addAttribute("bestViews", homePage.getBestViews());
		model.addAttribute("bestBoard", homePage.getBestBoard());

		model.addAttribute("likesPeriod", 0);
		model.addAttribute("viewsPeriod", 0);
		model.addAttribute("boardPeriod", 0);

		return "home";
	}

	@RequestMapping(path = "/test/post")
	public String detailPost(@RequestParam("bid") int bid, @RequestParam(defaultValue = "1") int groupIndex, @RequestParam(defaultValue = "1") int pageIndex, @ModelAttribute Comment comment, Model model) {
		int pid = testService.selectLatestRandom(bid);
		PostPage postPage = postService.detailPost(pid, groupIndex, pageIndex);

		model.addAttribute("post", postPage.getPost());
		model.addAttribute("board", postPage.getBoard());
		model.addAttribute("boardList", postPage.getBoardList());
		model.addAttribute("boardCategory", postPage.getBoardCategory());

		model.addAttribute("pagination", postPage.getPagination());
		model.addAttribute("cmtList", postPage.getCmtList());

		return "post/detail";
	}


	@RequestMapping(path = "/test/post/comment")
	public String insertCmt(@RequestParam("bid") int bid, @RequestParam("uid") int uid, @RequestParam("content") String content) {
		int pid = testService.selectLatestRandom(bid);

		Comment comment = new Comment();
		comment.setPid(pid);
		comment.setUid(uid);
		comment.setContent(content);

		commentService.insertCmt(comment);

		return "redirect:/post/detail?pid="+pid;
	}

	@RequestMapping(path = "/test/post/like")
	public String likePost(@RequestParam("bid") int bid){
		int pid = testService.selectLatestRandom(bid);
		postService.likePost(pid);

		return "redirect:/post/detail?pid="+pid;
	}

}
