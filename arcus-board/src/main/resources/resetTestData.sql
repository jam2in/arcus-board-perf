SET FOREIGN_KEY_CHECKS=0;

TRUNCATE user;
TRUNCATE board;
TRUNCATE category_board;
TRUNCATE category_post;
TRUNCATE best_board_request;

TRUNCATE best_likes_all;
TRUNCATE best_likes_board;
TRUNCATE best_views_all;
TRUNCATE best_views_board;

TRUNCATE comment;
TRUNCATE post;

SET FOREIGN_KEY_CHECKS=1;

LOAD DATA LOCAL INFILE '/home/jam2in/arcus-board-perf/arcus-board/csv/user.csv' INTO TABLE user
CHARACTER SET utf8 FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n' (uid, name, created_date);

INSERT INTO board(bid, name, category, req_today) VALUES(1,"전체 공지사항",1,0);
INSERT INTO board(bid, name, category, req_today) VALUES(2,"C",2,0);
INSERT INTO board(bid, name, category, req_today) VALUES(3,"Java",2,0);
INSERT INTO board(bid, name, category, req_today) VALUES(4,"Windows",3,0);
INSERT INTO board(bid, name, category, req_today) VALUES(5,"Linux",3,0);
INSERT INTO board(bid, name, category, req_today) VALUES(6,"Spring",4,0);
INSERT INTO board(bid, name, category, req_today) VALUES(7,"Laravel",4,0);
INSERT INTO board(bid, name, category, req_today) VALUES(8,"React",5,0);
INSERT INTO board(bid, name, category, req_today) VALUES(9,"AngularJS",5,0);
INSERT INTO board(bid, name, category, req_today) VALUES(10,"AWS",6,0);
INSERT INTO board(bid, name, category, req_today) VALUES(11,"Azure",6,0);

INSERT INTO category_board(id, name) VALUES(1, "전체 공지사항");
INSERT INTO category_board(id, name) VALUES(2, "언어");
INSERT INTO category_board(id, name) VALUES(3, "OS");
INSERT INTO category_board(id, name) VALUES(4,"Backend");
INSERT INTO category_board(id, name) VALUES(5,"Frontend");
INSERT INTO category_board(id, name) VALUES(6,"Cloud");

INSERT INTO category_post(id, name) VALUES(1, "공지");
INSERT INTO category_post(id, name) VALUES(2, "질문");
INSERT INTO category_post(id, name) VALUES(3, "잡담");

LOAD DATA LOCAL INFILE '/home/jam2in/arcus-board-perf/arcus-board/csv/time.csv' INTO TABLE best_board_request
CHARACTER SET utf8 FIELDS TERMINATED BY ','
LINES TERMINATED BY ';' (bid, time);

LOAD DATA LOCAL INFILE '/home/jam2in/arcus-board-perf/arcus-board/csv/post.csv' INTO TABLE post
CHARACTER SET utf8 FIELDS TERMINATED BY ','
LINES TERMINATED BY ';' (pid, uid, bid, category, title, content, likes, views, cmtCnt);

LOAD DATA LOCAL INFILE '/home/jam2in/arcus-board-perf/arcus-board/csv/cmt.csv' INTO TABLE comment
CHARACTER SET utf8 FIELDS TERMINATED BY ','
LINES TERMINATED BY ';' (uid,pid,content);