package com.stylefeng.guns.rest.modular.film;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.stylefeng.guns.api.film.FilmAsyncServiceApi;
import com.stylefeng.guns.api.film.FilmServiceApi;

import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.rest.modular.film.vo.FilmConditionVO;
import com.stylefeng.guns.rest.modular.film.vo.FilmIndexVo;
import com.stylefeng.guns.rest.modular.film.vo.FilmRequestVO;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/film/")
public class FilmController {
    private static final String IMG_PRE="http://img.meetingshop.cn/";
    @Reference(interfaceClass = FilmServiceApi.class,check = false)
    private FilmServiceApi filmServiceApi;
    @Reference(interfaceClass = FilmAsyncServiceApi.class,async = true,check = false)
    private FilmAsyncServiceApi filmAsyncServiceApi;
    //获取首页信息接口
    /*
    网关:
    1.功能聚合:[API聚合]
    好处:
    1. 六个接口，一次请求，同一时刻节省了五次http请求
    2.同一个接口对外暴露，降低了前后端分离开发的难度和复杂度
    坏处:
    1.一次获取数据过度，容易出现问题
     */
@RequestMapping(value = "getIndex",method = RequestMethod.GET)
    public ResponseVO getIndex(){
    FilmIndexVo filmIndexVo=new FilmIndexVo();

    //获取banner信息
    filmIndexVo.setBanners(filmServiceApi.getBanners());

    //获取正在热映的电影

filmIndexVo.setHotFilms(filmServiceApi.getHotFilms(true,8,1,1,99,99,99));
    //即将上映的film
filmIndexVo.setSoonFilms(filmServiceApi.getSoonFilms(true,8,1,1,99,99,99));
   //获取经典电影
    filmIndexVo.setClassicFilms(filmServiceApi.getClassicFilms(8,1,1,99,99,99));
    //票房排行榜
filmIndexVo.setBoxRanking(filmServiceApi.getBoxRanking());


    //获取受欢迎的榜单
filmIndexVo.setExpectRanking(filmServiceApi.getExpectRanking());

    //获取前100
    filmIndexVo.setTop100(filmServiceApi.getTop());
    return ResponseVO.success(IMG_PRE,filmIndexVo);
    }

@RequestMapping(value = "getConditionList",method = RequestMethod.GET)
    public ResponseVO getConditionList(@RequestParam(name = "catId",required = false,defaultValue = "99")String catId,
                                       @RequestParam(name = "sourceId",required = false,defaultValue = "99") String sourceId,
                                       @RequestParam(name = "yearId",required = false,defaultValue = "99") String yearId ){
    FilmConditionVO filmConditionVO=new FilmConditionVO();
    //标识位
    boolean flag=false;
         //类型集合
    List<CatVO> cats=filmServiceApi.getCats();
    List<CatVO> catResult=new ArrayList<>();
    CatVO catVO=null;
    for(CatVO cat:cats){
        //判断集合是否存在catId，如果存在将对应的实体变成active状态
        if(cat.getCatId().equals("99")){
            catVO=cat;
            continue;
        }
        if(cat.getCatId().equals(catId)){
            flag=true;
            cat.setActive(true);
        }else {
            cat.setActive(false);
        }
        catResult.add(cat);

    }
    //如果不存在，将默认全部变为Active状态
    if(!flag){
        //将id为99的全部置位true
        catVO.setActive(true);
        catResult.add(catVO);

    }else {
        catVO.setActive(false);
        catResult.add(catVO);
    }

        //片源集合
    flag=false;
    List<SourceVO> sources=filmServiceApi.getSources();
    List<SourceVO> sourceResult=new ArrayList<>();
    SourceVO sourceVO=null;
    for(SourceVO source:sources){
        //判断集合是否存在catId，如果存在将对应的实体变成active状态
        if(source.getSourceId().equals("99")){
            sourceVO=source;
            continue;
        }
        if(source.getSourceId().equals(sourceId)){
            flag=true;
            source.setActive(true);
        }else {
            source.setActive(false);
        }
        sourceResult.add(source);

    }
    //如果不存在，将默认全部变为Active状态
    if(!flag){
        //将id不为99的全部置位true
        sourceVO.setActive(true);
        sourceResult.add(sourceVO);

    }else {
        sourceVO.setActive(false);
        sourceResult.add(sourceVO);
    }
        //年代集合
    flag=false;
    List<YearVO> years=filmServiceApi.getYears();
    List<YearVO> yearResult=new ArrayList<>();
    YearVO yearVO=null;
    for(YearVO year:years){
        //判断集合是否存在catId，如果存在将对应的实体变成active状态
        if(year.getYearId().equals("99")){
            yearVO=year;
            continue;
        }
        if(year.getYearId().equals(yearId)){
            flag=true;
       year.setActive(true);
        }else {
            year.setActive(false);
        }
        yearResult.add(year);

    }
    //如果不存在，将默认全部变为Active状态
    if(!flag){
        //将id为99的全部置位true
        yearVO.setActive(true);
        yearResult.add(yearVO);

    }else {
        yearVO.setActive(false);
        yearResult.add(yearVO);
    }

    filmConditionVO.setCatInfo(catResult);
    filmConditionVO.setSourceInfo(sourceResult);
    filmConditionVO.setYearInfo(yearResult);
    return ResponseVO.success(filmConditionVO);
    }

@RequestMapping(value = "getFilms",method = RequestMethod.GET)
    public ResponseVO getFilms(FilmRequestVO filmRequestVO){
    String img_pre="http://img.meetingshop.cn/";
    FilmVO filmVO=null;
    //根据showType判断影片查询类型
    switch (filmRequestVO.getShowType()){
        case 1:
            filmVO=filmServiceApi.getHotFilms(false,filmRequestVO.getPageSize(),
                    filmRequestVO.getNowPage(),
                    filmRequestVO.getSortId(),
                    filmRequestVO.getSourceId(),
                    filmRequestVO.getYearId(),
                    filmRequestVO.getCatId());
            break;
        case 2:
            filmVO=filmServiceApi.getSoonFilms(false,filmRequestVO.getPageSize(),
                    filmRequestVO.getNowPage(),
                    filmRequestVO.getSortId(),
                    filmRequestVO.getSourceId(),
                    filmRequestVO.getYearId(),
                    filmRequestVO.getCatId());
            break;
        case 3:
            filmVO=filmServiceApi.getClassicFilms(filmRequestVO.getPageSize(),
                    filmRequestVO.getNowPage(),
                    filmRequestVO.getSortId(),
                    filmRequestVO.getSourceId(),
                    filmRequestVO.getYearId(),
                    filmRequestVO.getCatId());
            break;
        default:
            filmVO=filmServiceApi.getHotFilms(false,filmRequestVO.getPageSize(),
                filmRequestVO.getNowPage(),
                filmRequestVO.getSortId(),
                filmRequestVO.getSourceId(),
                filmRequestVO.getYearId(),
                filmRequestVO.getCatId());
            break;

    }
    //根据sortId排序
    //添加各种条件查询
    //判断当前是第几页
    return ResponseVO.success(filmVO.getNowPage(),filmVO.getTotalPage(),img_pre,filmVO.getFilmInfo());
    }

@RequestMapping(value = "films/{searchParam}",method = RequestMethod.GET)
    public ResponseVO films(@PathVariable("searchParam")String searchParam,int searchType) throws ExecutionException, InterruptedException {
//根据searchType，判断查询类型
FilmDetailVO filmDetail=filmServiceApi.getFilmDetail(searchType,searchParam);
if(filmDetail==null){
return ResponseVO.serviceFail("没有可查询的影片");
}else if(filmDetail.getFilmId()==null||filmDetail.getFilmId().trim().length()==0){
    return ResponseVO.serviceFail("没有可查询的影片");
}
String filmId=filmDetail.getFilmId();
    //不同的查询类型，传入的条件会略有不同【】
    //查询影片的详细信息->dubbo的异步获取

    //获取影片描述信息
// FilmDescVO filmDescVO=filmAsyncServiceApi.getFilmDesc(filmId);
    filmAsyncServiceApi.getFilmDesc(filmId);
    Future<FilmDescVO> filmDescVOFuture=RpcContext.getContext().getFuture();
    //获取图片信息
//ImgVO imgVO=filmAsyncServiceApi.getImgs(filmId);
    filmAsyncServiceApi.getImgs(filmId);
    Future<ImgVO> imgVOFuture=RpcContext.getContext().getFuture();
//获取导演信息
   // ActorVO directorVO=filmAsyncServiceApi.getDectInfo(filmId);
    filmAsyncServiceApi.getDectInfo(filmId);
    Future<ActorVO> decVOFuture=RpcContext.getContext().getFuture();

    //获取演员信息
 //  List<ActorVO> actors=filmAsyncServiceApi.getActors(filmId);
    filmAsyncServiceApi.getActors(filmId);
    Future<List<ActorVO>> actorsVOFuture=RpcContext.getContext().getFuture();

InfoRequestVO infoRequestVO=new InfoRequestVO();
//组织actor属性
ActorRequestVO actorRequestVO=new ActorRequestVO();
actorRequestVO.setActors(actorsVOFuture.get());
actorRequestVO.setDirector(decVOFuture.get());
//组织info对象
infoRequestVO.setActors(actorRequestVO);
infoRequestVO.setBiography(filmDescVOFuture.get().getBiography());
infoRequestVO.setFilmId(filmId);
infoRequestVO.setImgVO(imgVOFuture.get());
//组织成返回值
    filmDetail.setInfo04(infoRequestVO);
    return ResponseVO.success("http://img.meetingshop.cn/",filmDetail);

    }
}
