package me.josephzhu.proxytest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@Slf4j
public class TestController {

    private static String payload = IntStream.rangeClosed(1,1000).mapToObj(i->"a").collect(Collectors.joining(""));

    @GetMapping("/test")
    public String test(@RequestHeader(value = "aa", required = false) String aa){
        //log.info("aa:{}",aa);
        return payload;
    }
}
