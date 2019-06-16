package com.stylefeng.guns.rest.common.exception;

import com.stylefeng.guns.core.exception.ServiceExceptionEnum;

public enum FilmExceptionEnum implements ServiceExceptionEnum {
    /*
     *操作mooc_film_t表失败
     */
    MOOC_FILM_T_ERROR(901,"操作mooc_film_t表失败"),

    /*
     *操作mooc_film_info_t表失败
     */
    MOOC_FILM_INFO_T_ERROR(902,"操作mooc_film_info_t表失败"),
    /*
     *操作mooc_actor_t表失败
     */
    MOOC_ACTOR_T_ERROR(903,"操作mooc_actor_t表失败"),
    /*
     *操作mooc_film_actor_t表失败
     */
    MOOC_FILM_ACTOR_T_ERROR(903,"操作mooc_film_actor_t表失败"),



    ;
    FilmExceptionEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private Integer code;

    private String message;

    @Override
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
