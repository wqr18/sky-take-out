package com.sky.task;


import lombok.extern.slf4j.Slf4j;

//@Component//开启定时任务支持
@Slf4j
public class MyTask {
//    @Scheduled(cron = "0 0/1 * * * ?")//每1分钟执行一次
    public void executeTask() {
        log.info("execute task{}", System.currentTimeMillis());
    }
}
