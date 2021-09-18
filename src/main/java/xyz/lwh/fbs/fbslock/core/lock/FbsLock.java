package xyz.lwh.fbs.fbslock.core.lock;

import java.util.concurrent.TimeUnit;

public interface FbsLock {


    void lock();

    boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException;

    void unLock();

}
