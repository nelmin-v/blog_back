package com.nelmin.blog.content.service;

import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.dto.SuccessDto;
import com.nelmin.blog.content.dto.ArticleDto;
import com.nelmin.blog.content.dto.BookmarksRequestDto;
import com.nelmin.blog.content.dto.BookmarksResponseDto;
import com.nelmin.blog.content.model.Article;
import com.nelmin.blog.content.model.Bookmark;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.util.ClassUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarksService implements FillInfo<ArticleDto> {
    private final UserInfo userInfo;
    private final Bookmark.Repo bookmarkRepository;
    private final Article.Repo articleRepository;
    private final ActionService actionService;

    @Transactional
    public SuccessDto addToBookmarks(Long articleId) {
        var response = new SuccessDto(false);

        try {
            var userId = userInfo.getCurrentUser().getId();
            var bookmark = bookmarkRepository.findByArticleIdAndUserId(articleId, userId).orElse(new Bookmark());

            if (bookmark.getId() != null) {
                response.reject("alreadyAdded", "article");
            } else {
                bookmark.setArticleId(articleId);
                bookmark.setUserId(userId);
                bookmarkRepository.save(bookmark);
                response.setSuccess(bookmark.getId() != null);
            }
        } catch (Exception ex) {
            log.error("Error save to bookmark", ex);
        }

        return response;
    }

    @Transactional
    public SuccessDto removeFromBookmarks(Long articleId) {
        var response = new SuccessDto(false);
        var userId = userInfo.getCurrentUser().getId();
        var bookmark = bookmarkRepository.findByArticleIdAndUserId(articleId, userId);

        if (bookmark.isEmpty()) {
            response.reject("notFound", "bookmark");
        } else {
            bookmarkRepository.deleteById(bookmark.get().getId());
            response.setSuccess(true);
        }

        return response;
    }

    @Override
    @Transactional
    public void fillInfo(ArticleDto response) {
        var exists = bookmarkRepository.existsByArticleIdAndUserId(response.getId(), userInfo.getCurrentUser().getId());
        response.setIsSaved(exists);
    }

    @Transactional
    public BookmarksResponseDto list(BookmarksRequestDto requestDto) {
        var response = new BookmarksResponseDto();
        String[] sortBy = null;

        if (requestDto.getSortBy() != null &&
                !requestDto.getSortBy().isEmpty()) {
            sortBy = requestDto
                    .getSortBy()
                    .stream()
                    .filter(it -> ClassUtils.hasProperty(Article.class, it))
                    .map(ContentService::camelToSnake)
                    .toArray(String[]::new);
        }

        if (sortBy == null || sortBy.length == 0) {
            sortBy = new String[]{"id"};
        }

        var pageRequest = PageRequest.of(
                requestDto.getPage(),
                requestDto.getMax(),
                Sort.by(
                        requestDto.getDirection(),
                        sortBy
                )
        );

        var dbResponse = articleRepository.findAllInBookmarks(userInfo.getCurrentUser().getId(), pageRequest);

        if (dbResponse.isEmpty()) {
            response.setList(new ArrayList<>());
        } else {
            response.setList(dbResponse
                    .getContent()
                    .stream()
                    .map((it) -> {
                        var res = new ArticleDto(it);
                        res.setIsSaved(true);
                        fillInfo(res);
                        return res;
                    })
                    .toList());
            response.setTotalPages(dbResponse.getTotalPages());
        }

        return response;
    }
}