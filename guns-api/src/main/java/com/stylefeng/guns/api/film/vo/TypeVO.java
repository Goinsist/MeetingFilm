package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TypeVO implements Serializable {
    private String typeId;
    private String typeName;
    private boolean isActive;
}
