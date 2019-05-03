package com.stylefeng.guns.rest.modular.film.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.core.util.DateUtil;
import com.stylefeng.guns.rest.common.persistence.dao.*;
import com.stylefeng.guns.rest.common.persistence.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
@Service(interfaceClass = FilmServiceApi.class)
public class DefaultFilmServiceImpl implements FilmServiceApi {
    @Autowired
    private MoocBannerTMapper moocBannerTMapper;
    @Autowired
    private MoocFilmTMapper moocFilmTMapper;
    @Autowired
    private MoocCatDictTMapper moocCatDictTMapper;
    @Autowired
    private MoocYearDictTMapper moocYearDictTMapper;
    @Autowired
    private MoocSourceDictTMapper moocSourceDictTMapper;
    @Autowired
    private MoocFilmInfoTMapper moocFilmInfoTMapper;
    @Autowired
    private MoocActorTMapper moocActorTMapper;

    @Override
    public List<BannerVo> getBanners() {
        List<BannerVo> result=new ArrayList<>();
        List<MoocBannerT> moocBanners = moocBannerTMapper.selectList(null);
        for (MoocBannerT moocBannerT : moocBanners) {
            BannerVo bannerVo = new BannerVo();
            bannerVo.setBannerId(moocBannerT.getUuid() + "");
            bannerVo.setBannerUrl(moocBannerT.getBannerUrl());
            bannerVo.setBannerAddress(moocBannerT.getBannerAddress());
           result.add(bannerVo);

        }
        return result;
    }


    private List<FilmInfo> getFilmInfos(List<MoocFilmT> moocFilms){
        List<FilmInfo> filmInfos=new ArrayList<>();
        for(MoocFilmT moocFilmT:moocFilms){
            FilmInfo filmInfo=new FilmInfo();

            filmInfo.setImgAddress(moocFilmT.getImgAddress());
            if(moocFilmT.getFilmBoxOffice()==null){
                filmInfo.setBoxNum(0);
            }else {
                filmInfo.setBoxNum(moocFilmT.getFilmBoxOffice());
            }
            filmInfo.setFilmStatus(moocFilmT.getFilmStatus());
            filmInfo.setExpectNum(moocFilmT.getFilmPresalenum());
            filmInfo.setFilmId(moocFilmT.getUuid()+"");
            filmInfo.setFilmName(moocFilmT.getFilmName());
            if(moocFilmT.getFilmScore()==null){
                filmInfo.setFilmScore("");
            }else {
                filmInfo.setFilmScore(moocFilmT.getFilmScore());
            }
            filmInfo.setFilmType(moocFilmT.getFilmType());
            filmInfo.setShowTime(DateUtil.getDay(moocFilmT.getFilmTime()));
          filmInfos.add(filmInfo);
        }
        //将转换的对象放入结果集
        return filmInfos;
    }

    private void getFilmShortInfo(int nums,EntityWrapper<MoocFilmT> entityWrapper,FilmVO filmVO){

        Page<MoocFilmT> page=new Page<>(1,nums);
        int totalNum= moocFilmTMapper.selectCount(entityWrapper);
        List<MoocFilmT> moocFilms=moocFilmTMapper.selectPage(page,entityWrapper);
        //组织filmInfos
        filmVO.setFilmInfo(getFilmInfos(moocFilms));
        filmVO.setFilmNum(totalNum);
    }
    /**
    *
    * 功能描述:
    * 获取热映电影
     * @return 
    * @author gongyu
    * @date 2019/4/10 0010 15:30
    */
    @Override
    public FilmVO getHotFilms(boolean isLimit,int nums,int nowPage,int sortId,int sourceId,int yearId,int catId) {
        FilmVO filmVO=new FilmVO();
        List<FilmInfo> filmInfos=new ArrayList<>();
        //判断是否是首页需要的内容
        //热映影片的
        EntityWrapper<MoocFilmT> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("film_status","1");
        if(isLimit){
            getFilmShortInfo(nums,entityWrapper,filmVO);
        }else {
            //如果不是，则是列表页，同样需要限制内容为热映影片
            Page<MoocFilmT> page=null;
            //根据sortid的不同，来组织不同的page对象
            //1-按热门搜索，2-按时间搜索，3-按评价搜索
            switch (sortId){
                case 1:
                    page=new Page<>(nowPage,nums,"film_box_office");
                    break;
                case 2:
                    page=new Page<>(nowPage,nums,"film_time");
                    break;
                case 3:
                    page=new Page<>(nowPage,nums,"film_score");
                default:
                    page=new Page<>(nowPage,nums,"film_box_office");
                    break;
            }
            //如果sourceId，yearId,catId不为99，则标识按照对应的编号进行查询
            if(sourceId!=99){
                entityWrapper.eq("film_source",sourceId);
            }
            if(yearId!=99){
                entityWrapper.eq("film_date",yearId);
            }
            if(catId!=99){
                //#2#4#22#
                String catStr="%#"+catId+"#%";
                entityWrapper.like("film_cats",catStr);
            }
            List<MoocFilmT> moocFilms=moocFilmTMapper.selectPage(page,entityWrapper);
            //组织filmInfos
            filmVO.setFilmInfo(getFilmInfos(moocFilms));
            filmVO.setFilmNum(moocFilms.size());
            //需要总页数，totalcounts/numbers-》0+1=1
            int totalPages=0;//每页10条，现在有6条-》1
            int totalCounts=moocFilmTMapper.selectCount(entityWrapper);
             totalPages=(totalCounts/nums)+1;
            filmVO.setTotalPage(totalPages);
            filmVO.setNowPage(nowPage);
        }

        return filmVO;
    }
/**
*
* 功能描述:
* 获取近期上映电影
 * @return 
* @author gongyu
* @date 2019/4/10 0010 15:30
*/
    @Override
    public FilmVO getSoonFilms(boolean isLimit,int nums,int nowPage,int sortId,int sourceId,int yearId,int catId) {
        FilmVO filmVO=new FilmVO();
        List<FilmInfo> filmInfos=new ArrayList<>();
        //判断是否是首页需要的内容
        //即将上映影片的
        EntityWrapper<MoocFilmT> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("film_status","2");
        if(isLimit){
            getFilmShortInfo(nums,entityWrapper,filmVO);
        }else {
            //如果不是，则是列表页，同样需要限制内容为即将上映影片
            Page<MoocFilmT> page=null;
            //根据sortid的不同，来组织不同的page对象
            //1-按热门搜索，2-按时间搜索，3-按评价搜索
            switch (sortId){
                case 1:
                    page=new Page<>(nowPage,nums,"film_preSaleNum");
                    break;
                case 2:
                    page=new Page<>(nowPage,nums,"film_time");
                    break;
                case 3:
                    page=new Page<>(nowPage,nums,"film_preSaleNum");
                default:
                    page=new Page<>(nowPage,nums,"film_preSaleNum");
                    break;
            }
            //如果sourceId，yearId,catId不为99，则标识按照对应的编号进行查询
            if(sourceId!=99){
                entityWrapper.eq("film_source",sourceId);
            }
            if(yearId!=99){
                entityWrapper.eq("film_date",yearId);
            }
            if(catId!=99){
                //#2#4#22#
                String catStr="%#"+catId+"#%";
                entityWrapper.like("film_cats",catStr);
            }
            List<MoocFilmT> moocFilms=moocFilmTMapper.selectPage(page,entityWrapper);
            //组织filmInfos
            filmVO.setFilmInfo(getFilmInfos(moocFilms));
            filmVO.setFilmNum(moocFilms.size());
            //需要总页数，totalcounts/numbers-》0+1=1
            int totalPages=0;//每页10条，现在有6条-》1
            int totalCounts=moocFilmTMapper.selectCount(entityWrapper);
            totalPages=(totalCounts/nums)+1;
            filmVO.setTotalPage(totalPages);
            filmVO.setNowPage(nowPage);
        }

        return filmVO;
    }

    @Override
    public FilmVO getClassicFilms(int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {
        FilmVO filmVO=new FilmVO();
        List<FilmInfo> filmInfos=new ArrayList<>();
        //判断是否是首页需要的内容
        //即将上映影片的
        EntityWrapper<MoocFilmT> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("film_status","3");
        //如果不是，则是列表页，同样需要限制内容为经典影片
        Page<MoocFilmT> page=null;
        //根据sortid的不同，来组织不同的page对象
        //1-按热门搜索，2-按时间搜索，3-按评价搜索
        switch (sortId){
            case 1:
                page=new Page<>(nowPage,nums,"film_box_office");
                break;
            case 2:
                page=new Page<>(nowPage,nums,"film_time");
                break;
            case 3:
                page=new Page<>(nowPage,nums,"film_score");
             default:
                 page=new Page<>(nowPage,nums,"film_box_office");
                 break;
        }
        //如果sourceId，yearId,catId不为99，则标识按照对应的编号进行查询
        if(sourceId!=99){
            entityWrapper.eq("film_source",sourceId);
        }
        if(yearId!=99){
            entityWrapper.eq("film_date",yearId);
        }
        if(catId!=99){
            //#2#4#22#
            String catStr="%#"+catId+"#%";
            entityWrapper.like("film_cats",catStr);
        }
        List<MoocFilmT> moocFilms=moocFilmTMapper.selectPage(page,entityWrapper);
        int totalNum= moocFilmTMapper.selectCount(entityWrapper);
        //组织filmInfos
        filmVO.setFilmInfo(getFilmInfos(moocFilms));
        filmVO.setFilmNum(totalNum);
        //需要总页数，totalcounts/numbers-》0+1=1
        int totalPages=0;//每页10条，现在有6条-》1
        int totalCounts=moocFilmTMapper.selectCount(entityWrapper);
        totalPages=(totalCounts/nums)+1;
        filmVO.setTotalPage(totalPages);
        filmVO.setNowPage(nowPage);


        return filmVO;
    }

    /**
*
* 功能描述:
* 获取正在上映的前十名票房的电影信息
 * @return 
* @author gongyu
* @date 2019/4/10 0010 15:31
*/
    @Override
    public List<FilmInfo> getBoxRanking() {
        //条件->正在上映的,票房前十名
        EntityWrapper<MoocFilmT> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("film_status","1");
        //默认倒叙
        Page<MoocFilmT> page=new Page<>(1,10,"film_box_office");
       List<MoocFilmT> moocFilms=moocFilmTMapper.selectPage(page,entityWrapper);
       List<FilmInfo> filmInfos=getFilmInfos(moocFilms);
       return filmInfos;

    }
/**
*
* 功能描述:
* 获取即将上映的，预售前十名的票房电影的信息
 * @return 
* @author gongyu
* @date 2019/4/10 0010 15:32
*/
    @Override
    public List<FilmInfo> getExpectRanking() {
        //条件->即将上映的，预售前十名
        EntityWrapper<MoocFilmT> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("film_status","2");
        Page<MoocFilmT> page=new Page<>(1,10,"film_preSaleNum");
        List<MoocFilmT> moocFilms=moocFilmTMapper.selectPage(page,entityWrapper);
        List<FilmInfo> filmInfos=getFilmInfos(moocFilms);
        return filmInfos;
    }
/**
*
* 功能描述:
* 获取正在上映的，评分前十名
 * @return 
* @author gongyu
* @date 2019/4/10 0010 15:32
*/
    @Override
    public List<FilmInfo> getTop() {
        //条件->正在上映的，评分前十名
        EntityWrapper<MoocFilmT> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("film_status","1");
        Page<MoocFilmT> page=new Page<>(1,10,"film_score");
        List<MoocFilmT> moocFilms=moocFilmTMapper.selectPage(page,entityWrapper);
        List<FilmInfo> filmInfos=getFilmInfos(moocFilms);
        return filmInfos;
    }

    @Override
    public List<CatVO> getCats() {
        List<CatVO> cats=new ArrayList<>();
        //查询实体对象-MoocCatDictT
        List<MoocCatDictT> moocCats=moocCatDictTMapper.selectList(null);
        //将实体对象转换为业务对象-CatVO
        for(MoocCatDictT moocCatDictT:moocCats){
            CatVO catVO=new CatVO();
            catVO.setCatId(moocCatDictT.getUuid()+"");
            catVO.setCatName(moocCatDictT.getShowName());
           cats.add(catVO);
        }
        return cats;
    }

    @Override
    public List<SourceVO> getSources() {
        List<SourceVO> sources=new ArrayList<>();
        //查询实体对象-MoocSourceDictT
        List<MoocSourceDictT> moocSources=moocSourceDictTMapper.selectList(null);
        //将实体对象转换为业务对象
        for(MoocSourceDictT moocSourceDictT:moocSources){
            SourceVO sourceVO=new SourceVO();
            sourceVO.setSourceId(moocSourceDictT.getUuid()+"");
            sourceVO.setSourceName(moocSourceDictT.getShowName());
            sources.add(sourceVO);
        }
        return sources;
    }

    @Override
    public List<YearVO> getYears() {
        List<YearVO> years=new ArrayList<>();
        //查询实体对象-MoocYearDictT
        List<MoocYearDictT> moocYears=moocYearDictTMapper.selectList(null);
        //将实体对象转换为业务对象-YearVO
        for(MoocYearDictT moocYearDictT:moocYears){
            YearVO yearVO=new YearVO();
            yearVO.setYearId(moocYearDictT.getUuid()+"");
            yearVO.setYearName(moocYearDictT.getShowName());
            years.add(yearVO);
        }
        return years;
    }

    @Override
    public FilmDetailVO getFilmDetail(int searchType, String searchParam) {
      FilmDetailVO filmDetailVO=null;
       //searchType 1-按名称 2-按Id查找
        if(searchType==1){
          filmDetailVO=  moocFilmTMapper.getFilmDetailByName("%"+searchParam+"%");
        }else {
            filmDetailVO=  moocFilmTMapper.getFilmDetailById(searchParam);
        }
        return filmDetailVO;
    }

    private MoocFilmInfoT getFilmInfo(String filmId){
        MoocFilmInfoT moocFilmInfoT=new MoocFilmInfoT();
        moocFilmInfoT.setFilmId(filmId);
        moocFilmInfoT=  moocFilmInfoTMapper.selectOne(moocFilmInfoT);
        return moocFilmInfoT;
    }
    @Override
    public FilmDescVO getFilmDesc(String filmId) {
        MoocFilmInfoT moocFilmInfoT=getFilmInfo(filmId);
        FilmDescVO filmDescVO=new FilmDescVO();
        filmDescVO.setBiography(moocFilmInfoT.getBiography());
        filmDescVO.setFilmId(filmId);
        return filmDescVO;
    }

    @Override
    public ImgVO getImgs(String filmId) {
      MoocFilmInfoT moocFilmInfoT=getFilmInfo(filmId);
      //图片地址是五个以逗号为分割的链接url
     String filmImgStr= moocFilmInfoT.getFilmImgs();
     String[] filmImgs=filmImgStr.split(",");
     ImgVO imgVO=new ImgVO();
     imgVO.setMainImg(filmImgs[0]);
     imgVO.setImg01(filmImgs[1]);
        imgVO.setImg02(filmImgs[2]);
        imgVO.setImg03(filmImgs[3]);
        imgVO.setImg04(filmImgs[4]);
     return imgVO;
    }

    @Override
    public ActorVO getDectInfo(String filmId) {
        MoocFilmInfoT moocFilmInfoT=getFilmInfo(filmId);
        //获取导演编号
      Integer directId=  moocFilmInfoT.getDirectorId();
     MoocActorT moocActorT= moocActorTMapper.selectById(directId);
       ActorVO actorVO=new ActorVO();
       actorVO.setImgAddress(moocActorT.getActorImg());
       actorVO.setDirectorName(moocActorT.getActorName());
        return actorVO;
    }

    @Override
    public List<ActorVO> getActors(String filmId) {
       List<ActorVO> actors= moocActorTMapper.getActors(filmId);
        return actors;
    }
}
