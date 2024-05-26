package com.nelmin.blog.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nelmin.blog.common.dto.HasError;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoDto extends HasError {
    private Long id;

    @NotBlank(message = "nullable")
    private String nickname;
    private String email;
    private LocalDateTime registrationDate;
    private Boolean enabled;
}