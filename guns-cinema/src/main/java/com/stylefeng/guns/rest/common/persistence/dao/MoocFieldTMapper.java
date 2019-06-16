package com.stylefeng.guns.rest.common.persistence.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.stylefeng.guns.api.cinema.vo.CinemaWithFilmVO;
import com.stylefeng.guns.api.cinema.vo.FilmInfoVO;
import com.stylefeng.guns.api.cinema.vo.HallInfoVO;
import com.stylefeng.guns.rest.common.persistence.model.MoocFieldT;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 放映场次表 Mapper 接口
 * </p>
 *
 * @author gongyu
 * @since 2019-04-15
 */
public interface MoocFieldTMapper extends BaseMapper<MoocFieldT> {
List<FilmInfoVO> getFilmInfos(@Param("cinemaId") int cinemaId);
HallInfoVO getHallInfo(@Param("fieldId") int fieldId);
FilmInfoVO getFilmInfoById(@Param("fieldId") int fieldId);
List<CinemaWithFilmVO> getCinemasByFilmId(@Param("filmId") String filmId,@Param("releaseDate") String releaseDate);
int getCinemasByFilmIdCounts(@Param("filmId") String filmId,@Param("releaseDate") String releaseDate);

}
