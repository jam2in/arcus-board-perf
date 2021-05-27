package com.jam2in.arcus.board.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.service.BoardService;
import com.jam2in.arcus.board.service.CommentService;
import com.jam2in.arcus.board.service.PostService;

@Controller
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private BoardService boardService;
    @Autowired
    private CommentService commentService;

    @RequestMapping(path = "/post/write")
    public String writePost(@RequestParam("bid") int bid, @ModelAttribute Post post, Model model) {
        Board board = boardService.selectOneBoard(bid);
        List<Board> boardList = boardService.selectAllBoard();
        List<Category> boardCategory = boardService.boardCategoryAll();
        List<Category> postCategory = postService.postCategoryAll();

        model.addAttribute("board", board);
        model.addAttribute("boardList", boardList);
        model.addAttribute("boardCategory", boardCategory);
        model.addAttribute("postCategory", postCategory);

        if (bid == 1) {
            return "notice/write";
        }
        else {
            return "post/write";
        }
    }

    @RequestMapping( path = "/post/insert")
    public String insertPost(@ModelAttribute Post post) {
        postService.insertPost(post);

        return "redirect:/board?bid="+post.getBid();
    }

    @RequestMapping(path = "/post/detail", params = {"pid"})
    public String detailPost(@RequestParam("bid") int bid, @RequestParam("pid") int pid, @RequestParam(defaultValue = "1") int groupIndex, @RequestParam(defaultValue = "1") int pageIndex, @ModelAttribute Comment comment, Model model) {
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

        if (bid == 1) {
            return "notice/detail";
        }
        else {
            return "post/detail";
        }
    }


    @RequestMapping(path = "/post/edit")
    public String editPost(@RequestParam("bid") int bid, @RequestParam("pid") int pid, Model model) {
        Post post = postService.selectOnePost(pid);
        Board board = boardService.selectOneBoard(bid);
        List<Board> boardList = boardService.selectAllBoard();
        List<Category> boardCategory = boardService.boardCategoryAll();
        List<Category> postCategory = postService.postCategoryAll();

        model.addAttribute("post", post);
        model.addAttribute("board", board);
        model.addAttribute("boardList", boardList);
        model.addAttribute("boardCategory", boardCategory);
        model.addAttribute("postCategory", postCategory);

        if (bid == 1) {
            return "notice/edit";
        }
        else {
            return "post/edit";
        }
    }

    @RequestMapping(path = "/post/update")
    public String updatePost(@ModelAttribute Post post) {
        postService.updatePost(post);

        return "redirect:/post/detail?bid="+post.getBid()+"&pid="+post.getPid();
    }

    @RequestMapping(path = "/post/delete")
    public String deletePost(@RequestParam("pid") int pid) {
        int bid = postService.deletePost(pid);

        return "redirect:/board?bid="+bid;
    }


    @RequestMapping(path = "/post/like")
    public String likePost(@RequestParam("bid") int bid, @RequestParam("pid") int pid) {
        postService.likePost(pid);

        return "redirect:/post/detail?bid="+bid+"&pid="+pid;
    }

/*
    @ResponseBody
    @RequestMapping(path = "/post/like", method = RequestMethod.POST, produces = "application/json; charset=utf8")
    public ResponseEntity likePost(@RequestBody HashMap<String,Integer> hashMap) {
        int pid = hashMap.get("pid");
        postService.likePost(pid);

        int likes = postService.selectOnePost(pid).getLikes();

        return new ResponseEntity(likes, HttpStatus.OK);
    }
*/

}
