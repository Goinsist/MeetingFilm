package com.stylefeng.guns.rest.modular.cinema;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaServiceAPI;
import com.stylefeng.guns.api.cinema.vo.*;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.order.OrderServiceAPI;
import com.stylefeng.guns.rest.modular.cinema.vo.CinemaConditionResponseVO;
import com.stylefeng.guns.rest.modular.cinema.vo.CinemaFieldResponseVO;
import com.stylefeng.guns.rest.modular.cinema.vo.CinemaFieldsResponseVO;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cinema/")
public class CinemaController
{
    private static final String IMG_PRE="http://img.meetingshop.cn/";
    @Reference(interfaceClass = CinemaServiceAPI.class,check = false)
private CinemaServiceAPI cinemaServiceAPI;
    @Reference(interfaceClass = OrderServiceAPI.class,check = false)
    private OrderServiceAPI orderServiceAPI;

@RequestMapping(value = "getCinemas",method = RequestMethod.GET)
public ResponseVO getCinemas(CinemaQueryVO cinemaQueryVO){
    try{
        //按照五个条件进行筛选
        Page<CinemaVO> cinemas=cinemaServiceAPI.getCinemas(cinemaQueryVO);
        //判断是否有满足条件的影院
        if(cinemas.getRecords()==null||cinemas.getRecords().size()==0){
            return ResponseVO.success("没有影院可查");
        }else {
return ResponseVO.success(cinemas.getCurrent(),(int)cinemas.getPages(),"",cinemas.getRecords());
        }
    }catch (Exception e){
//如果出现异常，应该如何处理
        log.error("获取影院列表异常",e);
        return ResponseVO.serviceFail("查询影院列表失败");
    }




}
//获取影院的查询条件
    /*
    1.热点数据-放缓存
     */
@RequestMapping("getCondition")
public ResponseVO getCondition(CinemaQueryVO cinemaQueryVO){
    try{
//获取三个集合封装成一个对象返回即可
        List<BrandVO> brands = cinemaServiceAPI.getBrands(cinemaQueryVO.getBrandId());
        List<AreaVO> areas = cinemaServiceAPI.getAreas(cinemaQueryVO.getDistrictId());
        List<HallTypeVO> hallTypes = cinemaServiceAPI.getHallTypes(cinemaQueryVO.getHallType());
        CinemaConditionResponseVO cinemaConditionResponseVO=new CinemaConditionResponseVO();
        cinemaConditionResponseVO.setAreaList(areas);
        cinemaConditionResponseVO.setBrandList(brands);
        cinemaConditionResponseVO.setHalltypeList(hallTypes);
        return ResponseVO.success(cinemaConditionResponseVO);
    }catch (Exception e){
log.error("获取条件列表失败",e);
return ResponseVO.serviceFail("获取影院查询条件失败");
    }


}

@RequestMapping(value = "getFields")
public ResponseVO getFields(Integer cinemaId){
try{
    CinemaInfoVO cinemaInfoById = cinemaServiceAPI.getCinemaInfoById(cinemaId);
    List<FilmInfoVO> filmInfoByCinemaId = cinemaServiceAPI.getFilmInfoByCinemaId(cinemaId);
    CinemaFieldsResponseVO cinemaFieldsResponseVO=new CinemaFieldsResponseVO();
    cinemaFieldsResponseVO.setCinemaInfo(cinemaInfoById);
    cinemaFieldsResponseVO.setFilmList(filmInfoByCinemaId);

    return ResponseVO.success(IMG_PRE,cinemaFieldsResponseVO);

}catch (Exception e){
    log.error("获取播放场次失败",e);
    return ResponseVO.serviceFail("获取播放场次失败");
}
}
@RequestMapping(value = "getFieldInfo",method = RequestMethod.POST)
public ResponseVO getFieldInfo(Integer cinemaId,Integer fieldId){
    try{
        HallInfoVO filmFieldInfo = cinemaServiceAPI.getFilmFieldInfo(fieldId);
        CinemaInfoVO cinemaInfoById = cinemaServiceAPI.getCinemaInfoById(cinemaId);
        FilmInfoVO filmInfoByFieldId = cinemaServiceAPI.getFilmInfoByFieldId(fieldId);
        //造几个假数据，后湖会对接订单接口
        filmFieldInfo.setSoldSeats(orderServiceAPI.getSoldSeatsByFieldId(fieldId));
        CinemaFieldResponseVO cinemaFieldResponseVO=new CinemaFieldResponseVO();
        cinemaFieldResponseVO.setHallInfo(filmFieldInfo);
        cinemaFieldResponseVO.setFilmInfo(filmInfoByFieldId);
        cinemaFieldResponseVO.setCinemaInfo(cinemaInfoById);
        return ResponseVO.success(IMG_PRE,cinemaFieldResponseVO);

    }catch (Exception e){
        log.error("获取选座信息失败",e);
        return ResponseVO.serviceFail("获取选座信息失败");
    }
}


}
