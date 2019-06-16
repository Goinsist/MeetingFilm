package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class FilmDetailVO implements Serializable {
    private String filmId;
private String filmPreSaleNum;
    private String filmType;
    private String filmYear;
  private String filmName;
private String filmSource;
private String filmCat;
    private String filmEnName;
    private String imgAddress;
    private String score;
    private String socreNum;
    private String totalBox;
    private String info01;
    private String info02;
    private String info03;
    private InfoRequestVO info04;
    private String status;

}
