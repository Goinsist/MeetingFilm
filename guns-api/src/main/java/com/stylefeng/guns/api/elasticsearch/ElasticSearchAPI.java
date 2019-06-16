package com.stylefeng.guns.api.elasticsearch;

import com.stylefeng.guns.api.film.vo.FilmInfo;

import java.util.List;

public interface ElasticSearchAPI {
    //根据输入参数查询电影
  List<FilmInfo> search4film(String param);
}
