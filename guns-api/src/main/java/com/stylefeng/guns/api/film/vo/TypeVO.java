package com.stylefeng.guns.api.film.vo;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;

import java.io.Serializable;

@Data
public class TypeVO implements Serializable {
    private String typeId;
    private String typeName;
    private Bool active;
}
