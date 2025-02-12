package com.springdocex.domain.member.member.controller;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.springdocex.domain.member.member.entity.Member;
import com.springdocex.domain.member.member.service.MemberService;

import jakarta.servlet.http.Cookie;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc // 내부적으로 네트워크를 사용하지 않고, 네트워크를 사용하는 것처럼 보이게 테스트를 진행한다
class Ap1V1MemberControllerTest {
	@Autowired
	private MockMvc mvc; // 컨트롤러를 테스트하기 위해 사용한다. MockMvc로 내부적으로 서버처럼 보이게 사용할 수 있다

	@Autowired
	private MemberService memberService;

	private Member loginMember;
	private String token;

	@BeforeEach
	void setUp() {
		loginMember = memberService.findByUsername("user1").get();
		token = memberService.getAuthToken(loginMember);
	}

	@Nested
	@DisplayName("회원 가입")
	class join {

		@Test
		@DisplayName("성공 - 회원 가입을 할 수 있다")
		void joinA() throws Exception {
			var username = "usernew";
			var password = "1234";
			var nickname = "무명";
			var resultActions = joinRequest(username, password, nickname);

			Member member = memberService.findByUsername(username).get();
			assertThat(member.getNickname()).isEqualTo(nickname);
			resultActions
				.andExpect(status().isCreated()) // Expected: 201 CREATED
				.andExpect(handler().handlerType(
					ApiV1MemberController.class)) // Endpoint를 처리하는 Controller: ApiV1MemberController.class
				.andExpect(handler().methodName("join")) // Endpoint를 처리하는 메서드명: "join"
				.andExpect(jsonPath("$.code").value("201-1")) // 결과 body의 JSON 데이터를 검증
				.andExpect(jsonPath("$.msg").value("회원 가입이 완료되었습니다."));
			checkMember(resultActions, member);
		}

		@Test
		@DisplayName("실패 - 이미 존재하는 아이디로 회원 가입을 하면 실패한다")
		void joinB() throws Exception {
			var resultActions = joinRequest("user1", "1234", "무명");

			resultActions
				.andExpect(status().isConflict())
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("join"))
				.andExpect(jsonPath("$.code").value("409-1"))
				.andExpect(jsonPath("$.msg").value("이미 사용중인 아이디입니다."));
		}

		@Test
		@DisplayName("실패 - 입력 데이터가 누락되면 회원 가입에 실패한다")
		void joinC() throws Exception {
			var resultActions = joinRequest("", "", "");

			resultActions
				.andExpect(status().isBadRequest())
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("join"))
				.andExpect(jsonPath("$.code").value("400-1"))
				.andExpect(jsonPath("$.msg").value("""
					nickname : NotBlank : must not be blank
					password : NotBlank : must not be blank
					username : NotBlank : must not be blank
					""".trim().stripIndent()));
		}

		private ResultActions joinRequest(String username, String password, String nickname) throws Exception {
			return mvc
				.perform(
					post("/api/v1/members/join")
						.content("""
							{
								"username" : "%s",
								"password" : "%s",
								"nickname" : "%s"
							}
							""".formatted(username, password, nickname).stripIndent())
						.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
				)
				.andDo(print());
		}
	}

	private void checkMember(ResultActions resultActions, Member member) throws Exception {
		resultActions
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.id").value(member.getId()))
			.andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
			.andExpect(jsonPath("$.data.createdDate").value(matchesPattern(member.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
			.andExpect(jsonPath("$.data.modifiedDate").value(matchesPattern(member.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
	}

	@Nested
	@DisplayName("로그인")
	class login {

		@Test
		@DisplayName("성공 - 로그인을 할 수 있다")
		void loginA() throws Exception {
			var username = "user1";
			var password = "user11234";
			var resultActions = loginRequest(username, password);

			// 로그인 성공 시 응답 검증
			Member member = memberService.findByUsername("user1").get();
			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("login"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("%s님 환영합니다.".formatted(member.getNickname())))
				.andExpect(jsonPath("$.data").exists())
				.andExpect(jsonPath("$.data.item.id").value(member.getId()))
				.andExpect(jsonPath("$.data.item.nickname").value(member.getNickname()))
				.andExpect(jsonPath("$.data.item.createdDate").value(matchesPattern(member.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
				.andExpect(jsonPath("$.data.item.modifiedDate").value(matchesPattern(member.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
				.andExpect(jsonPath("$.data.apiKey").value(member.getApiKey()))
				.andExpect(jsonPath("$.data.accessToken").exists());

			// 로그인 성공 시 설정되는 쿠키 검증
			resultActions
				.andExpect(mvcResult -> {
					Cookie cookie = mvcResult.getResponse().getCookie("apiKey");
					assertThat(cookie).isNotNull();
					assertThat(cookie.getName()).isEqualTo("apiKey");
					assertThat(cookie.getValue()).isNotBlank();
					assertThat(cookie.getDomain()).isEqualTo("localhost");
					assertThat(cookie.getPath()).isEqualTo("/");
					assertThat(cookie.isHttpOnly()).isTrue();
					assertThat(cookie.getSecure()).isTrue();

					Cookie accessToken = mvcResult.getResponse().getCookie("accessToken");
					assertThat(accessToken).isNotNull();
					assertThat(accessToken.getName()).isEqualTo("accessToken");
					assertThat(accessToken.getValue()).isNotBlank();
					assertThat(accessToken.getDomain()).isEqualTo("localhost");
					assertThat(accessToken.getPath()).isEqualTo("/");
					assertThat(accessToken.isHttpOnly()).isTrue();
					assertThat(accessToken.getSecure()).isTrue();
				});
		}

		@Test
		@DisplayName("실패 - 비밀번호가 틀리면 로그인에 실패한다")
		void loginB_wrongPassword() throws Exception {
			var username = "user1";
			var password = "1234";
			var resultActions = loginRequest(username, password);

			resultActions
				.andExpect(status().isUnauthorized()) // 401 UNAUTHORIZED
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("login"))
				.andExpect(jsonPath("$.code").value("401-2"))
				.andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."));
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 아이디면 로그인에 실패한다")
		void loginC_wrongUsername() throws Exception {
			var username = "nonexistent";
			var password = "1234";
			var resultActions = loginRequest(username, password);

			resultActions
				.andExpect(status().isUnauthorized()) // 401 UNAUTHORIZED
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("login"))
				.andExpect(jsonPath("$.code").value("401-1"))
				.andExpect(jsonPath("$.msg").value("잘못된 아이디입니다."));
		}

		@Test
		@DisplayName("실패 - 아이디가 비어 있으면 로그인에 실패한다")
		void loginD_emptyUsername() throws Exception {
			var username = "";
			var password = "1234";
			var resultActions = loginRequest(username, password);

			resultActions
				.andExpect(status().isBadRequest()) // 400 BAD REQUEST
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("login"))
				.andExpect(jsonPath("$.code").value("400-1"))
				.andExpect(jsonPath("$.msg").value("username : NotBlank : must not be blank"));
		}

		@Test
		@DisplayName("실패 - 비밀번호가 비어 있으면 로그인에 실패한다")
		void loginD_emptyPassword() throws Exception {
			var username = "user1";
			var password = "";
			var resultActions = loginRequest(username, password);

			resultActions
				.andExpect(status().isBadRequest()) // 400 BAD REQUEST
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("login"))
				.andExpect(jsonPath("$.code").value("400-1"))
				.andExpect(jsonPath("$.msg").value("password : NotBlank : must not be blank"));
		}

		private ResultActions loginRequest(String username, String password) throws Exception {
			return mvc
				.perform(
					post("/api/v1/members/login")
						.content("""
							{
							    "username": "%s",
							    "password": "%s"
							}
							""".formatted(username, password).stripIndent())
						.contentType(
							new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
						)
				)
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("내 정보 조회")
	class me {

		@Test
		@DisplayName("성공 - 내 정보를 조회할 수 있다")
		void meA() throws Exception {
			var resultActions = meRequest(token);

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("me"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("내 정보 조회가 완료되었습니다."));
			checkMember(resultActions, loginMember);
		}

		@Test
		@DisplayName("실패 - 잘못된 API key로 내 정보 조회를 하면 실패한다")
		void meB() throws Exception {
			var wrongToken = "";
			var resultActions = meRequest(wrongToken);

			resultActions
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("401-1"))
				.andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
		}

		private ResultActions meRequest(String token) throws Exception {
			return mvc
				.perform(
					get("/api/v1/members/me")
						.header("Authorization", "Bearer %s".formatted(token))
				)
				.andDo(print());
		}

		@Test
		@DisplayName("성공 - 만료된 액세스 토큰으로 내 정보를 조회할 수 있다")
		void meC() throws Exception {
			var apiKey = loginMember.getApiKey();
			var expiredToken = apiKey + " eyJhbGciOiJIUzUxMiJ9.eyJpZCI6MywidXNlcm5hbWUiOiJ1c2VyMSIsImlhdCI6MTczOTI0MDc0OSwiZXhwIjoxNzM5MjQwNzU0fQ.3NimuNYStViWCejkUoP-TsLKDapoxoydxDRGHE0Wc_GlgBADUO75AJTHS7FLJ3WmC9IHZa1lUOn-B7SI1Bok5g";

			ResultActions resultActions = meRequest(expiredToken); // io.jsonwebtoken.ExpiredJwtException: JWT expired

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("me"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("내 정보 조회가 완료되었습니다."));
		}

		@Test
		@DisplayName("성공 - 잘못된 액세스 토큰으로 내 정보 조회")
		void meD() throws Exception {
			var apiKey = loginMember.getApiKey();
			var wrongToken = apiKey + " 123";

			ResultActions resultActions = meRequest(wrongToken); // io.jsonwebtoken.MalformedJwtException: Invalid compact JWT string

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("me"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("내 정보 조회가 완료되었습니다."));
		}
	}

	@Nested
	@DisplayName("로그아웃")
	class logout {

		@Test
		@DisplayName("성공 - 로그아웃할 수 있다")
		void logout() throws Exception {
			var resultActions = mvc.perform(
				delete("/api/v1/members/logout")
			);

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1MemberController.class))
				.andExpect(handler().methodName("logout"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("로그아웃 되었습니다."));

			resultActions
				.andExpect(
					mvcResult -> {
						Cookie apiKey = mvcResult.getResponse().getCookie("apiKey");
						assertThat(apiKey).isNotNull();
						assertThat(apiKey.getMaxAge()).isZero();

						Cookie accessToken = mvcResult.getResponse().getCookie("accessToken");
						assertThat(accessToken).isNotNull();
						assertThat(accessToken.getMaxAge()).isZero();
					}
				);
		}
	}
}
