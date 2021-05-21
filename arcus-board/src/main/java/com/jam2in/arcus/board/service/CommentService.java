package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.Arcus.CommentArcus;
import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.repository.BoardRepository;
import com.jam2in.arcus.board.repository.CommentRepository;
import com.jam2in.arcus.board.repository.PostRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    CommentArcus commentArcus;

    @Transactional
    public void insertCmt(Comment comment) {
        int bid = postRepository.selectOne(comment.getPid()).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.increaseCmt(comment.getPid());
        commentRepository.insert(comment);
        commentArcus.insertComment(comment.getCid());
    }

    @Transactional
    public void updateCmt(Comment comment) {
        int bid = postRepository.selectOne(comment.getPid()).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        commentRepository.update(comment);
        commentArcus.updateComment(comment);
    }

    @Transactional
    public int deleteCmt(int cid) {
        int pid = commentRepository.selectOne(cid).getPid();
        int bid = postRepository.selectOne(pid).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.decreaseCmt(pid);
        commentRepository.delete(cid);
        commentArcus.deleteComment(pid, cid);

        return pid;
    }

    public Comment selectOneCmt(int pid, int cid) {
        return commentArcus.getComment(pid, cid);
    }

    public List<Comment> selectAllCmt(int pid, int startList, int pageSize) {
        return commentArcus.getCommentList(pid, startList, pageSize);
    }

}
