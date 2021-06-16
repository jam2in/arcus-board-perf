package com.jam2in.arcus.board.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.spy.memcached.ArcusClientPool;

import com.jam2in.arcus.board.configuration.ArcusConfiguration;
import com.jam2in.arcus.board.model.BestPost;
import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.repository.LeaderBoardRepository;
import com.jam2in.arcus.board.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LeaderBoardService {
	@Autowired
	private LeaderBoardRepository leaderBoardRepository;
	@Autowired
	private PostRepository postRepository;

	ApplicationContext context = new AnnotationConfigApplicationContext(ArcusConfiguration.class);
	ArcusClientPool arcusClient = context.getBean("arcusClient", ArcusClientPool.class);

	@Transactional
	@Scheduled(fixedDelay = 600000)
	public void resetLeaderBoard() {
		leaderBoardRepository.deleteBestLikesAll();
		leaderBoardRepository.deleteBestLikesBoard();
		leaderBoardRepository.deleteBestViewsAll();
		leaderBoardRepository.deleteBestViewsBoard();

		List<Integer> bestLikesPidAll;
		List<Integer> bestViewsPidAll;
		List<BestPost> bestLikesPostList = new ArrayList<>();
		List<BestPost> bestViewsPostList = new ArrayList<>();

		for (int period=0; period<3; period++) {
			if(period == 0) {
				bestLikesPidAll = postRepository.bestLikesToday();
				bestViewsPidAll = postRepository.bestViewsToday();
			}
			else if (period == 1) {
				bestLikesPidAll = postRepository.bestLikesMonth();
				bestViewsPidAll = postRepository.bestViewsMonth();
			}
			else {
				bestLikesPidAll = postRepository.bestLikesAll();
				bestViewsPidAll = postRepository.bestViewsAll();
			}

			if (!bestLikesPidAll.isEmpty()) {
				for (int i = 0; i < 10; i++) {
					BestPost bestPost = BestPost.builder()
						.period(period)
						.rank(i+1)
						.pid(bestLikesPidAll.get(i))
						.build();

					bestLikesPostList.add(bestPost);
				}
				bestLikesPidAll.clear();
			}

			if (!bestViewsPidAll.isEmpty()) {
				for (int i = 0; i < 10; i++) {
					BestPost bestPost = BestPost.builder()
						.period(period)
						.rank(i+1)
						.pid(bestViewsPidAll.get(i))
						.build();

					bestViewsPostList.add(bestPost);
				}
				bestViewsPidAll.clear();
			}
		}
		leaderBoardRepository.insertBestLikesAll(bestLikesPostList);
		leaderBoardRepository.insertBestViewsAll(bestViewsPostList);


		List<Integer> bestLikesPidBoard;
		List<Integer> bestViewsPidBoard;
		bestLikesPostList.clear();
		bestViewsPostList.clear();

		for (int bid=1; bid<12; bid++) {
			for (int period=0; period<3; period++) {
				if(period == 0) {
					bestLikesPidBoard = postRepository.bestLikesTodayBoard(bid);
					bestViewsPidBoard = postRepository.bestViewsTodayBoard(bid);
				}
				else if (period == 1) {
					bestLikesPidBoard = postRepository.bestLikesMonthBoard(bid);
					bestViewsPidBoard = postRepository.bestViewsMonthBoard(bid);
				}
				else {
					bestLikesPidBoard = postRepository.bestLikesAllBoard(bid);
					bestViewsPidBoard = postRepository.bestViewsAllBoard(bid);
				}

				if (!bestLikesPidBoard.isEmpty()) {
					for (int i = 0; i < 10; i++) {
						BestPost bestPost = BestPost.builder()
							.bid(bid)
							.period(period)
							.rank(i+1)
							.pid(bestLikesPidBoard.get(i))
							.build();

						bestLikesPostList.add(bestPost);
					}
					bestLikesPidBoard.clear();
				}

				if (!bestViewsPidBoard.isEmpty()) {
					for (int i = 0; i < 10; i++) {
						BestPost bestPost = BestPost.builder()
							.bid(bid)
							.period(period)
							.rank(i+1)
							.pid(bestViewsPidBoard.get(i))
							.build();

						bestViewsPostList.add(bestPost);
					}
					bestViewsPidBoard.clear();
				}
			}
		}
		leaderBoardRepository.insertBestLikesBoard(bestLikesPostList);
		leaderBoardRepository.insertBestViewsBoard(bestViewsPostList);

	}

	public List<Post> bestLikesAll(int period) {
		List<Post> bestLikes = null;
		if (period==0) {
			Future<Object> future = null;
			try {
				future = arcusClient.asyncGet("BestLikesToday:All");
				bestLikes = (List<Post>)future.get(700L, TimeUnit.MILLISECONDS);
				if (bestLikes == null) {
					bestLikes = leaderBoardRepository.bestLikesAll(period);
					arcusClient.set("BestLikesToday:All", 600, bestLikes);
				}
			} catch (Exception e) {
				Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
				log.error(e.getMessage(), e);
			}
		}
		else {
			bestLikes = leaderBoardRepository.bestLikesAll(period);
		}

		return bestLikes;
	}

	public List<Post> bestViewsAll(int period) {
		List<Post> bestViews = null;
		if (period==0) {
			Future<Object> future = null;
			try {
				future = arcusClient.asyncGet("BestViewsToday:All");
				bestViews = (List<Post>)future.get(700L, TimeUnit.MILLISECONDS);
				if (bestViews == null) {
					bestViews = leaderBoardRepository.bestViewsAll(period);
					arcusClient.set("BestViewsToday:All", 600, bestViews);
				}
			} catch (Exception e) {
				Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
				log.error(e.getMessage(), e);
			}
		}
		else {
			bestViews = leaderBoardRepository.bestViewsAll(period);
		}

		return bestViews;
	}

	public List<Post> bestLikesBoard(int bid, int period) {
		List<Post> bestLikes = null;
		if (period==0) {
			Future<Object> future = null;
			try {
				future = arcusClient.asyncGet("BestLikesToday:" + bid);
				bestLikes = (List<Post>)future.get(1000L, TimeUnit.MILLISECONDS);
				if (bestLikes == null) {
					bestLikes = leaderBoardRepository.bestLikesBoard(bid, period);
					arcusClient.set("BestLikesToday:" + bid, 600, bestLikes);
				}
			} catch (Exception e) {
				Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
				log.error(e.getMessage(), e);
			}
		}
		return bestLikes;
	}

	public List<Post> bestViewsBoard(int bid, int period) {
		List<Post> bestViews = null;
		if (period==0) {
			Future<Object> future = null;
			try {
				future = arcusClient.asyncGet("BestViewsToday:" + bid);
				bestViews = (List<Post>)future.get(1000L, TimeUnit.MILLISECONDS);
				if (bestViews == null) {
					bestViews = leaderBoardRepository.bestViewsBoard(bid, period);
					arcusClient.set("BestViewsToday:" + bid, 600, bestViews);
				}
			} catch (Exception e) {
				Optional.ofNullable(future).ifPresent(f -> f.cancel(true));
				log.error(e.getMessage(), e);
			}
		}
		return bestViews;
	}

}
