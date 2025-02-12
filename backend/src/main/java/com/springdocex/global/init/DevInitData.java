package com.springdocex.global.init;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
			genApiJsonFile("http://localhost:8080/v3/api-docs/apiV1", "apiV1.json");
		};
	}

	public void genApiJsonFile(String url, String filename) {
		Path filePath = Path.of(filename); // 저장할 파일명
		// HttpClient 생성
		HttpClient client = HttpClient.newHttpClient();
		// HTTP 요청 생성
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.GET()
			.build();
		try {
			// 요청 보내고 응답 받기
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			// 응답 상태 코드 확인
			if (response.statusCode() == 200) {
				// JSON 데이터를 파일로 저장
				Files.writeString(filePath, response.body(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				System.out.println("JSON 데이터가 " + filePath.toAbsolutePath() + "에 저장되었습니다.");
			} else {
				System.err.println("오류: HTTP 상태 코드 " + response.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
