package com.stylefeng.guns.rest.modular.film.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.exception.FilmException;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.core.util.DateUtil;
import com.stylefeng.guns.core.util.QiniuCloudUtil;
import com.stylefeng.guns.rest.common.exception.FilmExceptionEnum;
import com.stylefeng.guns.rest.common.persistence.dao.*;
import com.stylefeng.guns.rest.common.persistence.model.*;
import com.stylefeng.guns.rest.modular.film.cache.JedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;

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
    @Autowired
    private MoocTypeDictTMapper moocTypeDictTMapper;
    @Autowired
    private MoocFilmActorTMapper moocFilmActorTMapper;
    private static final String HOT_SEARCH="HOT_SEARCH";
@Autowired
private JedisUtil.SortedSet jedisSortedSets;

    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
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
        MoocFilmInfoT moocFilmInfoT=new MoocFilmInfoT();
        for(MoocFilmT moocFilmT:moocFilms){
            FilmInfo filmInfo=new FilmInfo();

            moocFilmInfoT.setFilmId(moocFilmT.getUuid()+"");
            MoocFilmInfoT moocFilmInfoT1 = moocFilmInfoTMapper.selectOne(moocFilmInfoT);
            MoocActorT moocActorT = moocActorTMapper.selectById(moocFilmInfoT1.getDirectorId());
           filmInfo.setDirectorName(moocActorT.getActorName());

            filmInfo.setImgAddress("http://img.gongyu91.cn"+moocFilmT.getImgAddress());
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
                String catStr="#"+catId+"#";
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
            filmVO.setTotalCounts(totalCounts);
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
            filmVO.setTotalCounts(totalCounts);
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
                page=new Page<>(nowPage,nums,"film_score",false);
                break;
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
        filmVO.setTotalCounts(totalCounts);


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
    public List<FilmDetailVO> getFilmDetail(String status,int currentPage,int pageSize,boolean isList,int searchType, String searchParam) {
      List<FilmDetailVO> filmDetailVOs=null;
       //searchType 1-按名称 2-按Id查找
         int rowIndex=(currentPage-1)*pageSize;
         char[] c=status.toCharArray();
         boolean isAll=false;

        if(searchType==1){
          filmDetailVOs=  moocFilmTMapper.getFilmDetailByName(isList,"%"+searchParam+"%");

          if(jedisSortedSets.zrank(HOT_SEARCH,searchParam)==null){
              jedisSortedSets.zadd(HOT_SEARCH,0,searchParam);
          }else {
              jedisSortedSets.zincrby(HOT_SEARCH,1,searchParam);
          }

        }else {
            if(Objects.equals(status,"all")){
                isAll=true;
            }
            filmDetailVOs=moocFilmTMapper.getFilmDetailListOrById(isAll,c,rowIndex,pageSize,isList,searchParam);

        }
        return filmDetailVOs;
    }

    @Override
    public int getFilmNumsByStatus(String status) {
        char[] c =status.toCharArray();

            int nums= moocFilmTMapper.getFilmCountByStatus(c);


        return nums;
    }

    @Override
    public List<Integer> getAllFilmId() {
            List<Integer> allIds= moocFilmTMapper.getAllFilmId();
        return allIds;
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

    @Transactional
    @Override
    public boolean addFilm(String filmName, String directorName, String filmType, String filmYear, String filmSource, byte[] bytes, String[] filmCats, String biography, String filmStatus, String filmLength, Date filmTime,String[] actors)throws FilmException {

        MoocFilmT moocFilmT=new MoocFilmT();
       MoocFilmInfoT moocFilmInfoT=new MoocFilmInfoT();
        MoocActorT moocActorT=new MoocActorT();
        MoocFilmActorT moocFilmActorT=new MoocFilmActorT();
        int result;
       int k= filmName.indexOf("(");
     int j=filmName.indexOf(")",k+1);
        StringBuilder s=new StringBuilder();
        String split="#";
        for(int i=0;i<filmCats.length;i++){
            String filmCat = filmCats[i];
            s.append(split);
            s.append(filmCat);
            if(i==filmCats.length-1){
                s.append(split);
            }
        }
        moocFilmT.setFilmName(filmName.substring(0,k));
        moocFilmT.setFilmCats(s.toString());
        moocFilmT.setFilmTime(filmTime);
        moocFilmT.setFilmDate(Integer.valueOf(filmYear));
        moocFilmT.setFilmArea(3);
        moocFilmT.setFilmSource(Integer.valueOf(filmSource));
        moocFilmT.setFilmStatus(Integer.valueOf(filmStatus));
        moocFilmT.setFilmType(Integer.valueOf(filmType));

       Integer filmId= moocFilmTMapper.insertAndGetId(moocFilmT);
       if(filmId<=0){
           throw new FilmException(FilmExceptionEnum.MOOC_FILM_T_ERROR);
       }
        //获取随机五位数
        QiniuCloudUtil qiniuCloudUtil=new QiniuCloudUtil();
        int rannum = new Random().nextInt(89999) + 10000;
        String nowTimeStr = sDateFormat.format(new Date());
        String filmImgName="filmPoster/"+filmId+"/"+nowTimeStr+rannum;
        //使用base64方式上传到七牛云
        String imgUrl="";
        try {
            imgUrl = qiniuCloudUtil.put64image(bytes, filmImgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        moocFilmT.setImgAddress(imgUrl);
        moocFilmTMapper.updateById(moocFilmT);
        moocFilmInfoT.setFilmId(moocFilmT.getUuid()+"");
        moocFilmInfoT.setFilmEnName(filmName.substring(k+1,j));
        moocFilmInfoT.setFilmLength(Integer.valueOf(filmLength));
        moocFilmInfoT.setBiography(biography);
        moocFilmInfoT.setFilmImgs("s,s,s,s,s");
           moocActorT.setActorName(directorName);
           moocActorTMapper.insertAndGetId(moocActorT);
           moocFilmInfoT.setDirectorId(moocActorT.getUuid());
         result=   moocFilmInfoTMapper.insert(moocFilmInfoT);
         if(result<=0){
             throw new FilmException(FilmExceptionEnum.MOOC_FILM_INFO_T_ERROR);
         }

        for(int i=0;i<actors.length;i++){
            String[] actor=actors[i].split(":|：");
            moocActorT.setActorName(actor[0]);
          Integer actorId=   moocActorTMapper.insertAndGetId(moocActorT);
          if(actorId<=0){
              throw new FilmException(FilmExceptionEnum.MOOC_ACTOR_T_ERROR);
          }
            moocFilmActorT.setRoleName(actor[1]);
            moocFilmActorT.setActorId(moocActorT.getUuid());
            moocFilmActorT.setFilmId(moocFilmT.getUuid());
           result= moocFilmActorTMapper.insert(moocFilmActorT);
           if(result<=0){
               throw new FilmException(FilmExceptionEnum.MOOC_FILM_ACTOR_T_ERROR);
           }

        }
     return true;

    }



    @Override
    public List<TypeVO> getFilmTypes() {
        List<MoocTypeDictT> moocTypeDictT=moocTypeDictTMapper.selectList(null);
        List<TypeVO> typeVOS =new ArrayList<>();

        for(MoocTypeDictT moocTypeDictT1:moocTypeDictT){
            TypeVO typeVO =new TypeVO();
           typeVO.setTypeId(moocTypeDictT1.getUuid()+"");
           typeVO.setTypeName(moocTypeDictT1.getShowName());
            typeVOS.add(typeVO);
        }
        return typeVOS;
    }

@Transactional
    @Override
    public boolean updateFilmById(String filmId, boolean filmPosterExists,byte[] bytes, String filmName, String directorName, String filmType, String filmYear, String filmSource, String[] filmCat, String[] actors, String filmLength, String biography, String filmStatus, Date filmTime) {
        //获取随机五位数
        String imgUrl="";
        if(filmPosterExists){
            QiniuCloudUtil qiniuCloudUtil=new QiniuCloudUtil();
            int rannum = new Random().nextInt(89999) + 10000;
            String nowTimeStr = sDateFormat.format(new Date());
            String filmImgName="filmPoster/"+filmId+"/"+nowTimeStr+rannum;
            //使用base64方式上传到七牛云

            try {
                qiniuCloudUtil.deleteFilmPoster(filmId);
                imgUrl = qiniuCloudUtil.put64image(bytes, filmImgName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    MoocFilmActorT moocFilmActorT=new MoocFilmActorT();
    MoocActorT moocActorT=new MoocActorT();
EntityWrapper<MoocFilmActorT> moocFilmActorTEntityWrapper=new EntityWrapper<>();
    List<ActorNameAndRoleNameVO> actorNameAndRoleName = moocActorTMapper.getActorNameAndRoleName(filmId);
    List<Integer> actorIdList=new ArrayList<>();
    for(ActorNameAndRoleNameVO actorNameAndRoleNameVO:actorNameAndRoleName){
        actorIdList.add(actorNameAndRoleNameVO.getActorId());
    }
    moocFilmActorTEntityWrapper.eq("film_id",filmId);
    moocFilmActorTMapper.delete(moocFilmActorTEntityWrapper);
moocActorTMapper.deleteBatchIds(actorIdList);
    for(int l=0;l<actors.length;l++) {
        String[] actor = actors[l].split(":|：");
        moocActorT.setActorName(actor[0]);
        Integer actorId = moocActorTMapper.insertAndGetId(moocActorT);
        if (actorId <= 0) {
            throw new FilmException(FilmExceptionEnum.MOOC_ACTOR_T_ERROR);
        }
        moocFilmActorT.setRoleName(actor[1]);
        moocFilmActorT.setActorId(moocActorT.getUuid());
        moocFilmActorT.setFilmId(Integer.valueOf(filmId));
        moocFilmActorTMapper.insert(moocFilmActorT);
    }
//    List<ActorNameAndRoleNameVO> actorNameAndRoleName = moocActorTMapper.getActorNameAndRoleName(filmId);
//boolean flag=false;
//
//continueOut:
//    for(int i=0;i<actors.length;i++) {
//        String[] actor = actors[i].split(":|：");
//
//        for(ActorNameAndRoleNameVO actorNameAndRoleNameVO:actorNameAndRoleName){
//
//            if( actor[0].equals(actorNameAndRoleNameVO.getActorName())&&actor[1].equals(actorNameAndRoleNameVO.getRoleName())){
//
//                continue continueOut;
//            }
//
//        }
//
//        for(ActorNameAndRoleNameVO actorNameAndRoleNameVO1:actorNameAndRoleName){
//            if(actor[0].equals(actorNameAndRoleNameVO1.getActorName())&&!actor[1].equals(actorNameAndRoleNameVO1.getRoleName())){
//                moocFilmActorT.setRoleName(actor[1]);
//                EntityWrapper<MoocFilmActorT> entityWrapper1=new EntityWrapper<>();
//                entityWrapper1.eq("film_id",filmId);
//                moocFilmActorTMapper.update(moocFilmActorT,entityWrapper1);
//                continue continueOut;
//            }
//        }
//
//        for(ActorNameAndRoleNameVO actorNameAndRoleNameVO2:actorNameAndRoleName){
//            if(!actor[0].equals(actorNameAndRoleNameVO2.getActorName())&&actor[1].equals(actorNameAndRoleNameVO2.getRoleName())){
//                moocActorT.setActorName(actor[0]);
//                moocActorT.setUuid(actorNameAndRoleNameVO2.getActorId());
//                moocActorTMapper.updateById(moocActorT);
//                continue continueOut;
//            }
//        }
//        for(ActorNameAndRoleNameVO actorNameAndRoleNameVO3:actorNameAndRoleName){
//            if(!actor[0].equals(actorNameAndRoleNameVO3.getActorName())&&!actor[1].equals(actorNameAndRoleNameVO3.getRoleName())){
//                moocActorT.setActorName(actor[0]);
//                moocActorTMapper.insertAndGetId(moocActorT);
//                moocFilmActorT.setRoleName(actor[1]);
//                moocFilmActorT.setActorId(moocActorT.getUuid());
//                moocFilmActorT.setFilmId(Integer.valueOf(filmId));
//                moocFilmActorTMapper.insert(moocFilmActorT);
//                continue continueOut;
//            }
//        }
//    }
        MoocFilmT moocFilmT=new MoocFilmT();
        if(filmPosterExists) {
            moocFilmT.setImgAddress(imgUrl);
        }
        moocFilmT.setFilmType(Integer.valueOf(filmType));
        moocFilmT.setFilmStatus(Integer.valueOf(filmStatus));
        moocFilmT.setFilmSource(Integer.valueOf(filmSource));
        moocFilmT.setFilmArea(2);
        moocFilmT.setFilmDate(Integer.valueOf(filmYear));
        moocFilmT.setFilmTime(filmTime);
        String filmCats="";
        for(int i=0;i<filmCat.length;i++){
            filmCats+= "#"+filmCat[i];
            if(i==filmCat.length-1){
                filmCats=filmCats+"#";
            }
        }
        moocFilmT.setFilmCats(filmCats);
        int k= filmName.indexOf("(");
        int j=filmName.indexOf(")",k+1);
        moocFilmT.setFilmName(filmName.substring(0,k));
        moocFilmT.setUuid(Integer.valueOf(filmId));
        moocFilmTMapper.updateById(moocFilmT);
        MoocFilmInfoT moocFilmInfoT=new MoocFilmInfoT();
        moocFilmInfoT.setFilmId(filmId);
        moocFilmInfoT.setFilmEnName(filmName.substring(k+1,j));
        moocFilmInfoT.setFilmLength(Integer.valueOf(filmLength));
        moocFilmInfoT.setBiography(biography);
        EntityWrapper<MoocFilmInfoT> entityWrapper0=new EntityWrapper<>();
        entityWrapper0.eq("film_id",filmId);
        moocFilmInfoTMapper.update(moocFilmInfoT,entityWrapper0);

        EntityWrapper<MoocFilmInfoT> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("film_id",filmId);
        List<MoocFilmInfoT> moocFilmInfoTS = moocFilmInfoTMapper.selectList(entityWrapper);

        moocActorT.setUuid(moocFilmInfoTS.get(0).getDirectorId());

        moocActorT.setActorName(directorName);
        moocActorTMapper.updateById(moocActorT);

        return true;
    }
@Transactional
    @Override
    public boolean deleteFilmById(String filmId) {
       int num= moocFilmTMapper.deleteById(Integer.valueOf(filmId));
       if(num<0){
           throw new FilmException(FilmExceptionEnum.MOOC_FILM_T_ERROR);
       }
        EntityWrapper<MoocFilmInfoT> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("film_id",filmId);
        MoocFilmInfoT moocFilmInfoT=new MoocFilmInfoT();
        moocFilmInfoT.setFilmId(filmId);

    MoocFilmInfoT moocFilmInfoT1 = moocFilmInfoTMapper.selectOne(moocFilmInfoT);
    int directorId=moocFilmInfoT1.getDirectorId();
      int directorResult= moocActorTMapper.deleteById(directorId);
       if(directorResult<0){
           throw new FilmException(FilmExceptionEnum.MOOC_ACTOR_T_ERROR);
       }
       int result= moocFilmInfoTMapper.delete(entityWrapper);
       if(result<0){
           throw new FilmException(FilmExceptionEnum.MOOC_FILM_INFO_T_ERROR);
       }

        List<ActorNameAndRoleNameVO> actorNameAndRoleName = moocActorTMapper.getActorNameAndRoleName(filmId);
        List<Integer> actorIdList=new ArrayList<>();
        for(ActorNameAndRoleNameVO actorNameAndRoleNameVO:actorNameAndRoleName){
            actorIdList.add(actorNameAndRoleNameVO.getActorId());
        }

       int moocActorResult= moocActorTMapper.deleteBatchIds(actorIdList);
        if(moocActorResult<0){
            throw new FilmException(FilmExceptionEnum.MOOC_ACTOR_T_ERROR);
        }
       EntityWrapper<MoocFilmActorT> entityWrapper1=new EntityWrapper<>();
       entityWrapper1.eq("film_Id",filmId);
      int filmActorResult= moocFilmActorTMapper.delete(entityWrapper1);
      if(filmActorResult<0){
          throw new FilmException(FilmExceptionEnum.MOOC_FILM_ACTOR_T_ERROR);
      }

        return true;
    }

    @Override
    public Set<String> list5HotSearch() {
        Set<String> zrevrange = jedisSortedSets.zrevrange(HOT_SEARCH, 0, 4);
        return zrevrange;
    }

}
