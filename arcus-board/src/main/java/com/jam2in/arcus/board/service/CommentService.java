package com.jam2in.arcus.board.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam2in.arcus.board.arcus.CommentArcus;
import com.jam2in.arcus.board.arcus.PostArcus;
import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.repository.BoardRepository;
import com.jam2in.arcus.board.repository.CommentRepository;
import com.jam2in.arcus.board.repository.PostRepository;

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
    @Autowired
    PostArcus postArcus;

    @Transactional
    public void insertCmt(Comment comment) {
        int bid = postRepository.selectOne(comment.getPid()).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.increaseCmt(comment.getPid());
        postArcus.increaseCmtCnt(bid, comment.getPid());
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
        postArcus.decreaseCmtCnt(bid, pid);
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
