package com.stylefeng.guns.rest.modular.film.vo;

import com.stylefeng.guns.api.film.vo.CatVO;
import com.stylefeng.guns.api.film.vo.SourceVO;
import com.stylefeng.guns.api.film.vo.TypeVO;
import com.stylefeng.guns.api.film.vo.YearVO;
import lombok.Data;

import java.util.List;
@Data
public class OptionValueVO {
    List<CatVO> filmCats;
    List<TypeVO> filmTypes;
    List<SourceVO> filmSources;
    List<YearVO> filmYears;
    List<String> actors;

}
