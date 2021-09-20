package xyz.lwh.fbs.fbslock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import xyz.lwh.fbs.fbslock.core.lock.redis.RedisLock;
import xyz.lwh.fbs.fbslock.core.lock.redis.RedisLockCmd;

class FbsLockApplicationTests {

    @Test
    public void t1 (){
        RedisLockCmd cmd = new RedisLockCmd(new JedisPool("localhost"));
        String aa = cmd.createRedisKeyApi("aa");
        System.out.println(aa);
        aa = cmd.createRedisKeyApi("aa");
        System.out.println(aa);

    }

    public static RedisLock buildLock(String code){
        RedisLockCmd cmd = new RedisLockCmd(new JedisPool("localhost"));
        RedisLock lock = new RedisLock(code,cmd);
        return lock;
    }

    @Test
    public void testLock() throws InterruptedException {
        RedisLock lock = buildLock("sps1");
        lock.lock();
        System.out.println("活的锁，干点活");
        Thread.sleep(1000*5);
        lock.unLock();
        System.out.println("释放锁");
    }

    /**
     * 测试可重入
     */
    @Test
    public void testReLock(){
        RedisLock lock = buildLock("l");
        for (int i = 0 ; i<5;i++){
            lock.lock();
            System.out.println("获得锁--"+i);
        }
        for (int i = 0 ; i<5;i++){
            lock.unLock();
            System.out.println("释放锁--"+i);
        }
    }

    /**
     * 测试单进程内锁等待
     */
    @Test
    public void testThreads() throws InterruptedException {
        final RedisLock lock = buildLock("l");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                lock.lock();
                Thread current = Thread.currentThread();
                System.out.println(current.getName()+"running !");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(current.getName()+"done !");
                lock.unLock();
            }
        };
        Thread t1 = new Thread(r,"t1");
        Thread t2 = new Thread(r,"t2");
        Thread t3 = new Thread(r,"t3");
        Thread t4 = new Thread(r,"t4");
        t1.start();
        t4.start();
        t3.start();
        t2.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();



    }

}
