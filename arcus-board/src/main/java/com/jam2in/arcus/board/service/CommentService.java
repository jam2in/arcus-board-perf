package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.repository.CommentRepository;
import com.jam2in.arcus.board.repository.PostRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;

    public void insertCmt(Comment comment) {
        commentRepository.insert(comment);
        postRepository.increaseCmt(comment.getPid());
    }

    public void updateCmt(Comment comment) {
        commentRepository.update(comment);
    }

    public void deleteCmt(int cid) {
        commentRepository.delete(cid);
        int pid = commentRepository.selectOne(cid).getPid();
        postRepository.decreaseCmt(pid);
    }

    public Comment selectOneCmt(int cid) {
       return commentRepository.selectOne(cid);
    }

    public List<Comment> selectAllCmt(int pid, int startList, int pageSize) {
        return commentRepository.selectAll(pid, startList, pageSize);
    }

}
