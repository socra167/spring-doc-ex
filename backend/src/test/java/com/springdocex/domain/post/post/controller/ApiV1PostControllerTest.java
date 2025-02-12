package com.springdocex.domain.post.post.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.springdocex.domain.member.member.entity.Member;
import com.springdocex.domain.member.member.service.MemberService;
import com.springdocex.domain.post.post.entity.Post;
import com.springdocex.domain.post.post.service.PostService;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApiV1PostControllerTest {
	@Autowired
	private MockMvc mvc;

	@Autowired
	private PostService postService;

	@Autowired
	private MemberService memberService;

	private Member loginMember;
	private String token;

	@BeforeEach
	void setUp() {
		loginMember = memberService.findByUsername("user2").get();
		token = memberService.getAuthToken(loginMember);
	}

	@Nested
	@DisplayName("글 목록 조회")
	class items {

		@Test
		@DisplayName("성공 - 글 목록을 조회할 수 있으며 결과는 페이징되어야 한다")
		void itemsA() throws Exception {
			var page = 1;
			var pageSize = 3;
			var resultActions = mvc
				.perform(
					get("/api/v1/posts")
				)
				.andDo(print());
			var posts = postService.getListedItems(page, pageSize, "title", "").getContent();
			checkPosts(resultActions, posts);

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("getItems"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
				.andExpect(jsonPath("$.data.items.length()").value(pageSize)) // 한 페이지당 보여줄 글 개수
				.andExpect(jsonPath("$.data.currentPageNo").value(page)) // 현재 페이지
				.andExpect(jsonPath("$.data.totalPages").isNumber()); // 전체 페이지 개수
		}

		private void checkPosts(ResultActions resultActions, List<Post> posts) throws Exception {
			for (int i = 0; i < posts.size(); i++) {
				Post post = posts.get(i);
				resultActions
					.andExpect(jsonPath("$.data.items[%d]".formatted(i)).exists())
					.andExpect(jsonPath("$.data.items[%d].id".formatted(i)).value(post.getId()))
					.andExpect(jsonPath("$.data.items[%d].title".formatted(i)).value(post.getTitle()))
					.andExpect(jsonPath("$.data.items[%d].authorId".formatted(i)).value(post.getAuthor().getId()))
					.andExpect(jsonPath("$.data.items[%d].authorName".formatted(i)).value(post.getAuthor().getNickname()))
					.andExpect(jsonPath("$.data.items[%d].published".formatted(i)).value(post.isPublished()))
					.andExpect(jsonPath("$.data.items[%d].listed".formatted(i)).value(post.isListed()))
					.andExpect(jsonPath("$.data.items[%d].createdDate".formatted(i)).value(
						matchesPattern(post.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
					.andExpect(jsonPath("$.data.items[%d].modifiedDate".formatted(i)).value(
						matchesPattern(post.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
			}
		}

		@Test
		@DisplayName("성공 - 제목으로 글을 검색할 수 있으며 결과는 페이징되어야 한다")
		void itemsB_searchPostsByTitle() throws Exception {
			var page = 1;
			var pageSize = 3;
			var keywordType = "title";
			var keyword = "title";
			var resultActions = mvc
				.perform(
					get("/api/v1/posts?page=%d&pageSize=%d&keywordType=%s&keyword=%s"
						.formatted(page, pageSize, keywordType, keyword))
				)
				.andDo(print());

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("getItems"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
				.andExpect(jsonPath("$.data.content").doesNotExist())
				.andExpect(jsonPath("$.data.items.length()").value(pageSize)) // 한 페이지당 보여줄 글 개수
				.andExpect(jsonPath("$.data.currentPageNo").value(page)) // 현재 페이지
				.andExpect(jsonPath("$.data.totalPages").value(3)) // 전체 페이지 개수
				.andExpect(jsonPath("$.data.totalItems").value(7));

			var searchedPosts = postService.getListedItems(page, pageSize, keywordType, keyword).getContent();
			checkPosts(resultActions, searchedPosts);
		}

		@Test
		@DisplayName("성공 - 내용으로 글을 검색할 수 있으며 결과는 페이징되어야 한다")
		void itemsC_searchPostsByContent() throws Exception {
			var page = 1;
			var pageSize = 3;
			var keywordType = "content";
			var keyword = "content";
			var resultActions = mvc
				.perform(
					get("/api/v1/posts?page=%d&pageSize=%d&keywordType=%s&keyword=%s"
						.formatted(page, pageSize, keywordType, keyword))
				)
				.andDo(print());

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("getItems"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
				.andExpect(jsonPath("$.data.content").doesNotExist())
				.andExpect(jsonPath("$.data.items.length()").value(pageSize)) // 한 페이지당 보여줄 글 개수
				.andExpect(jsonPath("$.data.currentPageNo").value(page)) // 현재 페이지
				.andExpect(jsonPath("$.data.totalPages").value(3)) // 전체 페이지 개수
				.andExpect(jsonPath("$.data.totalItems").value(7));

			var searchedPosts = postService.getListedItems(page, pageSize, keywordType, keyword).getContent();
			checkPosts(resultActions, searchedPosts);
		}

		@Test
		@DisplayName("성공 - 내가 작성한 글 목록을 조회할 수 있으며 결과는 페이징되어야 한다")
		void itemsD_myPosts() throws Exception {
			var page = 1;
			var pageSize = 3;
			var keywordType = "content";
			var keyword = "content";
			var resultActions = mvc
				.perform(
					get("/api/v1/posts/mine?page=%d&pageSize=%d&keywordType=%s&keyword=%s"
						.formatted(page, pageSize, keywordType, keyword))
						.header("Authorization", "Bearer " + token)
				)
				.andDo(print());

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("getMines"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
				.andExpect(jsonPath("$.data.content").doesNotExist())
				.andExpect(jsonPath("$.data.items.length()").value(pageSize)) // 한 페이지당 보여줄 글 개수
				.andExpect(jsonPath("$.data.currentPageNo").value(page)) // 현재 페이지
				.andExpect(jsonPath("$.data.totalPages").value(2)) // 전체 페이지 개수
				.andExpect(jsonPath("$.data.totalItems").value(5));
		}
	}

	@Nested
	@DisplayName("글 단건 조회")
	class getItem {

		@Test
		@DisplayName("성공 - 다른 유저의 공개글 단건 조회를 할 수 있다")
		void itemA() throws Exception {
			var apiKey = "";
			var postId = 2L;
			var resultActions = itemRequest(apiKey, postId);
			var post = postService.getItem(postId).get();

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("getItem"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("%d번 글을 조회하였습니다.".formatted(postId)));
			checkPost(resultActions, post);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 글을 조회하면 실패한다")
		void itemB() throws Exception {
			var apiKey = "user1";
			var postId = 9999999L;
			var resultActions = itemRequest(apiKey, postId);

			resultActions
				.andExpect(status().isNotFound())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("getItem"))
				.andExpect(jsonPath("$.code").value("404-1"))
				.andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."));
		}

		@Test
		@DisplayName("실패 - 다른 유저의 비공개 글을 조회하면 실패한다")
		void itemC() throws Exception {
			var postId = 1L;
			var resultActions = itemRequest(token, postId);

			resultActions
				.andExpect(status().isForbidden())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("getItem"))
				.andExpect(jsonPath("$.code").value("403-1"))
				.andExpect(jsonPath("$.msg").value("비공개 설정된 글입니다."));
		}

		private ResultActions itemRequest(String apiKey, long postId) throws Exception {
			return mvc
				.perform(
					get("/api/v1/posts/%s".formatted(postId))
						.header("Authorization", "Bearer %s".formatted(apiKey))
				)
				.andDo(print());
		}
	}

	private void checkPost(ResultActions resultActions, Post post) throws Exception {
		resultActions
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.id").value(post.getId()))
			.andExpect(jsonPath("$.data.title").value(post.getTitle()))
			.andExpect(jsonPath("$.data.content").value(post.getContent()))
			.andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
			.andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getNickname()))
			.andExpect(jsonPath("$.data.published").value(post.isPublished()))
			.andExpect(jsonPath("$.data.listed").value(post.isListed()))
			.andExpect(jsonPath("$.data.createdDate").value(
				matchesPattern(post.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
			.andExpect(jsonPath("$.data.modifiedDate").value(
				matchesPattern(post.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
	}

	@Nested
	@DisplayName("글 작성")
	class write {

		@Test
		@DisplayName("성공 - 글을 작성할 수 있다")
			// @WithUserDetails("user2") // UserDetailsService 인터페이스를 구현해 시큐리티는 우리가 구현한 Member의 user를 인식할 수 있다
		void writeA() throws Exception {
			var title = "새로운 글 제목";
			var content = "새로운 글 내용";
			var resultActions = writeRequest(token, title, content);
			var post = postService.getLatestItem().get();

			resultActions
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("201-1"))
				.andExpect(jsonPath("$.msg").value("%d번 글 작성이 완료되었습니다.".formatted(post.getId())));
			checkPost(resultActions, post);
		}

		private ResultActions writeRequest(String token, String title, String content) throws Exception {
			return mvc
				.perform(
					post("/api/v1/posts")
						.header("Authorization", "Bearer %s".formatted(token))
						.content("""
							{
								"title" : "%s",
								"content" : "%s",
								"published" : true,
								"listed" : true
							}
							""".formatted(title, content).stripIndent())
						.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
				)
				.andDo(print());
		}

		@Test
		@DisplayName("실패 - 잘못된 API key로 글을 작성하면 실패한다")
		void writeB() throws Exception {
			var wrong_token = "";
			var title = "새로운 글 제목";
			var content = "새로운 글 내용";
			var resultActions = writeRequest(wrong_token, title, content);

			resultActions
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("401-1"))
				.andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
		}

		@Test
		@DisplayName("실패 - 입력 데이터가 누락되면 글 작성에 실패한다")
		void writeC() throws Exception {
			var title = "";
			var content = "";
			var resultActions = writeRequest(token, title, content);

			resultActions
				.andExpect(status().isBadRequest())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("write"))
				.andExpect(jsonPath("$.code").value("400-1"))
				.andExpect(jsonPath("$.msg").value("""
					content : NotBlank : must not be blank
					title : NotBlank : must not be blank
					""".trim().stripIndent()));
		}
	}

	@Nested
	@DisplayName("글 수정")
	class modify {

		@Test
		@DisplayName("성공 - 글을 수정할 수 있다")
		void modifyA() throws Exception {
			var postId = 3L;
			var title = "수정된 글 제목";
			var content = "수정된 글 내용";
			var resultActions = modifyRequest(postId, token, title, content);

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("modify"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("%d번 글 수정이 완료되었습니다.".formatted(postId)));
			var post = postService.getItem(postId).get();
			checkPost(resultActions, post);
		}

		private ResultActions modifyRequest(Long postId, String token, String title, String content) throws Exception {
			return mvc
				.perform(
					put("/api/v1/posts/%d".formatted(postId))
						.header("Authorization", "Bearer %s".formatted(token))
						.content("""
							{
								"title" : "%s",
								"content" : "%s"
							}
							""".formatted(title, content).stripIndent())
						.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
				)
				.andDo(print());
		}

		@Test
		@DisplayName("실패 - 잘못된 API key로 글을 수정하면 실패한다")
		void modifyB() throws Exception {
			var postId = 1L;
			var token = "wrong_api_key";
			var title = "수정된 글 제목";
			var content = "수정된 글 내용";
			var resultActions = modifyRequest(postId, token, title, content);

			resultActions
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("401-1"))
				.andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
		}

		@Test
		@DisplayName("실패 - 입력 데이터가 누락되면 글 수정에 실패한다")
		void modifyC() throws Exception {
			var postId = 1L;
			var title = "";
			var content = "";
			var resultActions = modifyRequest(postId, token, title, content);

			resultActions
				.andExpect(status().isBadRequest())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("modify"))
				.andExpect(jsonPath("$.code").value("400-1"))
				.andExpect(jsonPath("$.msg").value("""
					content : NotBlank : must not be blank
					title : NotBlank : must not be blank
					""".trim().stripIndent()));
		}

		@Test
		@DisplayName("실패 - 자신이 작성하지 않은 글을 수정할 수 없다")
		void modifyD() throws Exception {
			var postId = 1L;
			var title = "다른 유저의 글 제목 수정";
			var content = "다른 유저의 글 내용 수정";
			var resultActions = modifyRequest(postId, token, title, content);

			resultActions
				.andExpect(status().isForbidden())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("modify"))
				.andExpect(jsonPath("$.code").value("403-1"))
				.andExpect(jsonPath("$.msg").value("자신이 작성한 글만 수정 가능합니다."));
		}
	}

	@Nested
	@DisplayName("글 삭제")
	class delete {

		@Test
		@DisplayName("성공 - 글을 삭제할 수 있다")
		void deleteA() throws Exception {
			var postId = 3L;
			var resultActions = deleteRequest(postId, token);

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("delete"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("%d번 글 삭제가 완료되었습니다.".formatted(postId)));
		}

		private ResultActions deleteRequest(long postId, String apiKey) throws Exception {
			return mvc
				.perform(
					delete("/api/v1/posts/%d".formatted(postId))
						.header("Authorization", "Bearer %s".formatted(token))
				)
				.andDo(print());
		}

		@Test
		@DisplayName("실패 - 자신이 작성하지 않은 글을 삭제할 수 없다")
		void deleteB() throws Exception {
			var postId = 1L;
			var token = "user2";
			var resultActions = deleteRequest(postId, token);

			resultActions
				.andExpect(status().isForbidden())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("delete"))
				.andExpect(jsonPath("$.code").value("403-1"))
				.andExpect(jsonPath("$.msg").value("자신이 작성한 글만 삭제 가능합니다."));
		}
	}

	@Nested
	@DisplayName("통계")
	class statistics {

		@Test
		@WithUserDetails("admin")
		@DisplayName("성공 - 관리자는 통계 페이지에 접근할 수 있다")
		void statisticsA() throws Exception {
			var resultActions = mvc.perform(
					get("/api/v1/posts/statistics")
				)
				.andDo(print());

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("getStatistics"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("통계 조회가 완료되었습니다."))
				.andExpect(jsonPath("$.data.postCount").value(10))
				.andExpect(jsonPath("$.data.postPublishedCount").value(10))
				.andExpect(jsonPath("$.data.postListedCount").value(10));
		}

		@Test
		@WithUserDetails("user1")
		@DisplayName("실패 - 사용자는 통계 페이지에 접근할 수 없어야 한다")
		void statisticsB() throws Exception {
			var resultActions = mvc.perform(
					get("/api/v1/posts/statistics")
				)
				.andDo(print());

			resultActions
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("403-1"))
				.andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
		}
	}
}
