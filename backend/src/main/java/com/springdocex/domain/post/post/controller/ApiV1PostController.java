package com.springdocex.domain.post.post.controller;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springdocex.domain.member.member.entity.Member;
import com.springdocex.domain.post.post.dto.PageDto;
import com.springdocex.domain.post.post.dto.PostWithContnetDto;
import com.springdocex.domain.post.post.entity.Post;
import com.springdocex.domain.post.post.service.PostService;
import com.springdocex.global.Rq;
import com.springdocex.global.dto.RsData;
import com.springdocex.global.exception.ServiceException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
	private final PostService postService;
	private final Rq rq;

	@GetMapping
	@Transactional(readOnly = true)
	public RsData<PageDto> getItems(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "3") int pageSize,
		@RequestParam(defaultValue = "title") String keywordType,
		@RequestParam(defaultValue = "") String keyword) {
		Page<Post> postPage = postService.getListedItems(page, pageSize, keywordType, keyword);

		return new RsData<>(
			"200-1",
			"글 목록 조회가 완료되었습니다.",
			new PageDto(postPage)
		);
	}

	@GetMapping("{id}")
	@Transactional(readOnly = true)
	public RsData<PostWithContnetDto> getItem(@PathVariable long id) {
		Post post = postService.getItem(id)
			.orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 글입니다."));

		if (!post.isPublished()) {
			Member actor = rq.getActor();
			post.canRead(actor);
		}

		return new RsData<>(
			"200-1",
			"%d번 글을 조회하였습니다.".formatted(post.getId()),
			new PostWithContnetDto(post)
		);
	}

	record WriteReqBody(@NotBlank String title, @NotBlank String content, boolean published, boolean listed) {
	}

	@PostMapping
	@Transactional
	public RsData<PostWithContnetDto> write(@RequestBody @Valid WriteReqBody body) {
		Member actor = rq.getActor();
		Member realActor = rq.getRealActor(actor);

		Post post = postService.write(realActor, body.title(), body.content(), body.published(), body.listed());

		return new RsData<>(
			"201-1",
			"%d번 글 작성이 완료되었습니다.".formatted(post.getId()),
			new PostWithContnetDto(post)
		);
	}

	@PutMapping("{id}")
	@Transactional
	public RsData<PostWithContnetDto> modify(@PathVariable long id, @RequestBody @Valid WriteReqBody body) {
		Member actor = rq.getActor();
		Post post = postService.getItem(id)
			.orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 글입니다."));

		postService.modify(post, body.title(), body.content());

		post.canModify(actor);

		return new RsData<>(
			"200-1",
			"%d번 글 수정이 완료되었습니다.".formatted(post.getId()),
			new PostWithContnetDto(post)
		);
	}

	@DeleteMapping("{id}")
	@Transactional
	public RsData<Void> delete(@PathVariable long id) {
		Member actor = rq.getActor();
		Post post = postService.getItem(id).get();

		post.canDelete(actor);

		postService.delete(post);

		return new RsData<>(
			"200-1",
			"%d번 글 삭제가 완료되었습니다.".formatted(post.getId())
		);
	}

	@GetMapping("/mine")
	@Transactional(readOnly = true)
	public RsData<PageDto> getMines(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "3") int pageSize,
		@RequestParam(defaultValue = "title") String keywordType,
		@RequestParam(defaultValue = "") String keyword
	) {
		Member actor = rq.getActor();
		Page<Post> postPage = postService.getMines(page, pageSize, actor, keywordType, keyword);

		return new RsData<>(
			"200-1",
			"글 목록 조회가 완료되었습니다.",
			new PageDto(postPage)
		);
	}

	record StatisticsResBody(long postCount, long postPublishedCount, long postListedCount) {
	}

	@GetMapping("/statistics")
	public RsData<StatisticsResBody> getStatistics() {
		Member actor = rq.getActor();

		if (!actor.isAdmin()) {
			throw new ServiceException("403-1", "접근 권한이 없습니다.");
		}

		return new RsData<>(
			"200-1",
			"통계 조회가 완료되었습니다.",
			new StatisticsResBody(
				10,
				10,
				10
			)
		);
	}
}
