package com.stylefeng.guns.rest.common.persistence.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.stylefeng.guns.api.film.vo.FilmDetailVO;
import com.stylefeng.guns.rest.common.persistence.model.MoocFilmT;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 影片主表 Mapper 接口
 * </p>
 *
 * @author gongyu
 * @since 2019-04-10
 */
public interface MoocFilmTMapper extends BaseMapper<MoocFilmT> {
List<FilmDetailVO> getFilmDetailListOrByName(@Param("isList") boolean isList,@Param("filmName") String filmName);
List<FilmDetailVO> getFilmDetailListOrById(@Param("isAll") boolean isAll,@Param("status") char[] status,@Param("rowIndex") int rowIndex,@Param("pageSize") int pageSize,@Param("isList") boolean isList,@Param("uuid") String uuid);
List<Integer> getAllFilmId();
int getFilmCountByStatus(@Param("status") char[] status);
int insertAndGetId(@Param("moocFilmT") MoocFilmT moocFilmT);

}
