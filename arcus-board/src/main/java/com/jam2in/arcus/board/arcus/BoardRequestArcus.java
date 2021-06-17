package com.jam2in.arcus.board.arcus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.spy.memcached.ArcusClientPool;
import net.spy.memcached.internal.BulkFuture;

import com.jam2in.arcus.board.repository.BoardRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BoardRequestArcus {

	private static final int BOARD_COUNT = 11;

	public static final String ARCUS_KEY_BOARD_REQUEST_RECENT_PREFIX = "BoardRequestRecent";

	public static final String ARCUS_KEY_BOARD_REQUEST_TODAY_PREFIX = "BoardRequestToday";

	private static final List<String> ARCUS_KEYS_BOARD_REQUEST_RECENT =
		IntStream.range(2, BOARD_COUNT + 1)
			.mapToObj(BoardRequestArcus::makeRequestRecentKey)
			.collect(Collectors.toList());

	private static final List<String> ARCUS_KEYS_BOARD_REQUEST_TODAY =
		IntStream.range(2, BOARD_COUNT + 1)
			.mapToObj(BoardRequestArcus::makeRequestTodayKey)
			.collect(Collectors.toList());

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private ArcusClientPool arcusClient;

	private void updateRequest(List<String> requestKeys, BiConsumer<Integer, Integer> updater) {
		// FIXME: 실패한 연산 처리
		BulkFuture<Map<String, Object>> bulkFuture = null;

		try {
			bulkFuture = arcusClient.asyncGetBulk(requestKeys);

			for (Map.Entry<String, Object> entry : bulkFuture.get(Long.MAX_VALUE, TimeUnit.MILLISECONDS).entrySet()) {
				try {
					arcusClient
						.asyncDecr(entry.getKey(), Integer.parseInt(entry.getValue().toString()))
						.get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

					updater.accept(getBidFromKey(entry.getKey()), Integer.parseInt(entry.getValue().toString()));
				} catch (Exception e) {
					// do nothing. if any exception is caught, just skip.
					log.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Optional.ofNullable(bulkFuture).ifPresent(future -> future.cancel(true));
		}
	}

	@Scheduled(fixedDelay = 300_000 /* 5 minutes */)
	public void updateRequestRecent() {
		updateRequest(ARCUS_KEYS_BOARD_REQUEST_RECENT, (bid, count) -> {
			boardRepository.updateReqRecent(bid, count);
		});
	}

	@Scheduled(fixedDelay = 300_000 /* 5 minutes */)
	public void updateRequestToday() {
		updateRequest(ARCUS_KEYS_BOARD_REQUEST_TODAY, (bid, count) -> {
			boardRepository.updateReqToday(bid, count);
		});
	}

	public int getBidFromKey(String key) {
		return Integer.parseInt(key.split(":")[1]);
	}

	public void increaseRequestRecent(int bid) {
		try {
			arcusClient.incr(makeRequestRecentKey(bid), 1, 0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void increaseRequestToday(int bid) {
		try {
			arcusClient.incr(makeRequestTodayKey(bid), 1, 0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static String makeRequestRecentKey(int bid) {
		return ARCUS_KEY_BOARD_REQUEST_RECENT_PREFIX + ":" + bid;
	}

	private static String makeRequestTodayKey(int bid) {
		return ARCUS_KEY_BOARD_REQUEST_TODAY_PREFIX + ":" + bid;
	}

}