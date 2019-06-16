package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.stylefeng.guns.api.user.UserAPI;

import com.stylefeng.guns.api.user.vo.UserInfoModel;
import com.stylefeng.guns.api.user.vo.UserModel;
import com.stylefeng.guns.core.util.MD5Util;
import com.stylefeng.guns.rest.common.persistence.dao.MoocUserTMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocUserT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Service(interfaceClass = UserAPI.class)
public class UserServiceImpl implements UserAPI {
@Autowired
private MoocUserTMapper moocUserTMapper;
    @Override
    public int login(String email, String password) {
        // 根据登陆账号获取数据库信息
        MoocUserT moocUserT = new MoocUserT();
        moocUserT.setEmail(email);

        MoocUserT result = moocUserTMapper.selectOne(moocUserT);

        // 获取到的结果，然后与加密以后的密码做匹配
        if(result!=null && result.getUuid()>0){
            String md5Password = MD5Util.encrypt(password);
            if(result.getUserPwd().equals(md5Password)){
                return result.getUuid();
            }
        }
        return 0;
    }

    @Override
    public boolean register(UserModel userModel) {

        //将注册信息实体转换为数据实体[mooc_user_t]
        MoocUserT moocUserT=new MoocUserT();
        moocUserT.setUserName(userModel.getUsername());

        moocUserT.setEmail(userModel.getEmail());
        moocUserT.setAddress(userModel.getAddress());
        moocUserT.setUserPhone(userModel.getPhone());
        //创建时间和修改时间_>current_timestamp
        //数据加密【MD5混淆加密+盐值->shiro加密】
       String md5Password= MD5Util.encrypt(userModel.getPassword());
        moocUserT.setUserPwd(md5Password);
        //将数据实体存入数据库
      Integer insert=  moocUserTMapper.insert(moocUserT);
     if(insert>0){
         return true;
     }else {
         return false;
     }

    }

    @Override
    public boolean checkUsername(String username) {
        EntityWrapper<MoocUserT> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("user_name",username);
        Integer result=moocUserTMapper.selectCount(entityWrapper);
        if(result!=null&&result>0){
            return false;
        }else {
            return true;
        }

    }

    @Override
    public boolean checkEmail(String email) {
        EntityWrapper<MoocUserT> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("email",email);
        Integer result=moocUserTMapper.selectCount(entityWrapper);
        if(result!=null&&result>0){
            return false;
        }else {
            return true;
        }
    }

    private UserInfoModel do2UserInfo(MoocUserT moocUserT){
        UserInfoModel userInfoModel=new UserInfoModel();
        userInfoModel.setUuid(moocUserT.getUuid());
        userInfoModel.setHeadAddress(moocUserT.getHeadUrl());
        userInfoModel.setPhone(moocUserT.getUserPhone());
        userInfoModel.setUpdateTime(moocUserT.getUpdateTime().getTime());
        userInfoModel.setEmail(moocUserT.getEmail());
        userInfoModel.setUsername(moocUserT.getUserName());
        userInfoModel.setNickname(moocUserT.getNickName());
        userInfoModel.setLifeState(""+moocUserT.getLifeState());
        userInfoModel.setBirthday(moocUserT.getBirthday());
        userInfoModel.setAddress(moocUserT.getAddress());
        userInfoModel.setSex(moocUserT.getUserSex());
        userInfoModel.setBeginTime(moocUserT.getBeginTime().getTime());
        userInfoModel.setBiography(moocUserT.getBiography());


        return userInfoModel;
    }

    private MoocUserT chageBoToPo(UserInfoModel userInfoModel) {
        MoocUserT moocUserT = new MoocUserT();
        moocUserT.setUuid(userInfoModel.getUuid());
        moocUserT.setNickName(userInfoModel.getNickname());
        moocUserT.setLifeState(Integer.parseInt(userInfoModel.getLifeState()));
        moocUserT.setBirthday(userInfoModel.getBirthday());
        moocUserT.setBiography(userInfoModel.getBiography());

        moocUserT.setHeadUrl(userInfoModel.getHeadAddress());
        moocUserT.setEmail(userInfoModel.getEmail());
        moocUserT.setAddress(userInfoModel.getAddress());
        moocUserT.setUserPhone(userInfoModel.getPhone());
        moocUserT.setUserSex(userInfoModel.getSex());
        moocUserT.setUpdateTime(time2Date(System.currentTimeMillis()));
          return moocUserT;
    }
    private Date time2Date(long time){
        Date date=new Date(time);
        return date;
    }
    @Override
    public UserInfoModel getUserInfo(int uuid) {
//根据主键查询用户信息[MoocUserT]
        MoocUserT moocUserT=moocUserTMapper.selectById(uuid);
        //将MoocUserT转换UserInfoModel
        UserInfoModel userInfoModel=do2UserInfo(moocUserT);
        //返回UserInfoModel
        return userInfoModel;
    }

    @Override
    public UserInfoModel updateUserInfo(UserInfoModel userInfoModel) {
      //将传入的数据转换为MoocUserT
        MoocUserT moocUserT=chageBoToPo(userInfoModel);
        //将数据存入数据库
       Integer isSuccess= moocUserTMapper.updateById(moocUserT);
       if(isSuccess>0){
           //按照id将用户信息查出来
         UserInfoModel userInfo=getUserInfo(moocUserT.getUuid());
           //返回给前端
           return userInfo;
       }else {
           return userInfoModel;
       }

    }
}
