# arcus-board-perf
웹 응용 애플리케이션에 ARCUS Cache를 적용했을 때의 성능 개선 효과를 확인하기 위한 웹 게시판입니다.

- Branches 
  - **main** : MySQL DB만을 사용해서 구현한 웹 게시판
  - **cache_[name]** : ARCUS Cache를 적용하여 구현한 웹 게시판


### 게시판 기능
게시판의 기능은 다음과 같습니다.
- 게시판 조회 (게시글 목록 조회)
- 게시글 조회, 생성, 수정, 삭제, 추천 증가, 페이징
- 댓글 조회, 생성, 수정, 삭제, 페이징
- 홈 - 리더보드
  - 인기 게시판 랭킹 리더보드 (각 게시판 별 게시글 및 댓글 CRUD 요청량 계산)
  - 조회수/추천수 많은 글 랭킹 리더보드
  - 최근 공지사항 리더보드 
  
## properties 설정
프로젝트 실행을 위해 properties 파일에 값을 설정해야 합니다.
### application.properties
```properties:application.properties
spring.datasource.url = jdbc:mysql://[IP:PORT]/[SCHEMA]?characterEncoding=UTF-8&serverTimezone=Asia/Seoul&allowLoadLocalInfile=true
spring.datasource.username = USER_NAME
spring.datasource.password = PASSWORD
```
`[IP:PORT]` - ip : database 연결 주소, port : 포트 번호<br>
`[SCHEMA]` - 사용할 database schema의 이름<br>
`USER_NAME` - MySQL 접속 계정 이름<br>
`PASSWORD` - MySQL 비밀번호<br>

위의 부분에 해당하는 코드를 변경하여 값을 설정해줍니다.


### arcus.properties
```properties:arcus.properties
arcus.address = ADDRESS
arcus.serviceCode = SERVICE_CODE
arcus.poolSize = POOL_SIZE
```
`ADDRESS` - arcus admin address<br>
`SERVICE_CODE` - arcus service code<br>
`POOL_SIZE` - arcus client pool size<br>

ARCUS 캐시 적용 시에만 설정하는 properties 파일입니다.
마찬가지로 위의 부분에 해당하는 코드를 변경하여 값을 설정해줍니다.

## 성능 테스트
### 초기 데이터 저장
먼저, DB에 성능 테스트를 위한 데이터를 저장합니다.
1. [data csv](https://works.do/5ckYhf) 파일을 다운로드 받아 응용 서버와 같은 위치에 저장합니다.
2. resetTestData.sql 의 `[csv file path]` 부분에 저장한 csv 파일의 경로를 넣어 변경합니다.
```SQL:resetTestData.sql
LOAD DATA LOCAL INFILE '[csv file path]' INTO TABLE ...
```
3. 테스트 시작 전, 웹 응용 서버를 구동하고 `/test/reset` url을 실행하여 DB에 초기 데이터를 저장합니다. 항상 동일한 데이터를 가지고 테스트를 진행해야 하기때문에, 이 과정은 매번 테스트 시 반복적으로 수행하여야 합니다.

### 성능 테스트
성능테스트의 Client 부하는 JMeter를 이용합니다. Client에서 [JMeter Test Plan](https://works.do/GbvhY8) 파일을 다운로드 받아 실행합니다.

- 홈 조회
- 게시판 조회 (게시글 목록 조회)
- 게시글 조회

위의 3가지 조회 요청에 대한 TPS, Response Time를 확인하여 성능을 측정합니다.
