package com.stylefeng.guns.api.cinema.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FilmFieldVO implements Serializable {
    private List<ShowDate> showDates;
    private String showDate;

}
