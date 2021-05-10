package com.jam2in.arcus.board.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.jam2in.arcus.board.model.Post;
import com.jam2in.arcus.board.repository.PostRepository;

@Service
public class TestService {
	@Autowired
	private PostRepository postRepository;

	public int selectLatestRandom(int bid) {
		List<Post> postList = postRepository.selectAll(bid, 0, 100);
		int index = (int)(Math.random() * 100);
		return postList.get(index).getPid();
	}

	public void resetData() throws SQLException {
		Connection connection = DriverManager.getConnection("url","user","password");
		try {
			ScriptRunner sr = new ScriptRunner(connection);
			ClassPathResource resource = new ClassPathResource("resetTestData.sql");
			Reader reader = new BufferedReader(new FileReader(resource.getFile()));
			sr.runScript(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
