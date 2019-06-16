package com.stylefeng.guns.api.admin.user;

public interface AdminUserServiceAPI {
    int login(String username,String password);
    boolean register(String username,String password);
    boolean checkUsername(String username);
}
