package com.stylefeng.guns.rest.modular.admin.film;

import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.vo.FilmDetailVO;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/film")
public class AdminFilmController {
    @Reference(interfaceClass = FilmServiceApi.class,check = false)
    private FilmServiceApi filmServiceApi;

    @RequestMapping(value = "getFilmInfo")
    public ResponseVO  getFilmInfo(){
       List<Integer> allFilmId= filmServiceApi.getAllFilmId();
       List<FilmDetailVO> filmDetailVOS=new ArrayList<>();
     return null;
    }
    @RequestMapping(value = "addFilm")
    public ResponseVO addFilm(){

        return null;
    }
}
