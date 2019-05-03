package com.stylefeng.guns.rest.common.persistence.dao;

import com.stylefeng.guns.api.film.vo.FilmDetailVO;
import com.stylefeng.guns.rest.common.persistence.model.MoocFilmT;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>
 * 影片主表 Mapper 接口
 * </p>
 *
 * @author gongyu
 * @since 2019-04-10
 */
public interface MoocFilmTMapper extends BaseMapper<MoocFilmT> {
FilmDetailVO getFilmDetailByName(@Param("filmName") String filmName);
FilmDetailVO getFilmDetailById(@Param("uuid") String uuid);
}
