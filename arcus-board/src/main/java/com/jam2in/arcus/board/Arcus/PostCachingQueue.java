package com.jam2in.arcus.board.Arcus;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PostCachingQueue {
	private BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(10);
	@Autowired
	private PostArcus postArcus;

	@Async("PutExecutor")
	public void put(int bid) {
		if (!queue.contains(bid)) {
			try {
				queue.put(bid);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Async("CachingExecutor")
	public void cachingPost() {
		while (true) {
			try {
				int bid = queue.take();
				postArcus.setPostList(bid);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
