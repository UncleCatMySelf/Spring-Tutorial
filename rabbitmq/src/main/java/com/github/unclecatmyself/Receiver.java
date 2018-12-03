package com.github.unclecatmyself;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

/**
 * Created by MySelf on 2018/12/3.
 */
@Slf4j
@Component
public class Receiver {

    private CountDownLatch latch = new CountDownLatch(1);

    public void receiveMessage(String message){
        log.info("Received < " + message + " >");
        latch.countDown();
    }

    public CountDownLatch getLatch(){
        return latch;
    }

}
