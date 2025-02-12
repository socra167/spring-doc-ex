package com.springdocex.global.init;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev") // dev 모드에서만 실행
@Configuration
public class DevInitData {

	@Bean
	public ApplicationRunner devApplicationRunner() { // 서버를 실행할 때마다 실행
		return args -> {
			System.out.println("dev application runner start");
		};
	}
}
