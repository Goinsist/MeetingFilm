package com.stylefeng.guns.rest.modular.admin.user;
import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.stylefeng.guns.api.admin.user.AdminUserServiceAPI;
import com.stylefeng.guns.core.util.MD5Util;
import com.stylefeng.guns.rest.common.persistence.dao.AdminUserMapper;
import com.stylefeng.guns.rest.common.persistence.model.AdminUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Service(interfaceClass = AdminUserServiceAPI.class,filter = "tracing")
public class AdminUserServiceImpl implements AdminUserServiceAPI {
    @Autowired
   private AdminUserMapper adminUserMapper;
    @Override
    public int login(String username, String password) {
        AdminUser adminUser=new AdminUser();
        adminUser.setUserName(username);

      AdminUser result=  adminUserMapper.selectOne(adminUser);

      if(result.getUserPwd().equals(MD5Util.encrypt(password))){
          return result.getUserId();
      }else {
          return 0;
      }


    }

    @Override
    public boolean register(String username, String password) {
         AdminUser adminUser=new AdminUser();
         adminUser.setUserName(username);
         //MD5加密密码
      String md5Pwd=  MD5Util.encrypt(password);
         adminUser.setUserPwd(md5Pwd);
        Integer result=adminUserMapper.insert(adminUser);
        if(result>0){
            return  true;
        }else {
            return false;
        }

    }
//检查是否已存在用户名,false表示存在，true表示不存在
    @Override
    public boolean checkUsername(String username) {
        EntityWrapper<AdminUser> entityWrapper=new EntityWrapper<>();
        entityWrapper.eq("user_name",username);
        Integer result=adminUserMapper.selectCount(entityWrapper);
        if(result!=null&&result>0){
            return false;
        }else {
            return true;
        }

    }
}
