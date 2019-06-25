package com.stylefeng.guns.api.film;

import com.stylefeng.guns.api.film.exception.FilmException;
import com.stylefeng.guns.api.film.vo.*;

import java.util.Date;
import java.util.List;

public interface FilmServiceApi {
    //获取banners
  List<BannerVo> getBanners();
    //获取热映影片

FilmVO getHotFilms(boolean isLimit,int nums,int nowPage,int sortId,int sourceId,int yearId,int catId);
    //获取即将上映影片[受欢迎程度做排序]
FilmVO getSoonFilms(boolean isLimit,int nums,int nowPage,int sortId,int sourceId,int yearId,int catId);
//获取经典影片
FilmVO getClassicFilms(int nums,int nowPage,int sortId,int sourceId,int yearId,int catId);
    //获取票房排行榜
List<FilmInfo> getBoxRanking();
    //获取人气排行榜
List<FilmInfo> getExpectRanking();
    //获取Top100
  List<FilmInfo> getTop();
  //===获取影片条件接口
    //分类条件
    List<CatVO> getCats();
    //片源条件
    List<SourceVO> getSources();
    //获取年代条件
    List<YearVO> getYears();
    //根据影片ID或者名称获取影片信息
    List<FilmDetailVO> getFilmDetail(String status,int currentPage,int pageSize,boolean isList,int searchType,String searchParam);
   //获取影片相关的其他信息[演员表,图片地址...]
    //根据筛选条件获得相应影片数目
int getFilmNumsByStatus(String status);
    //获取所有影片的id
    List<Integer> getAllFilmId();
    //获取影片描述信息
FilmDescVO getFilmDesc(String filmId);
    //获取图片信息
ImgVO getImgs(String filmId);
//获取导演信息
    ActorVO getDectInfo(String filmId);
    //获取演员信息
List<ActorVO> getActors(String filmId);
//添加电影信息
    boolean  addFilm(String filmName, String directorName, String filmType,String filmYear,String filmSource, byte[] bytes, String[] filmCats, String biography,String filmStatus,String filmLength,Date filmTime,String[] actors)throws FilmException;

    //获取电影类型
    List<TypeVO> getFilmTypes();

 //修改电影详情信息
    boolean updateFilmById(String filmId,boolean filmPosterExists, byte[] bytes,String filmName,String directorName,String filmType,String filmYear,
                           String filmSource,String[] filmCat,String[] actors,String filmLength,String biography,
                           String filmStatus,Date filmTime);

    //删除电影详情及演员相关信息
    boolean deleteFilmById(String filmId);

}
