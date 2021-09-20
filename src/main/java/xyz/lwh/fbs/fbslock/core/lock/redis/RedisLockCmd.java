package xyz.lwh.fbs.fbslock.core.lock.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Slf4j
public class RedisLockCmd {


    private JedisPool jedisPool;
    //秒
    private int timeout = 500;


    private SetParams setParams = SetParams.setParams().nx().ex(timeout);

    public RedisLockCmd(JedisPool jedisPool){
        this.jedisPool = jedisPool;

    }

    String lock(String lockId){
        String lockValue = createRedisKeyApi(lockId);
        return lockValue;
    }


    void unlock(String lockId){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(lockId);
        }finally {
            if (jedis!=null)
                jedis.close();

        }
    }


    /**
     * api方式创建锁
     * @param lockId
     * @return
     */
    public String createRedisKeyApi(String lockId){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String set = jedis.set(lockId, lockId, setParams);
            return set;
        }finally {
            if(jedis!=null)
                jedis.close();
        }
    }

    /**
     * lua脚本创建锁
     * @param lockId
     * @return
     */
    private String createRedisKeyLua(String lockId){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String luaScript = "" +
                    "\nlocal r = tonumber(redis.call('SETNX',KEYS[1],ARGV[1]));" +
                    "\nredis.call('PEXPIRE',KEYS[1],ARGV[2]);" +
                    "\nreturn r";
            List<String> keys = new ArrayList();
            List<String> args = new ArrayList();
            keys.add(lockId);
            args.add(lockId);
            args.add(timeout+"");
            Long eval = (Long)jedis.eval(luaScript,keys,args);
            if(1 == eval.longValue()){
                return lockId;
            }
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        return null;

    }



}
