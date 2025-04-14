package com.grizz.wooman.reactivestreams;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;

@Slf4j
public class HotPublisher implements Flow.Publisher<Integer>{
    private final ExecutorService publisherExecutor = Executors.newSingleThreadExecutor();      // publisher 초기화시 주기적으로 데이터 생성 목적
    private final Future<Void> task;
    private List<Integer> numbers = new ArrayList<>();
    private List<HotSubscription> subscriptions = new ArrayList<>();


    public HotPublisher(){              // publisher 생성시, 100ms 마다 데이터 무한 생성
        numbers.add(1);
        task = publisherExecutor.submit(() ->{
            for ( int i = 2 ; Thread.interrupted(); i++){
                numbers.add(i);         // 값 추가기 subsriber들에게 전파 필요
                log.info("numbers: {}", numbers);
                subscriptions.forEach(HotSubscription::wakeup);     //subscriptions들 브로드캐스팅하며, 추가된 값에 대해서는 전달?, 100개의 subscrbier가 있다면, 100개의 요청을 보낼것
                Thread.sleep(100);
            }
            return null;
        });
    }

    /**
     * 스레드 작업 종료
     */
    public void shutdown(){
        this.task.cancel(true);         // future cancel
        publisherExecutor.shutdown();
    }


    /**
     * 데이터 구독 (subscribe)
     *
     * subscriber에게 줄 subscriptions 필요
     *
     * onSubscribe - 데이터를 통지할 준비가 되었음을 알림
     * @param subscriber the subscriber
     */
    @Override
    public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
        HotSubscription hotSubscription = new HotSubscription(subscriber);
        subscriber.onSubscribe(hotSubscription);
        subscriptions.add(hotSubscription);
    }


    /**
     * Subscription 클래스
     * numbers가 계속 쌓이고 있고, 타 subscriber가 subscribe한다면 현시점에 대한 offset 관리가 필요
     *
     * offset - 마지막 데이터
     * requiredOffset - 필요한 데이터수
     */
    private  class HotSubscription implements Flow.Subscription{
        private int offset;
        private int requiredOffset;
        private final Flow.Subscriber<? super Integer> subscriber;
        private final ExecutorService subscriptionExecutorService = Executors.newSingleThreadExecutor();

        public HotSubscription(Flow.Subscriber<? super Integer> subscriber){
            int lastIndex = numbers.size()-1;
            this.offset = lastIndex;
            this.requiredOffset = lastIndex;
            this.subscriber = subscriber;
        }


        /**
         * n = 요청수, 즉 backPressure
         *
         * @param n the increment of demand; a value of {@code
         * Long.MAX_VALUE} may be considered as effectively unbounded
         */
        @Override
        public void request(long n) {
            requiredOffset += n;        // 기존 offset + n(요청수)  --> [offset ~ requiredOffset] 데이터 구간 변수

            onNextWhilePossible();
        }
        /**
         * 값이 생겼으니, subscriber에게 값을 전달할 수 있으면 해
         */
        public void wakeup(){
            onNextWhilePossible();
        }
        private void onNextWhilePossible(){
            subscriptionExecutorService.submit(() ->{
                while(offset < requiredOffset && offset < numbers.size()){               // offset은 requiredOffset까지의 데이터 구간도 넘어서 안되고, numbers 사이즈를 넘쳐도 안됨
                    int item = numbers.get(offset);
                    subscriber.onNext(item);          // subscriber에 데이터를 전달
                    offset++;                         // 다음 offset 포인트 조정
                }
            });
        }

        /**
         * 더이상 데이터를 안 받으니, subscriptions에서 remove & 스레드 shutdown
         * subscriptions에는 HotSubscription 인스턴스들 존재
         */
        @Override
        public void cancel() {
            this.subscriber.onComplete();
            if(subscriptions.contains(this)){
                subscriptions.remove(this);
            }
            subscriptionExecutorService.shutdown();
        }
    }
}
