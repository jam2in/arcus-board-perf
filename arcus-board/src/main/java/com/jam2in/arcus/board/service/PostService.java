package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.repository.BoardRepository;
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

    public Post detailPost(int pid) {
        postRepository.increaseViews(pid);
        Post post = postRepository.selectOne(pid);
        boardRepository.increaseReqRecent(post.getBid());
        boardRepository.increaseReqToday(post.getBid());
        return post;
    }

    public Post selectOnePost(int pid) {
        return postRepository.selectOne(pid);
    }

    public List<Post> postList(int bid, Pagination pagination) {
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        return postRepository.selectAll(bid, pagination.getStartList()-1, pagination.getPageSize());
    }

    public List<Post> postCategoryList(int bid, int category, Pagination pagination) {
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        return postRepository.selectCategory(bid, category, pagination.getStartList()-1, pagination.getPageSize());
    }

    public List<Post> selectLatestNotice(int bid) {
        return postRepository.selectLatestNotice(bid);
    }

    public int countPost(int bid) {
        return postRepository.countPost(bid);
    }

    public int countPostCategory(int bid, int category) {
        return postRepository.countPostCategory(bid, category);
    }

    public void likePost(int id) {
        postRepository.likePost(id);
    }

    public List<Category> postCategoryAll(){
        return postRepository.postCategoryAll();
    }
}
