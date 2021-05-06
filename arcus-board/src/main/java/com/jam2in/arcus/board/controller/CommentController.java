package com.jam2in.arcus.board.controller;

import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.service.BoardService;
import com.jam2in.arcus.board.service.CommentService;
import com.jam2in.arcus.board.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class CommentController {
    @Autowired
    CommentService commentService;
    @Autowired
    PostService postService;
    @Autowired
    BoardService boardService;

/*    @ResponseBody
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
    }*/

    @RequestMapping(path = "/comment/insert")
    public String insertCmt(@ModelAttribute Comment comment) {
        commentService.insertCmt(comment);

        int bid = postService.selectOnePost(comment.getPid()).getBid();
        boardService.increaseReq(bid);

        return "redirect:/post/detail?pid="+comment.getPid();
    }

    @RequestMapping(path = "/comment/update")
    public String updateCmt(@ModelAttribute Comment comment) {
        commentService.updateCmt(comment);

        int bid = postService.selectOnePost(comment.getPid()).getBid();
        boardService.increaseReq(bid);

        return "redirect:/post/detail?pid="+comment.getPid();
    }

    @RequestMapping(path = "/comment/delete")
    public String deleteCmt(@RequestParam("cid") int cid) {
        Comment comment = commentService.selectOneCmt(cid);
        int bid = postService.selectOnePost(comment.getPid()).getBid();
        commentService.deleteCmt(cid);

        boardService.increaseReq(bid);

        return "redirect:/post/detail?pid="+comment.getPid();
    }
}
