package xyz.lwh.fbs.fbslock.core.lock.redis;

import xyz.lwh.fbs.fbslock.core.lock.FbsLock;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RedisLock implements FbsLock {

    private static class LockData{
        final Thread owner;
        final AtomicInteger count;
        Date startDate;
        public LockData(Thread owner){
            this.owner = owner;
            count = new AtomicInteger(1);
            startDate = new Date();
        }
    }

    private ReentrantLock reentrantLock = new ReentrantLock();

    private Condition tryLockCondition = reentrantLock.newCondition();

    private RedisLockCmd redisLockCmd;

    private String lockId;
    //用于记录锁信息，实现可重入 考虑要不要将重入信息放入公共table，多例情况下多个redis锁实例能够重入
    private ThreadLocal<LockData> locking = new ThreadLocal<>();


    public RedisLock(String lockId,RedisLockCmd redisLockCmd){
        this.lockId = lockId;
        this.redisLockCmd = redisLockCmd;
    }


    @Override
    public void lock() {
        Thread currentThread = Thread.currentThread();
        //先获取java锁，减少同进程中，不必要的分布式锁请求。
        reentrantLock.lock();
        try {
            //判断是否持有锁
            LockData lockData = locking.get();
            if(lockData!=null){
                lockData.count.incrementAndGet();
            }else{
                //获取分布式锁
                while (true){
                    String lock = redisLockCmd.lock(lockId);
                    if(lock!=null){
                        //获取成功
                        lockData = new LockData(currentThread);
                        locking.set(lockData);
                        break;
                    }else{
                        // 阻塞
                        try {
                            tryLockCondition.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
            }
        }finally {
            reentrantLock.unlock();
        }

    }

    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unLock() {
        //Thread currentThread = Thread.currentThread();
        LockData lockData = locking.get();
        if(lockData==null){
            throw new RuntimeException("根本没有锁");
        }else{
            int i = lockData.count.addAndGet(-1);
            if (i == 0 ){
                reentrantLock.lock();
                //释放redis锁
                redisLockCmd.unlock(lockId);
                //发送唤醒通知
                tryLockCondition.signalAll();
                reentrantLock.unlock();
            }
        }
    }
}
