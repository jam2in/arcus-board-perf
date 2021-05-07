package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.model.PostPage;
import com.jam2in.arcus.board.repository.BoardRepository;
import com.jam2in.arcus.board.repository.CommentRepository;
import com.jam2in.arcus.board.repository.PostRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public int deleteCmt(int cid) {
        int pid = commentRepository.selectOne(cid).getPid();
        int bid = postRepository.selectOne(pid).getBid();
        commentRepository.delete(cid);
        postRepository.decreaseCmt(pid);
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);

        return pid;
    }

    public PostPage editCmt(int pid, int cid, int groupIndex, int pageIndex) {
        Post post = postRepository.selectOne(pid);

        Pagination pagination = Pagination.builder().groupSize(5).pageSize(10).build();
        pagination.pageInfo(groupIndex, pageIndex, post.getCmtCnt());

        PostPage postPage = PostPage.builder()
            .post(post)
            .board(boardRepository.selectOne(post.getBid()))
            .boardList(boardRepository.selectAll())
            .boardCategory(boardRepository.boardCategoryAll())
            .pagination(pagination)
            .cmtList(commentRepository.selectAll(pid, pagination.getStartList()-1, pagination.getPageSize()))
            .comment(commentRepository.selectOne(cid))
            .build();

        return postPage;
    }

}
