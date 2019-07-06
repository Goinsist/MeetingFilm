package com.stylefeng.guns.rest.modular.film;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.stylefeng.guns.api.film.FilmAsyncServiceApi;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.rest.modular.film.vo.*;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

filmIndexVo.setHotFilms(filmServiceApi.getHotFilms(true,6,1,1,99,99,99));
    //即将上映的film
filmIndexVo.setSoonFilms(filmServiceApi.getSoonFilms(true,6,1,1,99,99,99));
   //获取经典电影
    filmIndexVo.setClassicFilms(filmServiceApi.getClassicFilms(6,1,1,99,99,99));
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
        if(Objects.equals(cat.getCatId(),"99")){
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
        if(Objects.equals(source.getSourceId(),"99")){
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
        if(Objects.equals(year.getYearId(),"99")){
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
    return ResponseVO.success(filmVO.getNowPage(),filmVO.getTotalCounts(),img_pre,filmVO.getFilmInfo());
    }
    //添加电影前获取用于填入选择框的电影各类别信息
@RequestMapping(value = "beforeAddFilm")
    public ResponseVO beforeAddFilm(@RequestParam(value = "filmId",required = false)String filmId){
      if(filmId!=null){
          FilmInfoVO filmInfoVO=new FilmInfoVO();
          FilmDetailVO filmDetail = filmServiceApi.getFilmDetail("0", 0, 0, false, 2, filmId).get(0);
          if(filmDetail==null){
              return ResponseVO.serviceFail("没有可查询的影片");
          }else if(filmDetail.getFilmId()==null||filmDetail.getFilmId().trim().length()==0){
              return ResponseVO.serviceFail("没有可查询的影片");
          }

          try {
              filmDetail= getFilmDetailInfo04(false,filmId,filmDetail);
              filmInfoVO.setFilmId(filmDetail.getFilmId());
              if(filmDetail.getScore()==null){
                  filmInfoVO.setFilmScore("");
              }else {
                  filmInfoVO.setFilmScore(filmDetail.getScore());
              }
              filmInfoVO.setFilmPreSaleNum(filmDetail.getFilmPreSaleNum());
              filmInfoVO.setBiography(filmDetail.getInfo04().getBiography());
              List<ActorVO> actors = filmDetail.getInfo04().getActors().getActors();
              List<String> actorAndName=new ArrayList<>();

              for(ActorVO actor:actors){
                 String nameAndRole= actor.getDirectorName()+":"+actor.getRoleName();
                 actorAndName.add(nameAndRole);
              }
              String[] array=actorAndName.toArray(new String[actorAndName.size()]);
              filmInfoVO.setActors(array);
              filmInfoVO.setDirectorName(filmDetail.getInfo04().getActors().getDirector().getDirectorName());
              filmInfoVO.setFilmCat(filmDetail.getFilmCat().substring(1,filmDetail.getFilmCat().length()-1).split("#"));
              filmInfoVO.setFilmLength(filmDetail.getInfo02().split("/")[1].split("分")[0]);
              filmInfoVO.setFilmName(filmDetail.getFilmName()+"("+filmDetail.getFilmEnName()+")");
              filmInfoVO.setFilmSource(filmDetail.getFilmSource());
              filmInfoVO.setFilmStatus(filmDetail.getStatus());
              SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
              try {
                  filmInfoVO.setDate1(sdf.parse(filmDetail.getInfo03().substring(0,19)));
                  filmInfoVO.setDate2(sdf.parse(filmDetail.getInfo03().substring(0,19)));
              } catch (ParseException e) {
                  e.printStackTrace();
              }

              filmInfoVO.setFilmType(filmDetail.getFilmType());
              filmInfoVO.setFilmYear(filmDetail.getFilmYear());
              filmInfoVO.setFilmPoster("http://img.gongyu91.cn"+filmDetail.getImgAddress());
              List<CatVO> cats = filmServiceApi.getCats();
              List<TypeVO> filmTypes = filmServiceApi.getFilmTypes();
              List<SourceVO> sources = filmServiceApi.getSources();
              List<YearVO> years = filmServiceApi.getYears();
              filmInfoVO.setFilmTypes(filmTypes);
              filmInfoVO.setFilmSources(sources);
              filmInfoVO.setFilmYears(years);
              filmInfoVO.setFilmCats(cats);
              return ResponseVO.success(filmInfoVO);
          } catch (ExecutionException e) {
              e.printStackTrace();
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
      }else {
          OptionValueVO optionValueVO = new OptionValueVO();
          List<CatVO> cats = filmServiceApi.getCats();
          List<TypeVO> filmTypes = filmServiceApi.getFilmTypes();
          List<SourceVO> sources = filmServiceApi.getSources();
          List<YearVO> years = filmServiceApi.getYears();

          optionValueVO.setFilmCats(cats);
          optionValueVO.setFilmSources(sources);
          optionValueVO.setFilmTypes(filmTypes);
          optionValueVO.setFilmYears(years);
          List<String> actors=new ArrayList<>();
          optionValueVO.setActors(actors);
          return ResponseVO.success(optionValueVO);

      }




return null;



    }
    //添加电影详细信息
    @RequestMapping(value = "addFilm")
    public ResponseVO addFilm( FilmInfoVO filmInfoVO, @RequestParam(value = "filmImg")MultipartFile filmImg){
   if(filmImg==null){
       return ResponseVO.serviceFail("文件为空,请上传文件");
   }

        try {
            byte[] bytes = filmImg.getBytes();

            try {


              boolean isSuccess=  filmServiceApi.addFilm(filmInfoVO.getFilmName(),filmInfoVO.getDirectorName(),
                        filmInfoVO.getFilmType(),filmInfoVO.getFilmYear(),filmInfoVO.getFilmSource(),bytes,filmInfoVO.getFilmCat(),
                        filmInfoVO.getBiography(),filmInfoVO.getFilmStatus(),filmInfoVO.getFilmLength(),filmInfoVO.getFilmTime(),filmInfoVO.getActors());
               if(isSuccess){
                   return ResponseVO.success("插入电影信息成功");
               }else {
                   return ResponseVO.serviceFail("插入电影信息失败");
               }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseVO.serviceFail("系统错误");
    }

@RequestMapping(value = "films",method = RequestMethod.GET)
    public ResponseVO films(@RequestParam(value = "status",required = false,defaultValue = "0") String status,
            @RequestParam(value = "currentPage",required = false,defaultValue = "0") int currentPage,
                            @RequestParam(value = "pageSize",required = false,defaultValue = "0") int pageSize,
                            @RequestParam(value = "searchParam",required = false,defaultValue ="3" )String searchParam,@RequestParam(value = "searchType",required = false,defaultValue = "2") int searchType,boolean isList) throws ExecutionException, InterruptedException {
//根据searchType，判断查询类型
    if(isList){

         char[] c=status.toCharArray();
         int nums=0;
        List<FilmDetailVO> filmDetailVOS=filmServiceApi.getFilmDetail(status,currentPage,pageSize,isList,searchType,searchParam);
  if(c[0]!="0".charAt(0)&&!Objects.equals(status,"all")){
      nums= filmServiceApi.getFilmNumsByStatus(status);
  }else if(Objects.equals(status,"all")){
      nums=filmServiceApi.getAllFilmId().size();
  }
        List<FilmDetailVO> filmDetailVOResult=new ArrayList<>();
        for(FilmDetailVO filmDetail:filmDetailVOS){
           String filmId= filmDetail.getFilmId();
          filmDetail= getFilmDetailInfo04(true,filmId,filmDetail);
          filmDetailVOResult.add(filmDetail);
        }
        return ResponseVO.success(currentPage,nums,"http://img.meetingshop.cn/",filmDetailVOResult);
    }else {
        List<FilmDetailVO> filmDetail=filmServiceApi.getFilmDetail("0",0,0,isList,searchType,searchParam);
        List<FilmDetailVO> filmDetailVOResult=new ArrayList<>();
        for(FilmDetailVO filmDetailVO:filmDetail){
            String filmId=filmDetailVO.getFilmId();
            filmDetailVO= getFilmDetailInfo04(false,filmId,filmDetailVO);
            filmDetailVO.setImgAddress("http://img.gongyu91.cn"+filmDetailVO.getImgAddress());
            filmDetailVOResult.add(filmDetailVO);
        }

        return ResponseVO.success("http://img.meetingshop.cn/",filmDetailVOResult);
    }


    }

@RequestMapping(value = "updateFilmById",method = RequestMethod.POST)
    public ResponseVO updateFilmById(String filmId,FilmInfoVO filmInfoVO,@RequestParam(value = "filmImg",required = false)MultipartFile filmImg) throws IOException {
boolean filmPosterExists=true;
if(filmImg==null){
  filmPosterExists=false;
}


                if(filmPosterExists) {
                    byte[] bytes = filmImg.getBytes();
                    //使用base64方式上传到七牛云
                    boolean isSuccess = filmServiceApi.updateFilmById(filmId,filmPosterExists,bytes, filmInfoVO.getFilmName(), filmInfoVO.getDirectorName(), filmInfoVO.getFilmType(), filmInfoVO.getFilmYear(), filmInfoVO.getFilmSource(), filmInfoVO.getFilmCat(),
                            filmInfoVO.getActors(), filmInfoVO.getFilmLength(), filmInfoVO.getBiography(), filmInfoVO.getFilmStatus(), filmInfoVO.getFilmTime());
                    if (isSuccess) {
                        return ResponseVO.success("修改电影详情信息成功");

                    } else {
                        return ResponseVO.serviceFail("修改电影详情信息失败");
                    }
                }else {
                    byte[] bytes=new byte[0];
                    //使用base64方式上传到七牛云
                    boolean isSuccess = filmServiceApi.updateFilmById(filmId,filmPosterExists,bytes, filmInfoVO.getFilmName(), filmInfoVO.getDirectorName(), filmInfoVO.getFilmType(), filmInfoVO.getFilmYear(), filmInfoVO.getFilmSource(), filmInfoVO.getFilmCat(),
                            filmInfoVO.getActors(), filmInfoVO.getFilmLength(), filmInfoVO.getBiography(), filmInfoVO.getFilmStatus(), filmInfoVO.getFilmTime());
                    if (isSuccess) {
                        return ResponseVO.success("修改电影详情信息成功");

                    } else {
                        return ResponseVO.serviceFail("修改电影详情信息失败");
                    }
                }




    }
    @RequestMapping(value = "deleteById",method = RequestMethod.POST)
    public ResponseVO deleteById(String filmId){
      boolean isSuccess=   filmServiceApi.deleteFilmById(filmId);
      if(isSuccess){
          return ResponseVO.success("删除成功");
      }
    return ResponseVO.serviceFail("删除失败");
    }
    //
    private FilmDetailVO getFilmDetailInfo04(boolean isList,String filmId,FilmDetailVO filmDetail) throws ExecutionException, InterruptedException {
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
        Future<ActorVO> actorsVOFuture=null;
        Future<List<ActorVO>> actorsVOFutureList=null;
        ActorRequestVO actorRequestVO=new ActorRequestVO();
if(isList){
    filmAsyncServiceApi.getActorName(filmId);
    actorsVOFuture=RpcContext.getContext().getFuture();
    actorRequestVO.setActorName(actorsVOFuture.get());
    actorRequestVO.setDirector(decVOFuture.get());
}else {
    //获取演员信息
    //  List<ActorVO> actors=filmAsyncServiceApi.getActors(filmId);
    filmAsyncServiceApi.getActors(filmId);
    actorsVOFutureList=RpcContext.getContext().getFuture();
    actorRequestVO.setActors(actorsVOFutureList.get());
    actorRequestVO.setDirector(decVOFuture.get());
}



        InfoRequestVO infoRequestVO=new InfoRequestVO();
//组织actor属性


//组织info对象
        infoRequestVO.setActors(actorRequestVO);
        infoRequestVO.setBiography(filmDescVOFuture.get().getBiography());
        infoRequestVO.setFilmId(filmId);
        infoRequestVO.setImgVO(imgVOFuture.get());
//组织成返回值
        filmDetail.setInfo04(infoRequestVO);
        return filmDetail;
    }

    @RequestMapping(value = "list5HotSearch",method = RequestMethod.GET)
    public ResponseVO list5HotSearch(){
        Set<String> strings = filmServiceApi.list5HotSearch();
        return ResponseVO.success(strings);
    }
}
