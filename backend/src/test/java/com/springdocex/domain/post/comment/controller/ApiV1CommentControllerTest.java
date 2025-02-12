package com.springdocex.domain.post.comment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.springdocex.domain.member.member.entity.Member;
import com.springdocex.domain.member.member.service.MemberService;
import com.springdocex.domain.post.post.service.PostService;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApiV1CommentControllerTest {
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
		loginMember = memberService.findByUsername("user1").get();
		token = memberService.getAuthToken(loginMember);
	}

	@Test
	@DisplayName("댓글을 작성할 수 있다")
	void write() throws Exception {
		var content = "댓글의 내용입니다.";
		var postId = 1L;
		var resultActions = mvc
			.perform(
				post("/api/v1/posts/%d/comments".formatted(postId))
					.header("Authorization", "Bearer " + token)
					.content("""
						{
							"content" : "%s"
						}
						""".formatted(content).trim().stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		var post = postService.getItem(postId).get();
		var comment = post.getLatestComment();
		resultActions
			.andExpect(status().isCreated())
			.andExpect(handler().handlerType(ApiV1CommentController.class))
			.andExpect(handler().methodName("write"))
			.andExpect(jsonPath("$.code").value("201-1"))
			.andExpect(jsonPath("$.msg").value("%d번 댓글 작성이 완료되었습니다.".formatted(comment.getId())));
	}

	@Test
	@DisplayName("댓글을 수정할 수 있다")
	void modify() throws Exception {
		var content = "수정된 댓글 내용";
		var postId = 1L;
		var commentId = 1L;
		var resultActions = mvc
			.perform(
				put("/api/v1/posts/%d/comments/%d".formatted(postId, commentId))
					.header("Authorization", "Bearer " + token)
					.content("""
						{
							"content" : "%s"
						}
						""".formatted(content).trim().stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		var post = postService.getItem(postId).get();
		var comment = post.getCommentById(commentId);

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(ApiV1CommentController.class))
			.andExpect(handler().methodName("modify"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.msg").value("%d번 댓글 수정이 완료되었습니다.".formatted(comment.getId())));
	}

	@Test
	@DisplayName("댓글을 삭제할 수 있다")
	void delete() throws Exception {
		var postId = 1L;
		var commentId = 1L;
		var resultActions = mvc
			.perform(
				MockMvcRequestBuilders.delete("/api/v1/posts/%d/comments/%d".formatted(postId, commentId))
					.header("Authorization", "Bearer " + token)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(ApiV1CommentController.class))
			.andExpect(handler().methodName("delete"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.msg").value("%d번 댓글 삭제가 완료되었습니다.".formatted(commentId)));
	}

	@Test
	@DisplayName("글에 대한 댓글을 모두 조회할 수 있다")
	void items() throws Exception {
		var postId = 1L;
		var resultActions = mvc
			.perform(
				get("/api/v1/posts/%d/comments".formatted(postId))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(ApiV1CommentController.class))
			.andExpect(handler().methodName("getItems"))
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[1].id").value(2));
	}
}