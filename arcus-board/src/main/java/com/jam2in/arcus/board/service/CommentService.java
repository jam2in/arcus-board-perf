package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.repository.BoardRepository;
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
    @Autowired
    private BoardRepository boardRepository;

    public void insertCmt(Comment comment) {
        commentRepository.insert(comment);
        postRepository.increaseCmt(comment.getPid());
        int bid = postRepository.selectOne(comment.getPid()).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
    }

    public void updateCmt(Comment comment) {
        commentRepository.update(comment);
        int bid = postRepository.selectOne(comment.getPid()).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
    }

    public void deleteCmt(int cid) {
        int pid = commentRepository.selectOne(cid).getPid();
        int bid = postRepository.selectOne(pid).getBid();
        commentRepository.delete(cid);
        postRepository.decreaseCmt(pid);
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
    }

    public Comment selectOneCmt(int cid) {
       return commentRepository.selectOne(cid);
    }

    public List<Comment> selectAllCmt(int pid, int startList, int pageSize) {
        return commentRepository.selectAll(pid, startList, pageSize);
    }

}
