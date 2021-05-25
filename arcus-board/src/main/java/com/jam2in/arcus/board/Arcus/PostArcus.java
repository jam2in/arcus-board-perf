package com.jam2in.arcus.board.Arcus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.spy.memcached.ArcusClientPool;
import net.spy.memcached.collection.BTreeOrder;
import net.spy.memcached.collection.CollectionAttributes;
import net.spy.memcached.collection.CollectionOverflowAction;
import net.spy.memcached.collection.CollectionResponse;
import net.spy.memcached.collection.Element;
import net.spy.memcached.collection.ElementFlagFilter;
import net.spy.memcached.collection.ElementValueType;
import net.spy.memcached.internal.CollectionFuture;
import net.spy.memcached.ops.CollectionOperationStatus;

import com.jam2in.arcus.board.configuration.ArcusConfiguration;
import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PostArcus {
	@Autowired
	PostRepository postRepository;

	static final int PAGE_SIZE = 20;

	ApplicationContext context = new AnnotationConfigApplicationContext(ArcusConfiguration.class);
	ArcusClientPool arcusClient = context.getBean("arcusClient", ArcusClientPool.class);

	public List<Post> getPostList(int bid, Pagination pagination) {
		List<Post> postList = new ArrayList<>();
		int startList = pagination.getStartList()-1;
		int pageSize = pagination.getPageSize();

		if (startList >= pageSize*5) {
			return postRepository.selectAll(bid, startList, pageSize);
		}

		int postTo = startList + pageSize - 1;
		// Get post list
		CollectionFuture<Map<Integer, Element<Object>>> future = arcusClient.asyncBopGetByPosition("PostList:"+bid, BTreeOrder.DESC, startList, postTo);
		try {
			Map<Integer, Element<Object>> result = future.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = future.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.NOT_FOUND)) {
				return postRepository.selectAll(bid, startList, pageSize);
			}
			log.info("[ARCUS] GET : PostList:" + bid);
			for (Map.Entry<Integer, Element<Object>> each : result.entrySet()) {
				postList.add((Post)each.getValue().getValue());
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
		// Merge views
		future = arcusClient.asyncBopGetByPosition("PostViews:"+bid, BTreeOrder.DESC, startList, postTo);
		try {
			Map<Integer, Element<Object>> result = future.get(1000L, TimeUnit.MILLISECONDS);
			log.info("[ARCUS] GET : PostViews:" + bid);
			int i=0;
			for (Map.Entry<Integer, Element<Object>> each : result.entrySet()) {
				postList.get(i++).setViews(Integer.parseInt((String)each.getValue().getValue()));
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
		// Merge likes
		future = arcusClient.asyncBopGetByPosition("PostLikes:"+bid, BTreeOrder.DESC, startList, postTo);
		try {
			Map<Integer, Element<Object>> result = future.get(1000L, TimeUnit.MILLISECONDS);
			log.info("[ARCUS] GET : PostLikes:" + bid);
			int i=0;
			for (Map.Entry<Integer, Element<Object>> each : result.entrySet()) {
				postList.get(i++).setLikes(Integer.parseInt((String)each.getValue().getValue()));
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
		// Merge cmtCnt
		future = arcusClient.asyncBopGetByPosition("PostCmtCnt:"+bid, BTreeOrder.DESC, startList, postTo);
		try {
			Map<Integer, Element<Object>> result = future.get(1000L, TimeUnit.MILLISECONDS);
			log.info("[ARCUS] GET : PostCmtCnt:" + bid);
			int i=0;
			for (Map.Entry<Integer, Element<Object>> each : result.entrySet()) {
				postList.get(i++).setCmtCnt(Integer.parseInt((String)each.getValue().getValue()));
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}

		return postList;
	}

	@Scheduled(fixedDelay = 3600000)
	public void setPostList() {
		for (int bid=2; bid<12; bid++) {
			List<Post> postList = postRepository.selectAll(bid, 0, PAGE_SIZE*5);
			CollectionAttributes attributes = new CollectionAttributes(3600, PAGE_SIZE*5L, CollectionOverflowAction.smallest_trim);

			arcusClient.delete("PostList:"+bid);
			arcusClient.delete("PostViews:"+bid);
			arcusClient.delete("PostLikes:"+bid);
			arcusClient.delete("PostCmtCnt:"+bid);

			if (postList.size() == 0) {
				arcusClient.asyncBopCreate("PostList:"+bid, ElementValueType.OTHERS, attributes);
				arcusClient.asyncBopCreate("PostViews:"+bid, ElementValueType.STRING, attributes);
				arcusClient.asyncBopCreate("PostLikes:"+bid, ElementValueType.STRING, attributes);
				arcusClient.asyncBopCreate("PostCmtCnt:"+bid, ElementValueType.STRING, attributes);
				continue;
			}

			List<Element<Object>> posts = new ArrayList<>();
			List<Element<Object>> views = new ArrayList<>();
			List<Element<Object>> likes = new ArrayList<>();
			List<Element<Object>> cmtCnt = new ArrayList<>();
			for (Post post : postList) {
				posts.add(new Element<>(post.getPid(), post, new byte[]{1,1}));
				views.add(new Element<>(post.getPid(), String.valueOf(post.getViews()), new byte[]{1,1}));
				likes.add(new Element<>(post.getPid(), String.valueOf(post.getLikes()), new byte[]{1,1}));
				cmtCnt.add(new Element<>(post.getPid(), String.valueOf(post.getCmtCnt()), new byte[]{1,1}));
			}

			setPostListCache("PostList:"+bid, posts, attributes);
			setPostListCache("PostViews:"+bid, views, attributes);
			setPostListCache("PostLikes:"+bid, likes, attributes);
			setPostListCache("PostCmtCnt:"+bid, cmtCnt, attributes);
		}
	}
	
	public void setPostListCache(String key, List<Element<Object>> elements, CollectionAttributes attributes) {
		CollectionFuture<Map<Integer, CollectionOperationStatus>> future = null;
		try {
			future = arcusClient.asyncBopPipedInsertBulk(key, elements, attributes);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		if (future == null) return;

		try {
			Map<Integer, CollectionOperationStatus> result = future.get(1000L, TimeUnit.MILLISECONDS);
			if (!result.isEmpty()) {
				log.error("[ARCUS] Some items failed to insert.");
				for (Map.Entry<Integer, CollectionOperationStatus> entry : result.entrySet()) {
					log.error("Failed item = " + elements.get(entry.getKey()));
					log.error("Caused by " + entry.getValue().getResponse());
				}
			}
			else {
				log.info("[ARCUS] SET : " + key);
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

	public void insertPost(int pid) {
		Post insertedPost = postRepository.selectOne(pid);
		Post post = new Post();
		post.setPid(insertedPost.getPid());
		post.setTitle(insertedPost.getTitle());
		post.setCreatedDate(insertedPost.getCreatedDate());
		post.setUserName(insertedPost.getUserName());
		post.setCategoryName(insertedPost.getCategoryName());

		insertPostCache("PostList:"+insertedPost.getBid(), post.getPid(), post);
		insertPostCache("PostViews:"+insertedPost.getBid(), post.getPid(), String.valueOf(insertedPost.getViews()));
		insertPostCache("PostLikes:"+insertedPost.getBid(), post.getPid(), String.valueOf(insertedPost.getLikes()));
		insertPostCache("PostCmtCnt:"+insertedPost.getBid(), post.getPid(), String.valueOf(insertedPost.getCmtCnt()));
	}

	public void insertPostCache(String key, long bkey, Object value) {
		CollectionFuture<Boolean> future = arcusClient.asyncBopInsert(key, bkey, new byte[]{1,1}, value, null);
		try {
			if(!future.get(1000L, TimeUnit.MILLISECONDS))
				log.error("[ARCUS] Failed to insert : " + key + ":" + bkey);
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

	public void increaseViews(int bid, int pid) {
		String key = "PostViews:"+bid;
		CollectionFuture<Long> future = arcusClient.asyncBopIncr(key, pid, 1);
		try {
			Long result = future.get(1000L, TimeUnit.MILLISECONDS);
			if (result == null) {
				log.error("[ARCUS] Failed to increase views : " + key + ":" + pid);
				log.error(String.valueOf(future.getOperationStatus().getResponse()));
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

	public void increaseLikes(int bid, int pid) {
		String key = "PostLikes:"+bid;
		CollectionFuture<Long> future = arcusClient.asyncBopIncr(key, pid, 1);
		try {
			Long result = future.get(1000L, TimeUnit.MILLISECONDS);
			if ( result == null) {
				log.error("[ARCUS] Failed to increase Likes : " + key + ":" + pid);
				log.error(String.valueOf(future.getOperationStatus().getResponse()));
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

	public void increaseCmtCnt(int bid, int pid) {
		String key = "PostCmtCnt:"+bid;
		CollectionFuture<Long> future = arcusClient.asyncBopIncr(key, pid, 1);
		try {
			Long result = future.get(1000L, TimeUnit.MILLISECONDS);
			if ( result == null ) {
				log.error("[ARCUS] Failed to increase CmtCnt : " + key + ":" + pid);
				log.error(String.valueOf(future.getOperationStatus().getResponse()));
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

	public void decreaseCmtCnt(int bid, int pid) {
		String key = "PostCmtCnt:"+bid;
		CollectionFuture<Long> future = arcusClient.asyncBopDecr(key, pid, 1);
		try {
			Long result = future.get(1000L, TimeUnit.MILLISECONDS);
			if ( result == null ) {
				log.error("[ARCUS] Failed to decrease CmtCnt : " + key + ":" + pid);
				log.error(String.valueOf(future.getOperationStatus().getResponse()));
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

	public void deletePost(int bid, int pid) {
		deletePostCache("PostList:"+bid, pid);
		deletePostCache("PostViews:"+bid, pid);
		deletePostCache("PostLikes:"+bid, pid);
		deletePostCache("PostCmtCnt:"+bid, pid);
	}

	public void deletePostCache(String key, long bkey) {
		CollectionFuture<Boolean> future = arcusClient.asyncBopDelete(key, bkey, ElementFlagFilter.DO_NOT_FILTER, false);
		if (future == null) return;
		try {
			future.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = future.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.NOT_FOUND_ELEMENT)) return;
			log.info("[ARCUS] DELETE : " + key + ":" + bkey);
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

	public void updatePost(Post updatedPost) {
		Post post = new Post();
		post.setPid(updatedPost.getPid());
		post.setTitle(updatedPost.getTitle());
		post.setCreatedDate(updatedPost.getCreatedDate());
		post.setUserName(updatedPost.getUserName());
		post.setCategoryName(updatedPost.getCategoryName());

		String key = "PostList:"+updatedPost.getBid();
		CollectionFuture<Boolean> future = arcusClient.asyncBopUpdate(key, post.getPid(), null, post);
		try {
			future.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = future.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.NOT_FOUND_ELEMENT)) return;
			log.info("[ARCUS] UPDATE : " + key + ":" + post.getPid());
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

}
