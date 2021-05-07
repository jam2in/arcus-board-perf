package com.jam2in.arcus.board.controller;

import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.model.PostPage;
import com.jam2in.arcus.board.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class PostController {
    @Autowired
    private PostService postService;

    @RequestMapping(path = "/post/write")
    public String writePost(@RequestParam("bid") int bid, @ModelAttribute Post post, Model model) {
        PostPage postPage = postService.writePost(bid);

        model.addAttribute("board", postPage.getBoard());
        model.addAttribute("boardList", postPage.getBoardList());
        model.addAttribute("boardCategory", postPage.getBoardCategory());
        model.addAttribute("postCategory", postPage.getPostCategory());

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
    public String detailPost(@RequestParam("pid") int pid, @RequestParam(defaultValue = "1") int groupIndex, @RequestParam(defaultValue = "1") int pageIndex, @ModelAttribute Comment comment, Model model) {
        PostPage postPage = postService.detailPost(pid, groupIndex, pageIndex);

        model.addAttribute("post", postPage.getPost());
        model.addAttribute("board", postPage.getBoard());
        model.addAttribute("boardList", postPage.getBoardList());
        model.addAttribute("boardCategory", postPage.getBoardCategory());

        model.addAttribute("pagination", postPage.getPagination());
        model.addAttribute("cmtList", postPage.getCmtList());

        if (postPage.getBoard().getBid() == 1) {
            return "notice/detail";
        }
        else {
            return "post/detail";
        }
    }


    @RequestMapping(path = "/post/edit")
    public String editPost(@RequestParam("pid") int pid, Model model) {
        PostPage postPage = postService.editPost(pid);

        model.addAttribute("post", postPage.getPost());
        model.addAttribute("board", postPage.getBoard());
        model.addAttribute("boardList", postPage.getBoardList());
        model.addAttribute("boardCategory", postPage.getBoardCategory());
        model.addAttribute("postCategory", postPage.getPostCategory());

        if (postPage.getBoard().getBid() == 1) {
            return "notice/edit";
        }
        else {
            return "post/edit";
        }
    }

    @RequestMapping(path = "/post/update")
    public String updatePost(@ModelAttribute Post post) {
        postService.updatePost(post);

        return "redirect:/post/detail?pid="+post.getPid();
    }

    @RequestMapping(path = "/post/delete")
    public String deletePost(@RequestParam("pid") int pid) {
        int bid = postService.deletePost(pid);

        return "redirect:/board?bid="+bid;
    }


    @RequestMapping(path = "/post/like")
    public String likePost(@RequestParam("pid") int pid) {
        postService.likePost(pid);

        return "redirect:/post/detail?pid="+pid;
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
