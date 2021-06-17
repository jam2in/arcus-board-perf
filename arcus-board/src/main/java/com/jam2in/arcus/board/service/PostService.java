package com.jam2in.arcus.board.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.spy.memcached.ArcusClientPool;

import com.jam2in.arcus.board.arcus.BoardRequestArcus;
import com.jam2in.arcus.board.arcus.PostArcus;
import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.repository.BoardRepository;
import com.jam2in.arcus.board.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private PostArcus postArcus;
    @Autowired
    private ArcusClientPool arcusClient;

    @Transactional
    public void insertPost(Post post) {
        int bid = post.getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.insert(post);
        postArcus.insertPost(post.getPid());
        postArcus.increasePostCount(bid);
    }

    @Transactional
    public void updatePost(Post post) {
        int bid = post.getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.update(post);
        postArcus.updatePost(postRepository.selectOne(post.getPid()));
    }

    @Transactional
    public int deletePost(int pid) {
        int bid = postRepository.selectOne(pid).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.delete(pid);
        postArcus.deletePost(bid, pid);
        postArcus.decreasePostCount(bid);

        return bid;
    }

    @Transactional
    public Post detailPost(int bid, int pid) {
        Post post = postArcus.getPost(bid, pid);
        boardRepository.increaseReqRecent(post.getBid());
        boardRepository.increaseReqToday(post.getBid());
        postRepository.increaseViews(pid);
        postArcus.increaseViews(bid, pid);
        return post;
    }

    public Post selectOnePost(int bid, int pid) {
        return postArcus.getPost(bid, pid);
    }

    @Transactional
    public List<Post> postList(int bid, Pagination pagination) {
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        return postArcus.getPostList(bid, pagination);
    }

    @Transactional
    public List<Post> postCategoryList(int bid, int category, Pagination pagination) {
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        return postRepository.selectCategory(bid, category, pagination.getStartList()-1, pagination.getPageSize());
    }

    @Transactional(readOnly = true)
    public List<Post> selectLatestNotice(int bid) {
        List<Post> latestNotice = null;
        if (bid==1) {
            Future<Object> future = null;
            try {
                future = arcusClient.asyncGet("LatestNotice");
                latestNotice = (List<Post>) future.get(700L, TimeUnit.MILLISECONDS);
                if (latestNotice == null) {
                    latestNotice = postRepository.selectLatestNotice(bid);
                    arcusClient.set("LatestNotice", 3600, latestNotice);
                }
            } catch (Exception e) {
                Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
                log.error(e.getMessage(), e);
            }
        }
        else {
            latestNotice = postRepository.selectLatestNotice(bid);
        }

        return latestNotice;
    }

    public int countPost(int bid) {
        return postArcus.getPostCount(bid);
    }

    public int countPostCategory(int bid, int category) {
        return postRepository.countPostCategory(bid, category);
    }

    public void likePost(int pid) {
        postRepository.likePost(pid);
        int bid = postRepository.selectOne(pid).getBid();
        postArcus.increaseLikes(bid, pid);
    }

    public List<Category> postCategoryAll() {
        List<Category> postCategory = null;
        Future<Object> future = null;
        try {
            future = arcusClient.asyncGet("Category:Post");
            postCategory = (List<Category>) future.get(1000L, TimeUnit.MILLISECONDS);
            if (postCategory == null) {
                postCategory = postRepository.postCategoryAll();
                arcusClient.set("Category:Post", 3600, postCategory);
            }
        } catch (Exception e) {
            Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
            log.error(e.getMessage(), e);
        }

        return postCategory;
    }
}
