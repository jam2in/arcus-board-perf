package com.jam2in.arcus.board.arcus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.jam2in.arcus.board.model.Pagination;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PostArcus {
	private static final int PAGE_SIZE = 20;

	@Autowired
	private PostRepository postRepository;
	@Autowired
	private PostCachingQueue postCachingQueue;
	@Autowired
	private ArcusClientPool arcusClient;

	public List<Post> getPostList(int bid, Pagination pagination) {
		List<Post> postList = new ArrayList<>();
		int startList = pagination.getStartList()-1;
		int pageSize = pagination.getPageSize();

		if (startList >= pageSize*5) {
			return postRepository.selectAll(bid, startList, pageSize);
		}

		int postTo = startList + pageSize - 1;

		CollectionFuture<Map<Integer, Element<Object>>> postListFuture = null;
		CollectionFuture<Map<Integer, Element<Object>>> viewsFuture = null;
		CollectionFuture<Map<Integer, Element<Object>>> likesFuture = null;
		CollectionFuture<Map<Integer, Element<Object>>> commentCountFuture = null;

		try {
			// Get post list
			postListFuture = arcusClient.asyncBopGetByPosition("PostList:"+bid, BTreeOrder.DESC, startList, postTo);
			Map<Integer, Element<Object>> result = postListFuture.get(1000L, TimeUnit.MILLISECONDS);
			if (!postListFuture.getOperationStatus().isSuccess()) {
				postCachingQueue.put(bid);
				return postRepository.selectAll(bid, startList, pageSize);
			}
			// Merge post list
			for (Map.Entry<Integer, Element<Object>> each : result.entrySet()) {
				postList.add((Post)each.getValue().getValue());
			}
		} catch (Exception e) {
			Optional.ofNullable(postListFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
			return postRepository.selectAll(bid, startList, pageSize);
		}

		try {
			// Get views
			viewsFuture = arcusClient.asyncBopGetByPosition("PostViews:"+bid, BTreeOrder.DESC, startList, postTo);
			Map<Integer, Element<Object>> result = viewsFuture.get(1000L, TimeUnit.MILLISECONDS);
			if (!viewsFuture.getOperationStatus().isSuccess()) {
				postCachingQueue.put(bid);
				return postRepository.selectAll(bid, startList, pageSize);
			}
			// Merge views
			int i=0;
			for (Map.Entry<Integer, Element<Object>> each : result.entrySet()) {
				postList.get(i++).setViews(Integer.parseInt((String)each.getValue().getValue()));
			}
		} catch (Exception e) {
			Optional.ofNullable(viewsFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
			return postRepository.selectAll(bid, startList, pageSize);
		}

		try {
			// Get likes
			likesFuture = arcusClient.asyncBopGetByPosition("PostLikes:"+bid, BTreeOrder.DESC, startList, postTo);
			Map<Integer, Element<Object>> result = likesFuture.get(1000L, TimeUnit.MILLISECONDS);
			if (!likesFuture.getOperationStatus().isSuccess()) {
				postCachingQueue.put(bid);
				return postRepository.selectAll(bid, startList, pageSize);
			}
			// Merge likes
			int i=0;
			for (Map.Entry<Integer, Element<Object>> each : result.entrySet()) {
				postList.get(i++).setLikes(Integer.parseInt((String)each.getValue().getValue()));
			}
		} catch (Exception e) {
			Optional.ofNullable(likesFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
			return postRepository.selectAll(bid, startList, pageSize);
		}

		try {
			// Get comment count
			commentCountFuture = arcusClient.asyncBopGetByPosition("PostCmtCnt:"+bid, BTreeOrder.DESC, startList, postTo);
			Map<Integer, Element<Object>> result = commentCountFuture.get(1000L, TimeUnit.MILLISECONDS);
			if (!commentCountFuture.getOperationStatus().isSuccess()) {
				postCachingQueue.put(bid);
				return postRepository.selectAll(bid, startList, pageSize);
			}
			// Merge comment count
			int i=0;
			for (Map.Entry<Integer, Element<Object>> each : result.entrySet()) {
				postList.get(i++).setCmtCnt(Integer.parseInt((String)each.getValue().getValue()));
			}
		} catch (Exception e) {
			Optional.ofNullable(commentCountFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
			return postRepository.selectAll(bid, startList, pageSize);
		}

		return postList;
	}

	@PostConstruct
	public void setPostListAll() {
		for (int bid=2; bid<12; bid++) {
			postCachingQueue.put(bid);
		}
		postCachingQueue.cachingPost();
	}

	public void setPostList(int bid) {
		List<Post> postList = postRepository.selectAllCache(bid, 0, PAGE_SIZE*5);
		CollectionAttributes attributes = new CollectionAttributes(3600, PAGE_SIZE*5L, CollectionOverflowAction.smallest_trim);

		try {
			arcusClient.delete("PostList:" + bid);
			arcusClient.delete("PostContent:" + bid);
			arcusClient.delete("PostViews:" + bid);
			arcusClient.delete("PostLikes:" + bid);
			arcusClient.delete("PostCmtCnt:" + bid);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		if (postList.size() == 0) {
			try {
				arcusClient.asyncBopCreate("PostList:" + bid, ElementValueType.OTHERS, attributes);
				arcusClient.asyncBopCreate("PostContent:" + bid, ElementValueType.STRING, attributes);
				arcusClient.asyncBopCreate("PostViews:" + bid, ElementValueType.STRING, attributes);
				arcusClient.asyncBopCreate("PostLikes:" + bid, ElementValueType.STRING, attributes);
				arcusClient.asyncBopCreate("PostCmtCnt:" + bid, ElementValueType.STRING, attributes);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		List<Element<Object>> contents = new ArrayList<>();
		List<Element<Object>> posts = new ArrayList<>();
		List<Element<Object>> views = new ArrayList<>();
		List<Element<Object>> likes = new ArrayList<>();
		List<Element<Object>> cmtCnt = new ArrayList<>();
		for (Post post : postList) {
			contents.add(new Element<>(post.getPid(), post.getContent(), new byte[]{1,1}));
			post.setContent(null);
			posts.add(new Element<>(post.getPid(), post, new byte[]{1,1}));
			views.add(new Element<>(post.getPid(), String.valueOf(post.getViews()), new byte[]{1,1}));
			likes.add(new Element<>(post.getPid(), String.valueOf(post.getLikes()), new byte[]{1,1}));
			cmtCnt.add(new Element<>(post.getPid(), String.valueOf(post.getCmtCnt()), new byte[]{1,1}));
		}

		setPostListCache("PostContent:"+bid, contents, attributes);
		setPostListCache("PostList:"+bid, posts, attributes);
		setPostListCache("PostViews:"+bid, views, attributes);
		setPostListCache("PostLikes:"+bid, likes, attributes);
		setPostListCache("PostCmtCnt:"+bid, cmtCnt, attributes);

		log.warn("[ARCUS] Set PostList : "+bid);
	}

	public void setPostListCache(String key, List<Element<Object>> elements, CollectionAttributes attributes) {
		CollectionFuture<Map<Integer, CollectionOperationStatus>> future = null;
		try {
			future = arcusClient.asyncBopPipedInsertBulk(key, elements, attributes);
			Map<Integer, CollectionOperationStatus> result = future.get(1000L, TimeUnit.MILLISECONDS);
			if (!result.isEmpty()) {
				log.error("[ARCUS] Some items failed to insert.");
				for (Map.Entry<Integer, CollectionOperationStatus> entry : result.entrySet()) {
					log.error("Failed item = " + elements.get(entry.getKey()));
					log.error("Caused by " + entry.getValue().getResponse());
				}
			}
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
	}

	public void insertPost(int pid) {
		Post post = postRepository.selectOne(pid);

		insertPostCache("PostContent:"+post.getBid(), pid, post.getContent());
		post.setContent(null);
		insertPostCache("PostList:"+post.getBid(), pid, post);
		insertPostCache("PostViews:"+post.getBid(), pid, String.valueOf(0));
		insertPostCache("PostLikes:"+post.getBid(), pid, String.valueOf(0));
		insertPostCache("PostCmtCnt:"+post.getBid(), pid, String.valueOf(0));
	}

	public void insertPostCache(String key, long bkey, Object value) {
		CollectionFuture<Boolean> future = null;
		try {
			future = arcusClient.asyncBopInsert(key, bkey, new byte[]{1,1}, value, null);
			if(!future.get(1000L, TimeUnit.MILLISECONDS))
				log.error("[ARCUS] Failed to insert : " + key + ":" + bkey);
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
	}

	public void increaseViews(int bid, int pid) {
		String key = "PostViews:"+bid;
		CollectionFuture<Long> future = null;
		try {
			future = arcusClient.asyncBopIncr(key, pid, 1);
			Long result = future.get(1000L, TimeUnit.MILLISECONDS);
			if (result == null) {
				log.error("[ARCUS] Failed to increase views : " + key + ":" + pid);
				log.error(String.valueOf(future.getOperationStatus().getResponse()));
			}
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
	}

	public void increaseLikes(int bid, int pid) {
		String key = "PostLikes:"+bid;
		CollectionFuture<Long> future = null;
		try {
			future = arcusClient.asyncBopIncr(key, pid, 1);
			Long result = future.get(1000L, TimeUnit.MILLISECONDS);
			if (result == null) {
				log.error("[ARCUS] Failed to increase Likes : " + key + ":" + pid);
				log.error(String.valueOf(future.getOperationStatus().getResponse()));
			}
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
	}

	public void increaseCmtCnt(int bid, int pid) {
		String key = "PostCmtCnt:"+bid;
		CollectionFuture<Long> future = null;
		try {
			future = arcusClient.asyncBopIncr(key, pid, 1);
			Long result = future.get(1000L, TimeUnit.MILLISECONDS);
			if (result == null) {
				log.error("[ARCUS] Failed to increase CmtCnt : " + key + ":" + pid);
				log.error(String.valueOf(future.getOperationStatus().getResponse()));
			}
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
	}

	public void decreaseCmtCnt(int bid, int pid) {
		String key = "PostCmtCnt:"+bid;
		CollectionFuture<Long> future = null;
		try {
			future = arcusClient.asyncBopDecr(key, pid, 1);
			Long result = future.get(1000L, TimeUnit.MILLISECONDS);
			if (result == null) {
				log.error("[ARCUS] Failed to decrease CmtCnt : " + key + ":" + pid);
				log.error(String.valueOf(future.getOperationStatus().getResponse()));
			}
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
	}

	public void deletePost(int bid, int pid) {
		deletePostCache("PostList:"+bid, pid);
		deletePostCache("PostContent:"+bid, pid);
		deletePostCache("PostViews:"+bid, pid);
		deletePostCache("PostLikes:"+bid, pid);
		deletePostCache("PostCmtCnt:"+bid, pid);
	}

	public void deletePostCache(String key, long bkey) {
		CollectionFuture<Boolean> future = null;
		try {
			future = arcusClient.asyncBopDelete(key, bkey, ElementFlagFilter.DO_NOT_FILTER, false);
			future.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = future.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.NOT_FOUND_ELEMENT)) return;
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
	}

	public void updatePost(Post post) {
		CollectionFuture<Boolean> ContentFuture = null;
		CollectionFuture<Boolean> postListFuture = null;
		try {
			ContentFuture = arcusClient.asyncBopUpdate("PostContent:"+post.getBid(), post.getPid(), null, post.getContent());
			ContentFuture.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = ContentFuture.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.NOT_FOUND_ELEMENT)) return;
		} catch (Exception e) {
			Optional.ofNullable(ContentFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
		try {
			post.setContent(null);
			postListFuture = arcusClient.asyncBopUpdate("PostList:"+post.getBid(), post.getPid(), null, post);
			postListFuture.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = postListFuture.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.NOT_FOUND_ELEMENT)) return;
		} catch (Exception e) {
			Optional.ofNullable(postListFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
	}

	public Post getPost(int bid, int pid) {
		Post post = new Post();

		CollectionFuture<Map<Long, Element<Object>>> postListFuture = null;
		CollectionFuture<Map<Long, Element<Object>>> contentFuture = null;
		CollectionFuture<Map<Long, Element<Object>>> viewsFuture = null;
		CollectionFuture<Map<Long, Element<Object>>> likesFuture = null;
		CollectionFuture<Map<Long, Element<Object>>> commentCountFuture = null;

		try {
			postListFuture = arcusClient.asyncBopGet("PostList:"+bid, pid, ElementFlagFilter.DO_NOT_FILTER, false, false);
			Map<Long, Element<Object>> result = postListFuture.get(1000L, TimeUnit.MILLISECONDS);
			if (!postListFuture.getOperationStatus().isSuccess()) {
				postCachingQueue.put(bid);
				return postRepository.selectOne(pid);
			}
			post = (Post) result.get((long)pid).getValue();
		} catch (Exception e) {
			Optional.ofNullable(postListFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
			return postRepository.selectOne(pid);
		}
		try {
			contentFuture = arcusClient.asyncBopGet("PostContent:"+bid, pid, ElementFlagFilter.DO_NOT_FILTER, false, false);
			Map<Long, Element<Object>> result = contentFuture.get(1000L, TimeUnit.MILLISECONDS);
			if (!contentFuture.getOperationStatus().isSuccess()) {
				postCachingQueue.put(bid);
				return postRepository.selectOne(pid);
			}
			post.setContent((String)result.get((long)pid).getValue());
		} catch (Exception e) {
			Optional.ofNullable(contentFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
			return postRepository.selectOne(pid);
		}
		try {
			viewsFuture = arcusClient.asyncBopGet("PostViews:"+bid, pid, ElementFlagFilter.DO_NOT_FILTER, false, false);
			Map<Long, Element<Object>> result = viewsFuture.get(1000L, TimeUnit.MILLISECONDS);
			if (!viewsFuture.getOperationStatus().isSuccess()) {
				postCachingQueue.put(bid);
				return postRepository.selectOne(pid);
			}
			post.setViews(Integer.parseInt((String)result.get((long)pid).getValue()));
		} catch (Exception e) {
			Optional.ofNullable(viewsFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
			return postRepository.selectOne(pid);
		}
		try {
			likesFuture = arcusClient.asyncBopGet("PostLikes:"+bid, pid, ElementFlagFilter.DO_NOT_FILTER, false, false);
			Map<Long, Element<Object>> result = likesFuture.get(1000L, TimeUnit.MILLISECONDS);
			if (!likesFuture.getOperationStatus().isSuccess()) {
				postCachingQueue.put(bid);
				return postRepository.selectOne(pid);
			}
			post.setLikes(Integer.parseInt((String)result.get((long)pid).getValue()));
		} catch (Exception e) {
			Optional.ofNullable(likesFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
			return postRepository.selectOne(pid);
		}
		try {
			commentCountFuture = arcusClient.asyncBopGet("PostCmtCnt:"+bid, pid, ElementFlagFilter.DO_NOT_FILTER, false, false);
			Map<Long, Element<Object>> result = commentCountFuture.get(1000L, TimeUnit.MILLISECONDS);
			if (!commentCountFuture.getOperationStatus().isSuccess()) {
				postCachingQueue.put(bid);
				return postRepository.selectOne(pid);
			}
			post.setCmtCnt(Integer.parseInt((String)result.get((long)pid).getValue()));
		} catch (Exception e) {
			Optional.ofNullable(commentCountFuture).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
			return postRepository.selectOne(pid);
		}

		return post;
	}

	public int getPostCount(int bid) {
		int postCount = 0;
		CollectionFuture<Map<Long, Element<Object>>> future = null;
		try {
			future = arcusClient.asyncBopGet("PostCount", bid, ElementFlagFilter.DO_NOT_FILTER, false, false);
			Map<Long, Element<Object>> result = future.get(700L, TimeUnit.MILLISECONDS);
			if (!future.getOperationStatus().isSuccess()) {
				return setPostCount(bid);
			}
			postCount = Integer.parseInt((String)result.get((long)bid).getValue());
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}

		return postCount;
	}

	public int setPostCount(int bid) {
		int postCount = postRepository.countPost(bid);
		CollectionAttributes attributes = new CollectionAttributes();
		attributes.setExpireTime(3600);
		CollectionFuture<Boolean> future = null;
		try {
			future = arcusClient.asyncBopUpsert("PostCount", bid, new byte[]{1,1}, String.valueOf(postCount), attributes);
			if(!future.get(1000L, TimeUnit.MILLISECONDS))
				log.error("[ARCUS] Failed to set PostCount:" + bid);
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
		return postCount;
	}

	public void increasePostCount(int bid) {
		CollectionFuture<Long> future = null;
		try {
			future = arcusClient.asyncBopIncr("PostCount", bid, 1);
			Long result = future.get(700L, TimeUnit.MILLISECONDS);
			if (result==null) {
				log.error("[ARCUS] Failed to increase PostCount:" + bid);
				log.error(String.valueOf(future.getOperationStatus().getResponse()));
			}
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
	}

	public void decreasePostCount(int bid) {
		CollectionFuture<Long> future = null;
		try {
			future = arcusClient.asyncBopDecr("PostCount", bid, 1);
			Long result = future.get(700L, TimeUnit.MILLISECONDS);
			if (result==null) {
				log.error("[ARCUS] Failed to decrease PostCount:" + bid);
				log.error(String.valueOf(future.getOperationStatus().getResponse()));
			}
		} catch (Exception e) {
			Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
			log.error(e.getMessage(), e);
		}
	}

}