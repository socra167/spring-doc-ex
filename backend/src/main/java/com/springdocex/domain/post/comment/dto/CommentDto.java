package com.springdocex.domain.post.comment.dto;

import java.time.LocalDateTime;

import com.springdocex.domain.post.comment.entity.Comment;

import lombok.Getter;

@Getter
public class CommentDto {

    private long id;
    private String content;
    private long postId;
    private long authorId;
    private String authorName;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;

    public CommentDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.postId = comment.getPost().getId();
        this.authorId = comment.getAuthor().getId();
        this.authorName = comment.getAuthor().getNickname();
        this.createdTime = comment.getCreatedDate();
        this.modifiedTime = comment.getModifiedDate();
    }

}
