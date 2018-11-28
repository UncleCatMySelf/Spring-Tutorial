package com.github.unclecatmyself.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.TimeUnit;

/**
 * Created by MySelf on 2018/11/28.
 */
@Slf4j
@Component
public class LongTimeTask {

    @Async
    public void execute(DeferredResult<String> deferred){
        log.info(Thread.currentThread().getName() + "进入 taskService 的 execute方法");
        //try {
            //模拟长时间任务调度，睡眠2s
            //TimeUnit.SECONDS.sleep(2);
            //2s后给deferred发送成功消息，告诉deferred，处理结束，返回给客户端
            deferred.setResult("world");
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }
    }

}
