package com.jam2in.arcus.board.repository;

import com.jam2in.arcus.board.model.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CommentRepository {
    void insert(Comment comment);
    void update(Comment comment);
    void delete(int cid);
    Comment selectOne(int cid);
    List<Comment> selectAll(@Param("pid") int pid, @Param("startList") int startList, @Param("pageSize") int pageSize);
}
