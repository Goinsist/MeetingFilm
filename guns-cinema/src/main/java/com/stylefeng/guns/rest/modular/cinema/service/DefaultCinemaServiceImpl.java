package com.stylefeng.guns.rest.modular.cinema.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;

import com.stylefeng.guns.api.cinema.CinemaServiceAPI;
import com.stylefeng.guns.api.cinema.vo.*;
import com.stylefeng.guns.api.order.OrderServiceAPI;
import com.stylefeng.guns.rest.common.persistence.dao.*;
import com.stylefeng.guns.rest.common.persistence.model.*;
import com.stylefeng.guns.rest.common.util.FTPUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@Service(interfaceClass = CinemaServiceAPI.class,filter = "tracing")
public class DefaultCinemaServiceImpl implements CinemaServiceAPI{
    @Autowired
    private MoocFieldTMapper moocFieldTMapper;
    @Autowired
    private MoocCinemaTMapper moocCinemaTMapper;
    @Autowired
    private MoocBrandDictTMapper moocBrandDictTMapper;
    @Autowired
    private MoocAreaDictTMapper moocAreaDictTMapper;
    @Autowired
    private MoocHallDictTMapper moocHallDictTMapper;
    @Autowired
    private FTPUtil ftpUtil;
    @Reference(interfaceClass = OrderServiceAPI.class, group = "order2019",check = false)
    private OrderServiceAPI orderServiceAPI;
    //!.根据CinemaQueryVO,查询影院列表
    @Override
    public Page<CinemaVO> getCinemas(CinemaQueryVO cinemaQueryVO) {
        //业务实体集合
        List<CinemaVO> cinemas=new ArrayList<>();
        Page<MoocCinemaT> page=new Page<>(cinemaQueryVO.getNowPage(),cinemaQueryVO.getPageSize());
        //判断是否传入查询条件->brandId,distId,hallType是否==99
        EntityWrapper<MoocCinemaT> entityWrapper=new EntityWrapper<>();

        if(cinemaQueryVO.getBrandId()!=99){
entityWrapper.eq("brand_id",cinemaQueryVO.getBrandId());
}
        if(cinemaQueryVO.getDistrictId()!=99){
            entityWrapper.eq("area_id",cinemaQueryVO.getDistrictId());
        }
        if(cinemaQueryVO.getHallType()!=99){
            entityWrapper.like("hall_ids","%#"+cinemaQueryVO.getHallType()+"#%");
        }
        //将数据实体转换为业务实体
      List<MoocCinemaT> moocCinemaTS = moocCinemaTMapper.selectPage(page,entityWrapper);
        for(MoocCinemaT moocCinemaT:moocCinemaTS){
            CinemaVO cinemaVO=new CinemaVO();
            cinemaVO.setAddress(moocCinemaT.getCinemaAddress());
            cinemaVO.setCinemaName(moocCinemaT.getCinemaName());
            cinemaVO.setMinimumPrice(moocCinemaT.getMinimumPrice()+"");
            cinemaVO.setUuid(moocCinemaT.getUuid()+"");
            cinemas.add(cinemaVO);
        }
        //根据条件判断影院列表总数
     long counts=moocCinemaTMapper.selectCount(entityWrapper);
        //组织返回值对象
        Page<CinemaVO> result=new Page<>();
        result.setRecords(cinemas);
        result.setSize(cinemaQueryVO.getPageSize());
        result.setTotal(counts);
        return result;
    }
//2.根据条件获取品牌列表[除了99以外,其他的数字为isActive】
    @Override

    public List<BrandVO> getBrands(int brandId) {
        boolean flag=false;
        List<BrandVO> brandVOS=new ArrayList<>();
        //判断传入的id是否存在
     MoocBrandDictT moocBrandDictT= moocBrandDictTMapper.selectById(brandId);

        //判断brandId是否等于99
        if(brandId==99||moocBrandDictT==null||moocBrandDictT.getUuid()==null){
            flag=true;
        }
        //查询所有列表
       List<MoocBrandDictT> moocBrandDictTS= moocBrandDictTMapper.selectList(null);
//判断flag如果为true，则将99置位isActive
        for(MoocBrandDictT brand:moocBrandDictTS){
            BrandVO brandVO=new BrandVO();
            brandVO.setBrandName(brand.getShowName());
            brandVO.setBrandId(brand.getUuid()+"");
            //如果flag为true，则需要99，如为false，则匹配上的内容为active
            if(flag){
                if(brand.getUuid()==99){
                    brandVO.setActive(true);
                }
            }else {
                if(brand.getUuid()==brandId){
                    brandVO.setActive(true);
                }
            }
            brandVOS.add(brandVO);
        }
        return brandVOS;
    }
//3.根据条件获取区域列表
    @Override
    public List<AreaVO> getAreas(int areaId) {
        boolean flag=false;
        List<AreaVO> areaVOS=new ArrayList<>();
        //判断传入的id是否存在
        MoocAreaDictT moocAreaDictT= moocAreaDictTMapper.selectById(areaId);

        //判断brandId是否等于99
        if(areaId==99||moocAreaDictT==null||moocAreaDictT.getUuid()==null){
            flag=true;
        }
        //查询所有列表
        List<MoocAreaDictT> moocAreaDictTS= moocAreaDictTMapper.selectList(null);
//判断flag如果为true，则将99置位isActive
        for(MoocAreaDictT area:moocAreaDictTS){
            AreaVO areaVO=new AreaVO();
            areaVO.setAreaName(area.getShowName());
            areaVO.setAreaId(area.getUuid()+"");
            //如果flag为true，则需要99，如为false，则匹配上的内容为active
            if(flag){
                if(area.getUuid()==99){
                    areaVO.setActive(true);
                }
            }else {
                if(area.getUuid()==areaId){
                    areaVO.setActive(true);
                }
            }
            areaVOS.add(areaVO);
        }
        return areaVOS;

    }

    @Override
    public List<HallTypeVO> getHallTypes(int hallType) {
        boolean flag=false;
        List<HallTypeVO> hallTypeVOS=new ArrayList<>();
        //判断传入的id是否存在
        MoocHallDictT moocHallDictT= moocHallDictTMapper.selectById(hallType);

        //判断brandId是否等于99
        if(hallType==99||moocHallDictT==null||moocHallDictT.getUuid()==null){
            flag=true;
        }
        //查询所有列表
        List<MoocHallDictT> moocHallDictTS= moocHallDictTMapper.selectList(null);
//判断flag如果为true，则将99置位isActive
        for(MoocHallDictT hall:moocHallDictTS){
            HallTypeVO hallTypeVO=new HallTypeVO();
            hallTypeVO.setHalltypeName(hall.getShowName());
            hallTypeVO.setHalltypeId(hall.getUuid()+"");
            //如果flag为true，则需要99，如为false，则匹配上的内容为active
            if(flag){
                if(hall.getUuid()==99){
                    hallTypeVO.setActive(true);
                }
            }else {
                if(hall.getUuid()==hallType){
                    hallTypeVO.setActive(true);
                }
            }
            hallTypeVOS.add(hallTypeVO);
        }
        return hallTypeVOS;
    }
//根据影院编号，获取影院信息
    @Override
    public CinemaInfoVO getCinemaInfoById(int cinemaId) {
        //数据实体
        MoocCinemaT moocCinemaT=moocCinemaTMapper.selectById(cinemaId);
      CinemaInfoVO cinemaInfoVO=new CinemaInfoVO();
        //将数据实体转换成业务实体
cinemaInfoVO.setImgUrl(moocCinemaT.getImgAddress());
cinemaInfoVO.setCinemaPhone(moocCinemaT.getCinemaPhone());
cinemaInfoVO.setCinemaName(moocCinemaT.getCinemaName());
cinemaInfoVO.setCinemaId(moocCinemaT.getUuid()+"");
cinemaInfoVO.setCinemaAddress(moocCinemaT.getCinemaAddress());
        return cinemaInfoVO;
    }
//获取所有电影的信息和对应的放映场次信息，根据影院编号
    @Override
    public List<FilmInfoVO> getFilmInfoByCinemaId(int cinemaId) {
List<FilmInfoVO> filmInfos=moocFieldTMapper.getFilmInfos(cinemaId);
for(FilmInfoVO filmInfoVO:filmInfos){
    filmInfoVO.setImgAddress("http://img.gongyu91.cn"+filmInfoVO.getImgAddress());
    filmInfoVO.setDesc(filmInfoVO.getFilmLength()+"分钟"+" | "+filmInfoVO.getFilmCats()+" | "+"导演:"+filmInfoVO.getDirectorName());
}

        return filmInfos;
    }
//根据放映场次ID获取放映信息
    @Override
    public HallInfoVO getFilmFieldInfo(int fieldId) {
         HallInfoVO hallInfoVO=  moocFieldTMapper.getHallInfo(fieldId);
        return hallInfoVO;
    }
//根据放映场次查询播放的电影编号，然后根据电影编号获取对应的电影信息
    @Override
    public FilmInfoVO getFilmInfoByFieldId(int fieldId) {
FilmInfoVO filmInfoVO=moocFieldTMapper.getFilmInfoById(fieldId);

        return filmInfoVO;
    }

    @Override
    public OrderQueryVO getOrderNeeds(int fieldId) {
      OrderQueryVO orderQueryVO=new OrderQueryVO();
        MoocFieldT moocFieldT=moocFieldTMapper.selectById(fieldId);
        orderQueryVO.setCinemaId(moocFieldT.getCinemaId()+"");
        orderQueryVO.setFilmPrice(moocFieldT.getPrice()+"");
        return orderQueryVO;
    }

    @Override
    public Page<CinemaWithFilmVO> getCinemasByFilmId(String filmId, String releaseDate, String pageIndex, String pageSize) {
        Page<CinemaWithFilmVO> result=new Page<CinemaWithFilmVO>(Integer.valueOf(pageIndex),Integer.valueOf(pageSize));
        List<CinemaWithFilmVO> cinemasByFilmId = moocFieldTMapper.getCinemasByFilmId(filmId, releaseDate);
        if(cinemasByFilmId==null||cinemasByFilmId.size()==0){
            result.setTotal(0);
            result.setRecords(new ArrayList<>());
            return result;
        }else{
            result.setRecords(cinemasByFilmId);
            result.setTotal(moocFieldTMapper.getCinemasByFilmIdCounts(filmId,releaseDate));
            result.setSize(Integer.valueOf(pageSize));
            return result;
        }


    }

    @Override
    public CinemaFilmVO getFilmsWithCinemaId(String cinemaId) {
     CinemaFilmVO cinemaFilmVO=new CinemaFilmVO();
        MoocCinemaT moocCinemaT = moocCinemaTMapper.selectById(cinemaId);
        List<FilmInfoVO> filmInfos = moocFieldTMapper.getFilmInfos(Integer.valueOf(cinemaId));
         cinemaFilmVO.setCinemaAddress(moocCinemaT.getCinemaAddress());
         cinemaFilmVO.setCinemaId(moocCinemaT.getUuid()+"");
         cinemaFilmVO.setCinemaName(moocCinemaT.getCinemaName());
         cinemaFilmVO.setFilmInfos(filmInfos);

        return cinemaFilmVO;
    }

    @Override
    public long getShowTime(String fieldId) {
        MoocFieldT moocFieldT = moocFieldTMapper.selectById(fieldId);

        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date=format1.parse(moocFieldT.getBeginTime());
            long time = date.getTime();
         return time;
        } catch (ParseException e) {
            e.printStackTrace();
        }
return  0;
    }



    @Override
    public JSONObject getHallSeatsJson(String hallId,String fieldId) {
        MoocHallDictT moocHallDictT = moocHallDictTMapper.selectById(hallId);
        String seatPath=moocHallDictT.getSeatAddress();
        String fileStrByAddress = ftpUtil.getFileStrByAddress(seatPath);
        String soldSeatsByFieldId = orderServiceAPI.getSoldSeatsByFieldId(Integer.valueOf(fieldId));
        //将影厅的json字符串转换为json对象
        JSONObject jsonObject= JSONObject.parseObject(fileStrByAddress);
        if(soldSeatsByFieldId!=null){
            String[] soldSeats = soldSeatsByFieldId.split(",");

            List<JSONObject> seatList= (List<JSONObject>) jsonObject.get("seatList");

            for(JSONObject s:seatList){
                for(String soldSeat:soldSeats){
                    if(Objects.equals(s.get("id"),soldSeat)){
                        if(Objects.equals(s.get("type"),"0")){
                            s.put("type","0-2");
                        }else if(Objects.equals(s.get("type"),"1")){
                            s.put("type","1-2");
                        }else if(Objects.equals(s.get("type"),"2")){
                            s.put("type","2-2");
                        }
                        break;
                    }
                }
            }
        }


        return jsonObject;
    }


}
