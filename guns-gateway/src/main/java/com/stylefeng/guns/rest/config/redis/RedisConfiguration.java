package com.stylefeng.guns.rest.config.redis;


import com.stylefeng.guns.rest.modular.order.cache.JedisPoolWriper;
import com.stylefeng.guns.rest.modular.order.cache.JedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPoolConfig;

/**
 * spring-redis.xml里的配置
 */

@Configuration
public class RedisConfiguration {
    @Value("${redis.hostname}")
    private String hostname;
    @Value("${redis.port}")
    private int port;
    @Value("${redis.pool.maxActive}")
    private int maxTotal;
    @Value("${redis.pool.maxIdle}")
    private  int maxIdle;
    @Value("${redis.pool.maxWait}")
    private long maxWaitMillis;
    @Value("${redis.pool.testOnBorrow}")
    private boolean testOnBorrow;
    @Value("${redis.password}")
    private String password;
    @Value(("${redis.pool.timeOut}"))
    private int timeout;

    @Autowired
    private JedisPoolConfig jedisPoolConfig;
    @Autowired
    private JedisPoolWriper jedisWritePool;
    @Autowired
    private JedisUtil jedisUtil;

    /**
     * 创建redis连接池的设置
     * @return
     */
    @Bean(name="jedisPoolConfig")
public JedisPoolConfig createJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //控制一个pool可分配多少个jedis实例
        jedisPoolConfig.setMaxTotal(maxTotal);
        //连接池中最多可空闲maxIdle个连接，这里取值为20
        //表示即使没有数据库连接时依然可以保持20空闲的连接
        //而不被清除，随时处于待命状态
        jedisPoolConfig.setMaxIdle(maxIdle);
        //最大等待时间，当没有可用连接时
        //l连接池等待裂解被归还的最大时间（以毫秒计数）,超过时间则抛出异常
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        //在获取连接的时候检查有效性
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        return jedisPoolConfig;
    }

    /**
     * 创建Redis连接池，并做相关配置
     * @return
     */
    @Bean(name = "jedisWritePool")
public JedisPoolWriper createJedisPoolWriper(){
        JedisPoolWriper jedisPoolWriper=new JedisPoolWriper(jedisPoolConfig,hostname,port,timeout,password);
        return jedisPoolWriper;
}

    /**
     * 创建Redis工具类，封装好redis的连接以进行相关操作
     * @return
     */
    @Bean(name="jedisUtil")
    public JedisUtil createJedisUtil(){
        JedisUtil jedisUtil=new JedisUtil();
        jedisUtil.setJedisPool(jedisWritePool);
        return jedisUtil;
}

    /**
     * Redis的key操作
     * @return
     */
    @Bean(name="jedisKeys")
    public JedisUtil.Keys createJedisKeys(){
        JedisUtil.Keys jedisKeys=jedisUtil.new Keys();
        return jedisKeys;
}

/**
 * Redis的Strings操作
 */
    @Bean(name="jedisStrings")
public JedisUtil.Strings createJedisStrings(){
        JedisUtil.Strings jedisStrings=jedisUtil.new Strings();
        return jedisStrings;
    }

    /**
     * Redis的Lists操作
     * @return
     */
    @Bean(name = "jedisLists")
    public JedisUtil.Lists createJedisLists(){
        JedisUtil.Lists jedisLists=jedisUtil.new Lists();
        return jedisLists;
    }

    /**
     * Redis的Sets操作
     * @return
     */
    @Bean(name = "jedisSets")
    public JedisUtil.Sets createJedisSets(){
        JedisUtil.Sets jedisSets=jedisUtil.new Sets();
        return jedisSets;
    }


    /**
     * Redis的Hash操作
     * @return
     */
    @Bean(name="jedisHash")
    public JedisUtil.Hash createJedisHash(){
        JedisUtil.Hash jedisHash=jedisUtil.new Hash();
        return jedisHash;
    }

}
