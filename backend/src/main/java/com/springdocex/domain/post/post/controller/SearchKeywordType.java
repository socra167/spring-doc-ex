package com.springdocex.domain.post.post.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SearchKeywordType {
	TITLE("title"),
	CONTENT("content");

	public final String value;
}
