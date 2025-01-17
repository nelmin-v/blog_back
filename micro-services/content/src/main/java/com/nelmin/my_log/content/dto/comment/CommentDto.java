package com.nelmin.my_log.content.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private String nickname;
    private Long userId;
    private String content;
    private LocalDateTime date;
    private Long rating;
    private Actions actions;
    private ParentDto parent;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Actions {
        private Boolean canDelete = false;
        private Boolean canVote = false;
        private Boolean canReply = false;
        private Boolean canReport = false;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParentDto {
        private String content;
        private String nickname;
    }
}
