package com.nelmin.my_log.content.controller;

import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.content.service.FillInfo;
import com.nelmin.my_log.content.dto.common.*;
import com.nelmin.my_log.content.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final BookmarksService bookmarksService;
    private final List<FillInfo<ArticleDto>> fillInfoList;
    private final ActionService actionService;

    @Secured("ROLE_USER")
    @PostMapping("/save")
    public ResponseEntity<CreateContentResponseDto> create(@Valid @RequestBody CreateContentRequestDto dto) {
        CreateContentResponseDto response = contentService.save(dto);
        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @PutMapping("/preview/{id}")
    public ResponseEntity<SuccessDto> changePreview(@Valid @PathVariable Long id, @Valid @RequestBody ChangePreviewRequestDto dto) {
        SuccessDto response = contentService.changePreview(id, dto);
        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DeleteContentResponseDto> delete(@Valid @PathVariable Long id) {
        DeleteContentResponseDto response = contentService.delete(id);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ArticleDto> view(@Valid @PathVariable Long id) {
        ArticleDto response = contentService.get(id);

        if (!response.hasErrors()) {
            fillInfoList.forEach(it -> it.fillContentInfo(response));
        }

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/get-by-link/{link}")
    public ResponseEntity<ArticleDto> getByLink(@Valid @PathVariable String link) {
        ArticleDto response = contentService.get(link);

        if (!response.hasErrors()) {
            fillInfoList.forEach(it -> it.fillContentInfo(response));
        }

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/list")
    public ResponseEntity<ListContentResponseDto> list(@Valid @RequestBody ListContentRequestDto dto) {
        ListContentResponseDto response = contentService.list(dto);

        actionService.fillActions(response);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @PostMapping("/change-status/{id}")
    public ResponseEntity<PublishContentResponseDto> changeStatus(@Valid @PathVariable Long id, @RequestBody @Valid ChangeStatusRequestDto requestDto) {
        PublishContentResponseDto response = contentService.changeStatus(id, requestDto.status());

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @PutMapping("/bookmark/{id}")
    public ResponseEntity<SuccessDto> addToBookmarks(@Valid @PathVariable Long id) {
        var response = bookmarksService.addToBookmarks(id);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/bookmark/{id}")
    public ResponseEntity<SuccessDto> removeFromBookmarks(@Valid @PathVariable Long id) {
        var response = bookmarksService.removeFromBookmarks(id);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
