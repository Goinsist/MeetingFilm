package com.stylefeng.guns.rest.modular.film.vo;

import com.stylefeng.guns.api.film.vo.BannerVo;
import com.stylefeng.guns.api.film.vo.FilmInfo;
import com.stylefeng.guns.api.film.vo.FilmVO;

import lombok.Data;

import java.util.List;

@Data
public class FilmIndexVo {
    private List<BannerVo> banners;
    private FilmVO hotFilms;
    private FilmVO soonFilms;
    private FilmVO classicFilms;
    private List<FilmInfo> boxRanking;
  private List<FilmInfo> expectRanking;
  private List<FilmInfo> top100;

}
