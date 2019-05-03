package com.stylefeng.guns.core.util;
//因为令牌桶对业务有一定的容忍度
public class TokenBucket {
    private int bucketNums=100;//桶容量
    private int rate=1;//流入速度 1毫秒1个
    private int nowTokens; //当前令牌数量
    private long timestamp=getNowTime(); //时间

    private  long getNowTime(){
        return System.currentTimeMillis();
    }
    private int min(int tokens){
     if(bucketNums>tokens){
return  tokens;
     }else {
         return bucketNums;
     }
    }
    public boolean getToken(){
        //记录来拿令牌的时间

long nowTime=getNowTime();
        //添加令牌[判断该有多少个令牌]
nowTokens+=(int)((nowTime-timestamp)*rate);

        //添加以后的令牌数量与桶的容量哪个小
   nowTokens=min(nowTokens);
        System.out.println("当期令牌数量:"+nowTokens);

        //修改拿令牌的时间
timestamp=nowTime;

        //判断令牌是否足够
        if(nowTokens<1){

            return false;
        }else {
            nowTokens-=1;
            return true;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TokenBucket tokenBucket=new TokenBucket();
        for(int i=0;i<100;i++){
          if(i==10){
              Thread.sleep(500);
          }
            System.out.println("第"+i+"次请求结果="+tokenBucket.getToken());
        }
    }
}
