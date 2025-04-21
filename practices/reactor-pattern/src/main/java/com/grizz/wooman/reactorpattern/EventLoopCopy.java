package com.grizz.wooman.reactorpattern;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Reactor 클래스
 * 조건 : 별도의 스레드에서 동작 가능 -> Runnable 구현
 *
 * run은 외부(main)에서 실행
 *
 * 생성자에서 selector, serversocket 초기화
 *
 * isAcceptable는 별도 Acceptor를 구성하여 처리 -> 별도 인터페이스 구현
 */
@Slf4j
public class EventLoopCopy implements Runnable{
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ServerSocketChannel serverSocket;
    private final Selector selector;
    private final EventHandlerCopy acceptor;

    @SneakyThrows
    public EventLoopCopy(int port){
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", port));
        serverSocket.configureBlocking(false);
        acceptor = new AcceptorCopy(selector, serverSocket);

        serverSocket.register(selector, SelectionKey.OP_ACCEPT).attach(acceptor);       // attach로 객체 전달까지 가능

    }

    /**
     * run 실행 = selector의 동작 시작
     * 별도 스레드 동장
     */
    @Override
    public void run() {
        executorService.submit(() ->{
            while (true) {
                selector.select();      // 요청전까지 blocking
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();

                while(selectionKeys.hasNext()){
                    SelectionKey key = selectionKeys.next();
                    selectionKeys.remove();

                    dispatch(key);          // dispatch에서 key전달, read시에는, read가 완료되었다면 selectionKey에서 read인지 판단이 필요하기 때문에 dispatch로 넘김
                }
            }
        });
    }

    /**
     * SelectionKey를 전달 받아 처리하는 과정
     * SelectionKey는 attach된 객체또한 접근 가능 -> attachment (object 타입)
     * 해당 객체는 EventHandler 인터페이스 구현체만 넣기로 했으니 casting 필요
     *
     * SelectionKey가 accpetable인지, readable인지에 따라 handle 호출
     * 전략패턴 같음
     *
     * @param key
     */
    public void dispatch(SelectionKey key){
        EventHandlerCopy eventHandler = (EventHandlerCopy) key.attachment();            

        if(key.isAcceptable() || key.isReadable()){
            eventHandler.handle();
        }
    }
}
