package com.github.unclecatmyself;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;

/**
 * Created by MySelf on 2018/12/3.
 */
@Slf4j
public class Receiver {

    private CountDownLatch latch;

    @Autowired
    public Receiver(CountDownLatch latch){
        this.latch = latch;
    }

    public void receiveMessage(String message){
        log.info("Received < " + message + " >");
        latch.countDown();
    }

}
