package com.jam2in.arcus.board.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    // TODO: 캐시 반영
    public void insertCmt(Comment comment) {
        int bid = postRepository.selectOne(comment.getPid()).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.increaseCmt(comment.getPid());
        commentRepository.insert(comment);
    }

    @Transactional
    // TODO: 캐시 반영
    public void updateCmt(Comment comment) {
        int bid = postRepository.selectOne(comment.getPid()).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        commentRepository.update(comment);
    }

    @Transactional
    // TODO: 캐시 반영
    public int deleteCmt(int cid) {
        int pid = commentRepository.selectOne(cid).getPid();
        int bid = postRepository.selectOne(pid).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.decreaseCmt(pid);
        commentRepository.delete(cid);

        return pid;
    }

    public Comment selectOneCmt(int cid) {
        return commentRepository.selectOne(cid);
    }

    public List<Comment> selectAllCmt(int pid, int startList, int pageSize) {
        List<Comment> cmtList = null;
        if (startList < pageSize*5) {
            // TODO: 캐싱
        }
        else {
            cmtList = commentRepository.selectAll(pid, startList, pageSize);
        }
        return cmtList;
    }

}
