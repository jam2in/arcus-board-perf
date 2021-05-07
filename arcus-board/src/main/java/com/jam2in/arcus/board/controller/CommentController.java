package com.jam2in.arcus.board.controller;

import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.model.PostPage;
import com.jam2in.arcus.board.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class CommentController {
    @Autowired
    CommentService commentService;

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

        return "redirect:/post/detail?pid="+comment.getPid();
    }

    @RequestMapping(path = "/comment/edit")
    public String editCmt(@RequestParam("pid") int pid, @RequestParam("cid") int cid, @RequestParam(defaultValue = "1") int groupIndex, @RequestParam(defaultValue = "1") int pageIndex, Model model) {
        PostPage postPage = commentService.editCmt(pid, cid, groupIndex, pageIndex);

        model.addAttribute("post", postPage.getPost());
        model.addAttribute("board", postPage.getBoard());
        model.addAttribute("boardList", postPage.getBoardList());
        model.addAttribute("boardCategory", postPage.getBoardCategory());
        model.addAttribute("pagination", postPage.getPagination());
        model.addAttribute("cmtList", postPage.getCmtList());
        model.addAttribute("comment", postPage.getComment());

        model.addAttribute("cid", cid);

        if (postPage.getPost().getBid() == 1) {
            return "notice/detail";
        }
        else {
            return "post/detail";
        }
    }

    @RequestMapping(path = "/comment/update")
    public String updateCmt(@ModelAttribute Comment comment) {
        commentService.updateCmt(comment);

        return "redirect:/post/detail?pid="+comment.getPid();
    }

    @RequestMapping(path = "/comment/delete")
    public String deleteCmt(@RequestParam("cid") int cid) {
        int pid = commentService.deleteCmt(cid);

        return "redirect:/post/detail?pid="+pid;
    }
}
