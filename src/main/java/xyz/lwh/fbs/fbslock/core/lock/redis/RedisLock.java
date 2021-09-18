package xyz.lwh.fbs.fbslock.core.lock.redis;

import xyz.lwh.fbs.fbslock.core.lock.FbsLock;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RedisLock implements FbsLock {

    private static class LockData{
        final Thread owner;
        final Integer count;
        Date startDate;
        public LockData(Thread owner){
            this.owner = owner;
            count = 1;
            
        }
    }



    private ThreadLocal<HashMap<String,>>

    @Override
    public void lock() {

    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unLock() {

    }
}
