package com.grizz.wooman.selector;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SelectorMultiServer2 {
    private static ExecutorService executorService = Executors.newFixedThreadPool(50);

    /**
     * Selector는 하나의 스레드로 여러 채널(Socket 등)을 감시할 수 있게 해주는 Java NIO의 핵심 클래스
     * select()를 호출하면 등록된 채널 중 준비된 이벤트(accept, read 등) 가 있는지 감시하고, 있으면 알림
     *
     * @param args
     */
    @SneakyThrows
    public static void main(String[] args) {
        log.info("start main");
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open();
             Selector selector = Selector.open()            // selector open
        ) {
            serverSocket.bind(new InetSocketAddress("localhost", 8080));
            serverSocket.configureBlocking(false);                          // Non-blocking 모드로 설정
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);        // 연결 수락 이벤트 감시

            while (true) {
//                SocketChannel clientSocket = serverSocket.accept();       // busy wait 제거. client 소켓이 있을 때까지 도는 루프를 없앰. cpu점유 방지
//                if (clientSocket == null) {
//                    Thread.sleep(100);
//                    continue;
//                }
                selector.select();                                                          // 등록된 채널중 준비된 이벤트가 없다면 스레드 blocking . (CPU 낭비 없음)
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();          // set으로 등록된 SelectionKey 리스트 반환

                while(selectionKeys.hasNext()){
                    SelectionKey key = selectionKeys.next();            // key 추출
                    selectionKeys.remove();                             // remove 안 하면 중복처리 문제 발생

                    // 어떤 채널, 어떤 이벤트, 어떤 셀럭터인지
                    if (key.isAcceptable()){                                                // accept라면, clientSocket을 반환받아서 비동기처리 + read 이벤트 등록
                        SocketChannel clientSocket = ((ServerSocketChannel) key.channel()).accept();          // Client의 연결을 감지 후, 연결 수락.
                        clientSocket.configureBlocking(false);
                        clientSocket.register(selector, SelectionKey.OP_READ);              // 이제 그 소켓을 OP_READ로 Selector에 등록함
                    } else if (key.isReadable()){                                           // read 이벤트라면 , clientSocket read를 while로 루프를 기다릴 필요가 없음
                        SocketChannel clientSocket = (SocketChannel) key.channel();
                        String requestBody = handleRequest(clientSocket);
                        sendResponse(clientSocket, requestBody);
                    }
                }
            }
        }
    }

    @SneakyThrows
    private static String handleRequest(SocketChannel clientSocket) {
        ByteBuffer requestByteBuffer = ByteBuffer.allocateDirect(1024);
        clientSocket.read(requestByteBuffer);

        requestByteBuffer.flip();
        String requestBody = StandardCharsets.UTF_8.decode(requestByteBuffer).toString();
        log.info("request: {}", requestBody);

        return requestBody;
    }

    @SneakyThrows
    private static void sendResponse(SocketChannel clientSocket, String requestBody){
        CompletableFuture.runAsync(()-> {
            try {
                Thread.sleep(10);
                ByteBuffer responeByteBuffer = ByteBuffer.wrap("This is server".getBytes());
                log.info("thead Check");
                clientSocket.write(responeByteBuffer);
                clientSocket.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
}
