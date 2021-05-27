package com.jam2in.arcus.board.service;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.spy.memcached.ArcusClientPool;

import com.jam2in.arcus.board.Arcus.PostArcus;
import com.jam2in.arcus.board.configuration.ArcusConfiguration;
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

    ApplicationContext context = new AnnotationConfigApplicationContext(ArcusConfiguration.class);
    ArcusClientPool arcusClient = context.getBean("arcusClient", ArcusClientPool.class);

    @Transactional
    public void insertPost(Post post) {
        int bid = post.getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.insert(post);
        postArcus.insertPost(post.getPid());
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

        return bid;
    }

    @Transactional
    public Post detailPost(int pid) {
        Post post = postRepository.selectOne(pid);
        boardRepository.increaseReqRecent(post.getBid());
        boardRepository.increaseReqToday(post.getBid());
        postRepository.increaseViews(pid);
        postArcus.increaseViews(post.getBid(), pid);
        return post;
    }

    public Post selectOnePost(int pid) {
        return postRepository.selectOne(pid);
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
            Future<Object> future = arcusClient.asyncGet("LatestNotice");
            try {
                latestNotice = (List<Post>) future.get(700L, TimeUnit.MILLISECONDS);
                log.info("[ARCUS] GET : LatestNotice");
            } catch (Exception e) {
                future.cancel(true);
                e.printStackTrace();
            }

            if (latestNotice == null) {
                latestNotice = postRepository.selectLatestNotice(bid);
                arcusClient.set("LatestNotice", 3600, latestNotice);
                log.info("[ARCUS] SET : LatestNotice");
            }
        }
        else {
            latestNotice = postRepository.selectLatestNotice(bid);
        }

        return latestNotice;
    }

    public int countPost(int bid) {
        return postRepository.countPost(bid);
    }

    public int countPostCategory(int bid, int category) {
        return postRepository.countPostCategory(bid, category);
    }

    public void likePost(int pid) {
        postRepository.likePost(pid);
        int bid = postRepository.selectOne(pid).getBid();
        postArcus.increaseLikes(bid, pid);
    }

    public List<Category> postCategoryAll(){
        List<Category> postCategory = null;
        Future<Object> future = arcusClient.asyncGet("Category:Post");
        try {
            postCategory = (List<Category>) future.get(1000L, TimeUnit.MILLISECONDS);
            log.info("[ARCUS] GET : Category:Board");
        } catch (Exception e) {
            future.cancel(true);
            e.printStackTrace();
        }

        if (postCategory == null) {
            postCategory = postRepository.postCategoryAll();
            arcusClient.set("Category:Post", 3600, postCategory);
            log.info("[ARCUS] SET : Category:Post");
        }

        return postCategory;
    }
}
