package com.springdocex.domain.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "HomeController", description = "API 서버 홈")
@Controller
public class controller {

	@Operation(summary = "API 서버 시작페이지", description = "API 서버 시작 페이지입니다.")
	@GetMapping("/")
	@ResponseBody
	public String welcome() {
		return "API 서버에 오신 것을 환영합니다.";
	}
}
