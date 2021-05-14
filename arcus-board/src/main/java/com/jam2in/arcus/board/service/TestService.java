package com.jam2in.arcus.board.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam2in.arcus.board.repository.PostRepository;

@Service
public class TestService {
	@Autowired
	private PostRepository postRepository;

	@Value("${spring.datasource.url}")
	private String url;
	@Value("${spring.datasource.username}")
	private String user;
	@Value("${spring.datasource.password}")
	private String password;

	public int selectLatestRandom(int bid) {
		List<Integer> pidList = postRepository.selectLatestPid(bid);
		int index = (int)(Math.random() * 100);
		return pidList.get(index);
	}

	@Transactional
	public void resetData() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			ScriptRunner sr = new ScriptRunner(connection);
			ClassPathResource resource = new ClassPathResource("resetTestData.sql");
			InputStream inputStream = resource.getInputStream();
			File sqlFile = File.createTempFile("resetTestData", ".sql");
			try {
				FileUtils.copyInputStreamToFile(inputStream, sqlFile);
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
			Reader reader = new BufferedReader(new FileReader(sqlFile));
			sr.runScript(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
