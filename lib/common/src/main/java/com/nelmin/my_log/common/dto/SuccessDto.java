package com.nelmin.my_log.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SuccessDto extends HasError {
    private Boolean success;
}
