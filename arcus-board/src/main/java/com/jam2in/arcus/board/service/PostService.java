package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public void insertPost(Post post) {
        postRepository.insert(post);
    }

    public void updatePost(Post post) {
        postRepository.update(post);
    }

    public void deletePost(int id) {
        postRepository.delete(id);
    }

    public Post selectOnePost(int id) {
        return postRepository.selectOne(id);
    }

    public List<Post> selectAll(int bid, int startList, int pageSize) {
        return postRepository.selectAll(bid, startList, pageSize);
    }

    public List<Post> selectCategory(int bid, int category, int startList, int pageSize) {
        return postRepository.selectCategory(bid, category, startList, pageSize);
    }

    public List<Post> selectLatestNotice(int bid) {
        return postRepository.selectCategory(bid, 1, 0, 3);
    }

    public int countPost(int bid) {
        return postRepository.countPost(bid);
    }

    public int countPostCategory(int bid, int category) {
        return postRepository.countPostCategory(bid, category);
    }

    public void increaseViews(int id) {
        postRepository.increaseViews(id);
    }

    public void likePost(int id) {
        postRepository.likePost(id);
    }

    public List<Category> postCategoryAll(){
        return postRepository.postCategoryAll();
    }

    public Post selectLatestRandom(int bid) {
        List<Post> postList = postRepository.selectAll(bid, 0, 100);
        int index = (int)(Math.random() * 100);
        return postList.get(index);
    }
}
