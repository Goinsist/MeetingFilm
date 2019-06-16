package com.stylefeng.guns.api.cinema.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CinemaFilmVO implements Serializable {
    private String cinemaId;
    private String cinemaName;
    private String cinemaAddress;
   private List<FilmInfoVO> filmInfos;
}
