package com.jam2in.arcus.board.service;

import com.jam2in.arcus.board.configuration.ArcusConfiguration;
import com.jam2in.arcus.board.model.Category;
import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.repository.BoardRepository;
import com.jam2in.arcus.board.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.ArcusClientPool;

@Slf4j
@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private BoardRepository boardRepository;

    ApplicationContext context = new AnnotationConfigApplicationContext(ArcusConfiguration.class);
    ArcusClientPool arcusClient = context.getBean("arcusClient", ArcusClientPool.class);

    @Transactional
    public void insertPost(Post post) {
        int bid = post.getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.insert(post);
    }

    @Transactional
    public void updatePost(Post post) {
        int bid = post.getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.update(post);
    }

    @Transactional
    public int deletePost(int pid) {
        int bid = postRepository.selectOne(pid).getBid();
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        postRepository.delete(pid);

        return bid;
    }

    @Transactional
    public Post detailPost(int pid) {
        Post post = postRepository.selectOne(pid);
        boardRepository.increaseReqRecent(post.getBid());
        boardRepository.increaseReqToday(post.getBid());
        postRepository.increaseViews(pid);
        return post;
    }

    public Post selectOnePost(int pid) {
        return postRepository.selectOne(pid);
    }

    @Transactional
    public List<Post> postList(int bid, Pagination pagination) {
        boardRepository.increaseReqRecent(bid);
        boardRepository.increaseReqToday(bid);
        return postRepository.selectAll(bid, pagination.getStartList()-1, pagination.getPageSize());
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

    public void likePost(int id) {
        postRepository.likePost(id);
    }

    public List<Category> postCategoryAll(){
        return postRepository.postCategoryAll();
    }
}
