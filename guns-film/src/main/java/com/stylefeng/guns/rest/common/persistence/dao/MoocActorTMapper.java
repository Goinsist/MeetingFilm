package com.stylefeng.guns.rest.common.persistence.dao;

import com.stylefeng.guns.api.film.vo.ActorNameAndRoleNameVO;
import com.stylefeng.guns.api.film.vo.ActorVO;
import com.stylefeng.guns.rest.common.persistence.model.MoocActorT;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 演员表 Mapper 接口
 * </p>
 *
 * @author gongyu
 * @since 2019-04-10
 */
public interface MoocActorTMapper extends BaseMapper<MoocActorT> {
List<ActorVO> getActors(@Param("filmId") String filmId);
ActorVO getActorName(@Param("filmId") String filmId);
int insertAndGetId(@Param("moocActorT")MoocActorT moocActorT);
    List<ActorNameAndRoleNameVO> getActorNameAndRoleName(String filmId);
}
