package com.stylefeng.guns.rest.modular.admin.user;

import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.api.admin.user.AdminUserServiceAPI;
import com.stylefeng.guns.rest.modular.auth.util.JwtTokenUtil;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/admin/")
public class AdminUserController {
@Reference(interfaceClass = AdminUserServiceAPI.class,check = false)
private AdminUserServiceAPI adminServiceAPI;
@Autowired
private JwtTokenUtil jwtTokenUtil;
    @RequestMapping(value = "login",method = RequestMethod.POST)

    public ResponseVO login(String username, String password, HttpServletResponse response) {
        int userId = adminServiceAPI.login(username, password);
        //randomKey和token已经生产完毕
        if (userId > 0) {
            final String randomKey = jwtTokenUtil.getRandomKey();
            final String token = jwtTokenUtil.generateToken("" + userId, randomKey);
            //返回值


            return ResponseVO.success(token);
        } else {
//检查用户名是否已注册

            boolean repeatName = adminServiceAPI.checkUsername(username);
            if (!repeatName) {
                return ResponseVO.serviceFail("用户名已注册");
            }
            //注册用户
            boolean isRegistSuccess = adminServiceAPI.register(username, password);
            if (!isRegistSuccess) {
                return ResponseVO.serviceFail("注册未成功");
            }
        //登录
           int isLoginSuccess= adminServiceAPI.login(username,password);
            if (isLoginSuccess<=0) {
                return ResponseVO.serviceFail("登录失败");
            }
            final String randomKey = jwtTokenUtil.getRandomKey();
            final String token = jwtTokenUtil.generateToken("" + userId, randomKey);
            //返回值

            return ResponseVO.success(token);
        }

    }
}
