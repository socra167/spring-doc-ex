package com.springdocex.domain.post.post.dto;

import java.time.LocalDateTime;

import com.springdocex.domain.post.post.entity.Post;

import lombok.Getter;

@Getter
public class PostWithContnetDto {
	private long id;
	private LocalDateTime createdDate;
	private LocalDateTime modifiedDate;
	private String title;
	private String content;
	private long authorId;
	private String authorName;
	private boolean published;
	private boolean listed;

	public PostWithContnetDto(Post post) {
		this.id = post.getId();
		this.createdDate = post.getCreatedDate();
		this.modifiedDate = post.getModifiedDate();
		this.title = post.getTitle();
		this.content = post.getContent();
		this.authorId = post.getAuthor().getId();
		this.authorName = post.getAuthor().getNickname();
		this.published = post.isPublished();
		this.listed = post.isListed();
	}
}
