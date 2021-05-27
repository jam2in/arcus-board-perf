package com.jam2in.arcus.board.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.service.BoardService;
import com.jam2in.arcus.board.service.CommentService;
import com.jam2in.arcus.board.service.PostService;
import com.jam2in.arcus.board.service.TestService;

@Controller
public class TestController {
	@Autowired
	BoardService boardService;
	@Autowired
	PostService postService;
	@Autowired
	CommentService commentService;
	@Autowired
	TestService testService;

	@RequestMapping(path = "/test/reset")
	public String resetData(Model model) throws SQLException {
		testService.resetData();

		return "home";
	}

	@RequestMapping(path = "/test/post")
	public String postDetail(@RequestParam("bid") int bid, @RequestParam(defaultValue = "1") int groupIndex, @RequestParam(defaultValue = "1") int pageIndex, @ModelAttribute Comment comment, Model model) {
		int pid = testService.selectLatestRandom(bid);
		Post post = postService.detailPost(pid);
		Board board = boardService.selectOneBoard(bid);
		List<Board> boardList = boardService.selectAllBoard();
		List<Category> boardCategory = boardService.boardCategoryAll();

		Pagination pagination = Pagination.builder().groupSize(5).pageSize(10).build();
		pagination.pageInfo(groupIndex, pageIndex, post.getCmtCnt());

		List<Comment> cmtList = commentService.selectAllCmt(pid, pagination.getStartList()-1, pagination.getPageSize());

		model.addAttribute("post", post);
		model.addAttribute("board", board);
		model.addAttribute("boardList", boardList);
		model.addAttribute("boardCategory", boardCategory);

		model.addAttribute("cmtList", cmtList);
		model.addAttribute("pagination", pagination);

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

		return "redirect:/post/detail?bid="+bid+"&pid="+pid;
	}

	@RequestMapping(path = "/test/post/like")
	public String likePost(@RequestParam("bid") int bid){
		int pid = testService.selectLatestRandom(bid);
		postService.likePost(pid);

		return "redirect:/post/detail?bid="+bid+"&pid="+pid;
	}

	@RequestMapping(path = "/test1")
	public String test1() {
		return "home";
	}

	@RequestMapping(path = "/test2")
	@ResponseBody
	public String test2() {
		return "";
	}

}
