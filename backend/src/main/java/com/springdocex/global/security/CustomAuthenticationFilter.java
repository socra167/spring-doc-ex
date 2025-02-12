package com.springdocex.global.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.springdocex.domain.member.member.entity.Member;
import com.springdocex.domain.member.member.service.MemberService;
import com.springdocex.global.Rq;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component // 컴포넌트 스캔 적용
public class CustomAuthenticationFilter extends OncePerRequestFilter {
	public static final String AUTHORIZATION = "Authorization";
	public static final String ACCESS_TOKEN = "accessToken";
	public static final String BEARER = "Bearer ";
	public static final String API_KEY = "apiKey";
	private final Rq rq;
	private final MemberService memberService;

	private boolean isAuthorizationHeader() {
		return Optional.ofNullable(rq.getHeader(AUTHORIZATION))
			.filter(header -> header.startsWith(BEARER))
			.isPresent();
	}

	record AuthToken(String apiKey, String accessToken) {
	}

	private AuthToken getAuthTokenFromRequest() {

		if (isAuthorizationHeader()) {

			String authorizationHeader = rq.getHeader(AUTHORIZATION);
			String authToken = authorizationHeader.substring(BEARER.length());

			String[] tokenBits = authToken.split(" ", 2);

			if (tokenBits.length < 2) {
				return null;
			}

			return new AuthToken(tokenBits[0], tokenBits[1]);
		}

		String accessToken = rq.getValueFromCookie(ACCESS_TOKEN);
		String apiKey = rq.getValueFromCookie(API_KEY);

		if (accessToken == null || apiKey == null) {
			return null;
		}

		return new AuthToken(apiKey, accessToken);

	}

	private Member getMemberByAccessToken(String accessToken, String apiKey) {

		Optional<Member> opMemberByAccessToken = memberService.getMemberByAccessToken(accessToken);

		if (opMemberByAccessToken.isPresent()) {
			return opMemberByAccessToken.get();
		}

		Optional<Member> opMemberByApiKey = memberService.findByApiKey(apiKey);

		if (opMemberByApiKey.isEmpty()) {
			return null;
		}

		String newAccessToken = memberService.genAccessToken(opMemberByApiKey.get());
		rq.addCookie(ACCESS_TOKEN, newAccessToken);
		rq.addCookie(API_KEY, apiKey);

		return opMemberByApiKey.get();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		// 인증이 불필요한 endpoint 필터 처리 통과
		String url = request.getRequestURI();
		if (List.of("api/v1/members/login", "api/v1/members/join", "api/v1/members/logout").contains(url)) {
			filterChain.doFilter(request, response);
			return;
		}

		AuthToken tokens = getAuthTokenFromRequest();

		if (tokens == null) {
			filterChain.doFilter(request, response);
			return;
		}

		String apiKey = tokens.apiKey();
		String accessToken = tokens.accessToken();

		Member actor = getMemberByAccessToken(accessToken, apiKey);

		if (actor == null) {
			filterChain.doFilter(request, response);
			return;
		}

		rq.setLogin(actor);
		filterChain.doFilter(request, response);
	}
}
