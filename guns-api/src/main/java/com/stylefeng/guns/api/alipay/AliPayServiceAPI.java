package com.stylefeng.guns.api.alipay;

import com.stylefeng.guns.api.alipay.vo.AliPayResultVO;

public interface AliPayServiceAPI {
    void getQRCode(String orderId) ;
    AliPayResultVO getOrderStatus(String orderId);
}
