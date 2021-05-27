package com.jam2in.arcus.board.Arcus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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
import com.jam2in.arcus.board.model.Comment;
import com.jam2in.arcus.board.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommentArcus {
	@Autowired
	CommentRepository commentRepository;

	ApplicationContext context = new AnnotationConfigApplicationContext(ArcusConfiguration.class);
	ArcusClientPool arcusClient = context.getBean("arcusClient", ArcusClientPool.class);

	public List<Comment> getCommentList(int pid, int startList, int pageSize) {
		List<Comment> commentList = new ArrayList<>();
		String key = "CommentList:"+pid;
		if (startList >= pageSize*5) {
			commentList = commentRepository.selectAll(pid, startList, pageSize);
		}
		else {
			int posTo = startList + pageSize - 1;
			CollectionFuture<Map<Integer, Element<Object>>> future = arcusClient.asyncBopGetByPosition(key, BTreeOrder.ASC, startList, posTo);
			try {
				Map<Integer, Element<Object>> result = future.get(1000L, TimeUnit.MILLISECONDS);
				CollectionResponse response = future.getOperationStatus().getResponse();
				if (response.equals(CollectionResponse.NOT_FOUND)) {
					return setCommentList(pid, pageSize);
				}
				for (Map.Entry<Integer, Element<Object>> each : result.entrySet()) {
					commentList.add((Comment)each.getValue().getValue());
				}
			} catch (Exception e) {
				future.cancel(true);
				e.printStackTrace();
			}
		}

		return commentList;
	}

	public List<Comment> setCommentList(int pid, int pageSize) {
		List<Comment> commentList = commentRepository.selectAll(pid, 0, pageSize*5);
		String key = "CommentList:"+pid;

		CollectionAttributes attributes = new CollectionAttributes(3600, pageSize*5L, CollectionOverflowAction.error);

		if (commentList.size() == 0) {
			arcusClient.asyncBopCreate(key, ElementValueType.OTHERS, attributes);
			return commentList;
		}

		List<Element<Object>> elements = new ArrayList<>();
		for (Comment comment : commentList) {
			elements.add(new Element<>(comment.getCid(), comment, new byte[]{1,1}));
		}

		CollectionFuture<Map<Integer, CollectionOperationStatus>> future = null;
		try {
			future = arcusClient.asyncBopPipedInsertBulk(key, elements, attributes);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		try {
			Map<Integer, CollectionOperationStatus> result = future.get(1000L, TimeUnit.MILLISECONDS);

			if (!result.isEmpty()) {
				log.error("[ARCUS] Some items failed to insert.");
				for (Map.Entry<Integer, CollectionOperationStatus> entry : result.entrySet()) {
					log.error("Failed item = " + elements.get(entry.getKey()));
					log.error("Caused by " + entry.getValue().getResponse());
				}
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}

		return commentList;
	}

	public void insertComment(int cid) {
		Comment comment = commentRepository.selectOne(cid);
		String key = "CommentList:"+comment.getPid();
		CollectionFuture<Boolean> future = arcusClient.asyncBopInsert(key, comment.getCid(), new byte[]{1,1}, comment, null);
		try {
			future.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = future.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.OVERFLOWED)) return;
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

	public void updateComment(Comment comment) {
		String key = "CommentList:"+comment.getPid();
		Comment updateCmt = getComment(comment.getPid(), comment.getCid());
		updateCmt.setContent(comment.getContent());
		CollectionFuture<Boolean> future = arcusClient.asyncBopUpdate(key, comment.getCid(), null, updateCmt);
		try {
			future.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = future.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.NOT_FOUND_ELEMENT)) return;
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

	public void deleteComment(int pid, int cid) {
		String key = "CommentList:"+pid;
		CollectionFuture<Boolean> future = arcusClient.asyncBopDelete(key, cid, ElementFlagFilter.DO_NOT_FILTER, false);
		if (future == null) return;
		try {
			future.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = future.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.NOT_FOUND_ELEMENT)) return;
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}

	public Comment getComment(int pid, int cid) {
		Comment comment = new Comment();
		String key = "CommentList:"+pid;
		CollectionFuture<Map<Long, Element<Object>>> future = arcusClient.asyncBopGet(key, cid, ElementFlagFilter.DO_NOT_FILTER, false, false);
		try {
			Map<Long, Element<Object>> result = future.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = future.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.NOT_FOUND_ELEMENT)) {
				return commentRepository.selectOne(cid);
			}
			comment = (Comment) result.get((long)cid).getValue();
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
		return comment;
	}
}
