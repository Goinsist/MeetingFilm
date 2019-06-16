package com.stylefeng.guns.rest.modular.film.vo;

import com.stylefeng.guns.api.film.vo.CatVO;
import com.stylefeng.guns.api.film.vo.SourceVO;
import com.stylefeng.guns.api.film.vo.TypeVO;
import com.stylefeng.guns.api.film.vo.YearVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class FilmInfoVO {
    private String filmId;
    private String filmPoster;
    private String filmPreSaleNum;
    private String filmScore;
    private String filmName;
    private String directorName;
    private String filmType;
    private String filmYear;
    private String filmSource;
    private String[] filmCat;
    private String[] actors;
    private String filmLength;
    private String biography;
    private String filmStatus;
    List<CatVO> filmCats;
    List<TypeVO> filmTypes;
    List<SourceVO> filmSources;
    List<YearVO> filmYears;
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss ")
    private Date filmTime;
    private Date date1;
    private Date date2;


}
