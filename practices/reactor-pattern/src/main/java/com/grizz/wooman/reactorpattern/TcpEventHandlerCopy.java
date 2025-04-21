package com.grizz.wooman.reactorpattern;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TcpEventHandlerCopy implements EventHandlerCopy {

    private final ExecutorService executorService = Executors.newFixedThreadPool(50);
    private final Selector selector;// 셀렉터 주입
    private final SocketChannel clientSocket; // 소켓채널 주입

    @SneakyThrows
    public TcpEventHandlerCopy(Selector selector, SocketChannel clientSocket){
        this.selector = selector;
        this.clientSocket = clientSocket;
        this.clientSocket.configureBlocking(false); // non blocking
        this.clientSocket.register(this.selector, SelectionKey.OP_READ).attach(this);
    }

    /**
     * 요청이 완료된 시점이기 때문에, socketChannel로부터 값을 읽어도 문제 없다는것을 보장함.
     */
    @Override
    public void handle() {
        String requestBody = handleRequest(this.clientSocket);
        sendResponse(clientSocket, requestBody);
    }

    @SneakyThrows
    private String handleRequest(SocketChannel clientSocket) {
        ByteBuffer requestByteBuffer = ByteBuffer.allocateDirect(1024);
        clientSocket.read(requestByteBuffer);

        requestByteBuffer.flip();
        String requestBody = StandardCharsets.UTF_8.decode(requestByteBuffer).toString();
        log.info("request: {}", requestBody);

        return requestBody;
    }

    @SneakyThrows
    private void sendResponse(SocketChannel clientSocket, String requestBody) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(10);

                String content = "received: " + requestBody;
                ByteBuffer responeByteBuffer = ByteBuffer.wrap(content.getBytes());
                clientSocket.write(responeByteBuffer);
                clientSocket.close();
            } catch (Exception e) { }
        }, executorService);
    }
}
