package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.model.Board;
import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.model.PostPage;
import com.jam2in.arcus.board.repository.BoardRepository;
import com.jam2in.arcus.board.repository.CommentRepository;
import com.jam2in.arcus.board.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private CommentRepository commentRepository;

    public void insertPost(Post post) {
        postRepository.insert(post);
        int bid = post.getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
    }

    public void updatePost(Post post) {
        postRepository.update(post);
        int bid = post.getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
    }

    public int deletePost(int pid) {
        int bid = postRepository.selectOne(pid).getBid();
        postRepository.delete(pid);
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);

        return bid;
    }

    public PostPage detailPost(int pid, int groupIndex, int pageIndex) {
        postRepository.increaseViews(pid);
        Post post = postRepository.selectOne(pid);
        Board board = boardRepository.selectOne(post.getBid());
        boardRepository.increaseReqRecent(post.getBid());
        boardRepository.increaseReqToday(post.getBid());

        Pagination pagination = Pagination.builder().groupSize(5).pageSize(10).build();
        pagination.pageInfo(groupIndex, pageIndex, post.getCmtCnt());

        PostPage postPage = PostPage.builder()
            .post(post)
            .board(board)
            .boardList(boardRepository.selectAll())
            .boardCategory(boardRepository.boardCategoryAll())
            .pagination(pagination)
            .cmtList(commentRepository.selectAll(pid, pagination.getStartList()-1, pagination.getPageSize()))
            .build();

        return postPage;
    }

    public PostPage writePost(int bid) {
        PostPage postPage = PostPage.builder()
            .board(boardRepository.selectOne(bid))
            .boardList(boardRepository.selectAll())
            .boardCategory(boardRepository.boardCategoryAll())
            .postCategory(postRepository.postCategoryAll())
            .build();

        return postPage;
    }

    public PostPage editPost(int pid) {
        Post post = postRepository.selectOne(pid);
        PostPage postPage = PostPage.builder()
            .post(postRepository.selectOne(pid))
            .board(boardRepository.selectOne(post.getBid()))
            .boardList(boardRepository.selectAll())
            .boardCategory(boardRepository.boardCategoryAll())
            .postCategory(postRepository.postCategoryAll())
            .build();

        return postPage;
    }

    public void likePost(int id) {
        postRepository.likePost(id);
    }

    public int selectLatestRandom(int bid) {
        List<Post> postList = postRepository.selectAll(bid, 0, 100);
        int index = (int)(Math.random() * 100);
        return postList.get(index).getPid();
    }
}
