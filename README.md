# MeetingFilm仿猫眼电影java后台

## 项目介绍
本项目是MeetingFilm的java后台服务,基于SpringBoot+SSM框架构建,基于maven多模块引入Dubbo实现分布式系统;
前台支持[MeetingFilm-vue](https://github.com/Goinsist/MeetingFilm-vue):基于vue构建,移动端rem自适应,调用了[movie-trailer](https://github.com/Goinsist/movie-trailer),[seat-select](https://github.com/zenghao0219/seat-select);

## 接口文档
后端接口文档[MeetingFilm-vue](https://www.showdoc.cc/327997373696136?page_id=1985046937870546)

## 项目移动端体验

<img src="http://img.gongyu91.cn/meetingfilm.png" />


## 主要使用技术
* SpringBoot+SSM框架
* 单点登录(token)
* Redis
* dubbo
* RabbitMQ
* Zookeeper
* guns框架
* SpringBootAdmin
* 七牛云存储图片
* 调用阿里沙箱测试支付


## 主要实现功能
* 首页展示: 首页展示热门电影、经典电影、即将上映电影
* 分类展示功能: 根据电影类别、电影年代、是否上映的选择展示相应电影信息
* 排名功能: 根据电影评分对电影进行排行
* 搜索功能: 输入电影名，搜索相应电影的卡片展示
* 热搜功能: 根据输入的查询参数，计入redis sortedSet
* 用户中心: 根据userId来显示用户对应信息
* 单点登录: 通过过滤器,将用户登录后的userId存入token,并返回客户端
* 影院功能: 实现影院对应放映的电影信息及票价
* 订单功能: 通过前端选座后进行下单操作
* 支付功能: 对接阿里支付沙箱环境测试接口
* 图片上传功能: 对接七牛云上传图片接口


### 目录结构
```
.guns-parent --(项目的父模块,做依赖包的管理)
├── guns-admin --(添加/编辑电影的后台管理模块)
├── guns-alipay --(阿里支付沙箱环境模块)
├── guns-api --(各服务依赖的api接口模块)
├── guns-cinema --(影院模块)
├── guns-core --(通用配置,工具包模块)
├── guns-film --(电影模块)
├── guns-gateway --(各服务的网关层)
├── guns-generator --(用于生产pojo及对面mapper,实际可在各服务test目录下的EntityGenerator里找到实现)
├── guns-monitor --(SpringBoot监控模块)
├── guns-order --(订单模块)
├── guns-rest --(各服务生成模板)
└── guns-user --(用户模块)

```

### 项目演示
