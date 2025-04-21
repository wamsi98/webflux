package com.grizz.wooman.reactorpattern;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MainCopy {
    public static void main(String[] args) {
        log.info("start main");
        List<EventLoopCopy> eventLoop = List.of(new EventLoopCopy(8080),  new EventLoopCopy(8081));
        eventLoop.forEach(EventLoopCopy::run);
        log.info("end main");
    }
}
