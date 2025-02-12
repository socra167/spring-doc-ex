package com.springdocex.domain.member.member.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.springdocex.domain.member.member.entity.Member;
import com.springdocex.standard.Ut;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthTokenServiceTest {
	@Autowired
	private AuthTokenService authTokenService;
	@Autowired
	private MemberService memberService;

	@Value("${custom.jwt.secret-key}") // application.yml 값
	private String keyString;

	@Value("${custom.jwt.expire-seconds}")
	private int expireSeconds;

	@Test
	@DisplayName("AuthTokenService 생성")
	void init() {
		assertThat(authTokenService).isNotNull();
	}

	@Test
	@DisplayName("JWT 생성")
	void createToken() {
		// 토큰 만료기간 : 1년
		Map<String, Object> originPayload = Map.of("name", "john", "age", 23);
		String jwtStr = Ut.Jwt.createToken(keyString, expireSeconds, originPayload);
		assertThat(jwtStr).isNotBlank();

		// 파싱하는 과정에서, JWT가 누구에게나 공개되어 있어 위변조가 있을 수 있다. 또 유효기간이 지나면 JWT는 동작하면 안된다.
		// -> parse() 과정을 살펴보자 - ExpiredJwtException, MalformedJwtException, SignatureException, SecurityException, IllegalArgumentException ...
		// 메서드 안에 이미 구현되어 있다. 잘못된 JWT가 들어오면 이미 데이터를 꺼내오기 전에 예외가 발생한다
		Map<String, Object> parsedPayload = Ut.Jwt.getPayload(keyString, jwtStr);
		assertThat(parsedPayload).containsAllEntriesOf(originPayload); // issuedAt, expiration가 추가되어서 완전히 일치하진 않는다
	}

	@Test
	@DisplayName("user1의 Access Token 생성")
	void accessToken() {
		// Access Token이라고 불리는 JWT (뭔가를 접근하기 위한 토큰, 인증 정보를 담고 있는 토큰)
		Member member = memberService.findByUsername("user1").get();
		String accessToken = authTokenService.genAccessToken(member); // 테스트 패키지 구조를 동일하게 가져가면 protected 메서드 사용 가능

		assertThat(accessToken).isNotBlank();
		System.out.println("accessToken = " + accessToken);
	}

	@Test
	@DisplayName("JWT valid check")
	void checkValid() {
		Member member = memberService.findByUsername("user1").get();
		String accessToken = authTokenService.genAccessToken(member);
		boolean isValid = Ut.Jwt.isValidToken(keyString, accessToken);
		assertThat(isValid).isTrue();

		// Map<String, Object> parsedPayload = Ut.Jwt.getPayload(secretKey, accessToken);
		Map<String, Object> parsedPayload = authTokenService.getPayload(accessToken);

		// JSON은 Long을 표현할 수 없다. -> JSON은 Long을 double로 표현한다.
		// 직렬화 -> 역직렬화 과정에서 Long 타입을 잃어버리고, double로 되었다가 기본 int로 돌아온 것
		assertThat(parsedPayload).containsAllEntriesOf(
			Map.of("id", member.getId(), "username", member.getUsername())
		);
	}
}
