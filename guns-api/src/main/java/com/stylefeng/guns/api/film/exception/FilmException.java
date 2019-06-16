package com.stylefeng.guns.api.film.exception;

import com.stylefeng.guns.core.exception.ServiceExceptionEnum;

public class FilmException extends RuntimeException {

    private Integer code;

    private String message;

    public FilmException(ServiceExceptionEnum serviceExceptionEnum) {
        this.code = serviceExceptionEnum.getCode();
        this.message = serviceExceptionEnum.getMessage();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}