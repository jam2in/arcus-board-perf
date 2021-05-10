package com.jam2in.arcus.board.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.jam2in.arcus.board.model.BestPost;
import com.jam2in.arcus.board.repository.LeaderBoardRepository;
import com.jam2in.arcus.board.repository.PostRepository;

@Service
public class LeaderBoardService {
	@Autowired
	private LeaderBoardRepository leaderBoardRepository;
	@Autowired
	private PostRepository postRepository;

	@Scheduled(cron = "0 0/10 * * * *")
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
}
