package com.github.unclecatmyself.main;

import com.github.unclecatmyself.pojo.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

/**
 * Created by MySelf on 2018/11/22.
 */
@Slf4j
public class Application {

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        Quote quote = restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
        log.info(quote.toString());
    }

}
