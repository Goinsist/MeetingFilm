package com.stylefeng.guns.api.cinema.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CinemaWithFilmVO implements Serializable {
    String cinemaId;
    String cinemaName;
    String filmMinPrice;
    String cinemaAddress;
    String filmRound;
  

}
