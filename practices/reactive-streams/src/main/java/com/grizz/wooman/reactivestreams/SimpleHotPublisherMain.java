package com.grizz.wooman.reactivestreams;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleHotPublisherMain {

    /**
     * 예상 시나리오
     *
     * subscriber1 = 1~50
     * subscriber2,3 = 50 ~ 100
     * subscriber4 = 110 ~ 150(대략)
     *
     * 비동기이기 때문에, subscribe한 시점부터 main스레드는 5초 대기.
     * 5초 후에 돌아가던 싱글스레드가 종료되면서 데이터 생성 중단.
     *
     * subscriber4는 subscriber2,3 cancel후 바로 subscribe되지 않고, 1000ms간 멈추기 때문에 대략 10개의 데이터가 빔 (데이터 생산은 100ms 기준)
     *
     * @param args
     */
    @SneakyThrows
    public static void main(String[] args) {
        // prepare publisher
        var publisher = new SimpleHotPublisher();

        // prepare subscriber1
        var subscriber = new SimpleNamedSubscriber<>("subscriber1");
        publisher.subscribe(subscriber);

        // cancel after 5s
        Thread.sleep(5000);
        subscriber.cancel();

        // prepare subscriber2,3
        var subscriber2 = new SimpleNamedSubscriber<>("subscriber2");
        var subscriber3 = new SimpleNamedSubscriber<>("subscriber3");
        publisher.subscribe(subscriber2);
        publisher.subscribe(subscriber3);

        // cancel after 5s
        Thread.sleep(5000);
        subscriber2.cancel();
        subscriber3.cancel();


        Thread.sleep(1000);

        var subscriber4 = new SimpleNamedSubscriber<>("subscriber4");
        publisher.subscribe(subscriber4);

        // cancel after 5s
        Thread.sleep(5000);
        subscriber4.cancel();

        // shutdown publisher
        publisher.shutdown();
    }
}
