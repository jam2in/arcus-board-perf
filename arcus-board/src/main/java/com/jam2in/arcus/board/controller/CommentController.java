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
public class CommentController {
    @Autowired
    CommentService commentService;
    @Autowired
    PostService postService;
    @Autowired
    BoardService boardService;

/*
    @ResponseBody
    @RequestMapping(path = "/comment", method = RequestMethod.POST, produces = "application/json; charset=utf8")
    public ResponseEntity CmtList(@RequestBody HashMap<String,Integer> hashMap) {
        int pid = hashMap.get("pid");
        int groupIndex = hashMap.get("groupIndex");
        int pageIndex = hashMap.get("pageIndex");

        Pagination pagination = new Pagination();
        pagination.setGroupSize(5);
        pagination.setPageSize(3);
        pagination.pageInfo(groupIndex, pageIndex, postService.selectOnePost(pid).getCmtCnt());

        List<Comment> cmtList = commentService.selectAllCmt(pid,pagination.getStartList()-1, pagination.getPageSize());

        HashMap<String, Object> map = new HashMap<>();
        map.put("cmtList", cmtList);
        map.put("pagination", pagination);

        return new ResponseEntity(map, HttpStatus.OK);
    }
*/

    @RequestMapping(path = "/comment/insert")
    public String insertCmt(@ModelAttribute Comment comment) {
        commentService.insertCmt(comment);

        return "redirect:/post/detail?bid="+comment.getBid()+"&pid="+comment.getPid();
    }

    @RequestMapping(path = "/comment/edit")
    public String editCmt(@RequestParam("bid") int bid, @RequestParam("pid") int pid, @RequestParam("cid") int cid, @RequestParam(defaultValue = "1") int groupIndex, @RequestParam(defaultValue = "1") int pageIndex, Model model) {
        Post post = postService.selectOnePost(pid);
        Board board = boardService.selectOneBoard(bid);
        List<Board> boardList = boardService.selectAllBoard();
        List<Category> boardCategory = boardService.boardCategoryAll();

        Pagination pagination = Pagination.builder().groupSize(5).pageSize(10).build();
        pagination.pageInfo(groupIndex, pageIndex, post.getCmtCnt());

        List<Comment> cmtList = commentService.selectAllCmt(pid, pagination.getStartList()-1, pagination.getPageSize());
        Comment comment = commentService.selectOneCmt(pid, cid);

        model.addAttribute("post", post);
        model.addAttribute("board", board);
        model.addAttribute("boardList", boardList);
        model.addAttribute("boardCategory", boardCategory);

        model.addAttribute("cmtList", cmtList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("cid", cid);
        model.addAttribute("comment", comment);

        if (bid == 1) {
            return "notice/detail";
        }
        else {
            return "post/detail";
        }
    }

    @RequestMapping(path = "/comment/update")
    public String updateCmt(@ModelAttribute Comment comment) {
        commentService.updateCmt(comment);

        return "redirect:/post/detail?bid="+comment.getBid()+"&pid="+comment.getPid();
    }

    @RequestMapping(path = "/comment/delete")
    public String deleteCmt(@RequestParam("bid") int bid, @RequestParam("cid") int cid) {
        int pid = commentService.deleteCmt(cid);

        return "redirect:/post/detail?bid="+bid+"&pid="+pid;
    }
}
