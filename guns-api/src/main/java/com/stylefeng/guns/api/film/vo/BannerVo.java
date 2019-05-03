package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BannerVo implements Serializable {
    private String bannerId;
    private String bannerAddress;
    private String bannerUrl;
    //getter和setter方法，tostring方法
}
