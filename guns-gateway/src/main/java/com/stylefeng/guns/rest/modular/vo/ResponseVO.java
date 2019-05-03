package com.stylefeng.guns.rest.modular.vo;

import lombok.Data;

@Data
public class ResponseVO<M> {
    //返回状态[0=成功，1=业务失败,999-表示系统异常]
    private int status;
    //返回信息
    private String msg;
    //返回数据实体
    private M data;
    //图片前缀
    private String imgPre;
    //分也使用
    private int nowPage;
    private int totalPage;

    private ResponseVO(){};
    //成功
    public static<M> ResponseVO success(int nowPage,int totalPage,String imgPre,M m){
        ResponseVO responseVO=new ResponseVO();
        responseVO.setStatus(0);
        responseVO.setData(m);
        responseVO.setImgPre(imgPre);
        responseVO.setTotalPage(totalPage);
        responseVO.setNowPage(nowPage);
        return responseVO;
    }
    //成功
    public static<M> ResponseVO success(String imgPre,M m){
        ResponseVO responseVO=new ResponseVO();
        responseVO.setStatus(0);
        responseVO.setData(m);
        responseVO.setImgPre(imgPre);
        return responseVO;
    }
    //成功
    public static<M> ResponseVO success(M m){
        ResponseVO responseVO=new ResponseVO();
        responseVO.setStatus(0);
        responseVO.setData(m);
        return responseVO;
    }
    //成功信息
    public static<M> ResponseVO success(String msg){
        ResponseVO responseVO=new ResponseVO();
        responseVO.setStatus(0);
        responseVO.setMsg(msg);
        return responseVO;
    }
    //业务异常
    public static<M> ResponseVO serviceFail(String msg){
        ResponseVO responseVO=new ResponseVO();
        responseVO.setStatus(1);
        responseVO.setMsg(msg);
        return responseVO;
    }
    //系统异常
    public static<M> ResponseVO appFail(String msg){
        ResponseVO responseVO=new ResponseVO();
        responseVO.setStatus(999);
        responseVO.setMsg(msg);
        return responseVO;
    }


}
