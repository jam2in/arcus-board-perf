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
import net.spy.memcached.collection.CollectionResponse;
import net.spy.memcached.collection.Element;
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
		List<Comment> commentList = null;
		String key = "CommentList:"+pid;
		int posFrom = startList;
		int posTo = startList + pageSize - 1;
		CollectionFuture<Map<Integer, Element<Object>>> future = arcusClient.asyncBopGetByPosition(key, BTreeOrder.DESC, posFrom, posTo);
		try {
			Map<Integer, Element<Object>> result = future.get(1000L, TimeUnit.MILLISECONDS);
			CollectionResponse response = future.getOperationStatus().getResponse();
			if (response.equals(CollectionResponse.NOT_FOUND)) {

			}
			for (Map.Entry<Integer, Element<Object>> each : result.entrySet()) {
				commentList.add((Comment)each.getValue().getValue());
			}
			log.info("[ARCUS] get : " + key);
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}



		return commentList;
	}

	public void setCommentList(int pid, int pageSize) {
		List<Comment> commentList = commentRepository.selectAll(pid, 0, pageSize*5);
		String key = "CommentList:"+pid;

		List<Element<Object>> elements = new ArrayList<>();
		for (Comment comment : commentList) {
			elements.add(new Element<>(comment.getCid(), comment, new byte[]{1,1}));
		}

		if (elements.size() > arcusClient.getMaxPipedItemCount()) {
			log.error("[ARCUS] ");
			return;
		}

		CollectionFuture<Map<Integer, CollectionOperationStatus>> future = null;
		try {
			// TODO: exp 설정 ?
			future = arcusClient.asyncBopPipedInsertBulk(key, elements, new CollectionAttributes());
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		if (future == null)	return;

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
				log.info("[ARCUS] set : " + key);
			}
		} catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
	}
}
