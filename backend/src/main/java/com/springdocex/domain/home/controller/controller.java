package com.springdocex.domain.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class controller {

	@GetMapping("/")
	@ResponseBody
	public String welcome() {
		return "API 서버에 오신 것을 환영합니다.";
	}
}
