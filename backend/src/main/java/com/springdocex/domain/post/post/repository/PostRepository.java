package com.springdocex.domain.post.post.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import com.springdocex.domain.member.member.entity.Member;
import com.springdocex.domain.post.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	Optional<Post> findTopByOrderByIdDesc();

	Page<Post> findByListedAndTitleLike(boolean listed, String title, PageRequest pageRequest);

	Page<Post> findByListedAndContentLike(boolean listed, String content, PageRequest pageRequest);

	Page<Post> findByAuthorAndContentLike(Member author, String likeKeyword, PageRequest pageRequest);

	Page<Post> findByAuthorAndTitleLike(Member author, String likeKeyword, PageRequest pageRequest);
}
