package com.springdocex.global;

import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.springdocex.domain.member.member.entity.Member;
import com.springdocex.domain.member.member.service.MemberService;
import com.springdocex.global.exception.ServiceException;
import com.springdocex.global.security.SecurityUser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// Request, Response, Session, Cookie, Header
@Component
@RequiredArgsConstructor
@RequestScope
public class Rq {

	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final MemberService memberService;

	public Member getAuthenticatedActor() {

		String authorizationValue = request.getHeader("Authorization");
		String apiKey = authorizationValue.substring("Bearer ".length());
		Optional<Member> opActor = memberService.findByApiKey(apiKey);

		if (opActor.isEmpty()) {
			throw new ServiceException("401-1", "잘못된 인증키입니다.");
		}

		return opActor.get();

	}

	public void setLogin(Member actor) {
		// Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 인증 정보 저장소
		// security는 인증된 사람이 여기 들어 있다고 생각하고 사용한다
		UserDetails user = new SecurityUser(actor.getId(), actor.getUsername(), "", actor.getAutorities());

		// 인증 정보를 수동으로 등록
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
		);
	}

	// 인증 정보를 통해서 회원 정보를 가져오는 메서드
	public Member getActor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 인증 정보
		if (authentication == null) {
			throw new ServiceException("401-2", "로그인이 필요합니다.");
		}

		Object principal = authentication.getPrincipal();
		if (!(principal instanceof SecurityUser)) {
			throw new ServiceException("401-3", "잘못된 인증 정보입니다.");
		}

		SecurityUser user = (SecurityUser)principal;

		return Member.builder()
			.id(user.getId())
			.username(user.getUsername())
			.build();
	}

	public String getValueFromCookie(String name) {
		Cookie[] cookies = request.getCookies();

		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie.getValue();
			}
		}

		return null;
	}

	public void setHeader(String name, String value) {
		response.setHeader(name, value);
	}

	public void addCookie(String name, String value) {
		Cookie cookie = new Cookie(name, value);
		cookie.setDomain("localhost");
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setAttribute("SameSite", "Strict");
		response.addCookie(cookie);
	}

	public String getHeader(String name) {
		return request.getHeader(name);
	}

	public Member getRealActor(Member actor) {
		return memberService.findById(actor.getId()).get();
	}

	public void removeCookie(String name) {
		// 원칙적으로 쿠키를 서버에서 삭제하는 것은 불가능하다.
		Cookie cookie = new Cookie(name, null);
		cookie.setDomain("localhost");
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setAttribute("SameSite", "Strict");
		cookie.setMaxAge(0); // 쿠키의 최대 유효 기간
		response.addCookie(cookie);
	}
}
