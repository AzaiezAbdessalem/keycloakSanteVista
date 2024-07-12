package com.mskeycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessDto {

    private Long id;
    private String name;
    private String code;
    private String type;
    private String path;

    private List<AccessDto> subAccess = new ArrayList<>();

}
