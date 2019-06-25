package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class ActorNameAndRoleNameVO implements Serializable {
    private Integer actorId;
    private String actorName;
    private String roleName;
}
