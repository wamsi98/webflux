package com.grizz.wooman.nioserver;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public class JavaNIONonBlockingServer {
    @SneakyThrows
    public static void main(String[] args) {
        log.info("start main");
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            serverSocket.bind(new InetSocketAddress("localhost", 8080));
            serverSocket.configureBlocking(false);                   // blocking 설정 false 지정. accept시 null을 반환하고, null로 작업함

            while (true) {
                SocketChannel clientSocket = serverSocket.accept();
                if (clientSocket == null) {
                    Thread.sleep(100);              // 100ms마다 응답이 왔는지 체크 
                    continue;
                }

                handleRequest(clientSocket);            // blocking 구간, 비동기 처리 필요
            }
        }
    }

    /**
     * 위에서는 연결대기, 여기서는 읽기대기
     * @param clientSocket
     */
    @SneakyThrows
    private static void handleRequest(SocketChannel clientSocket) {
        ByteBuffer requestByteBuffer = ByteBuffer.allocateDirect(1024);
        while (clientSocket.read(requestByteBuffer) == 0) {                  // non blocking일때는 무조건 값을 준다는 보장이 없다, 값이 생기면 loop 탈출
            log.info("Reading...");
        }
        requestByteBuffer.flip();
        String requestBody = StandardCharsets.UTF_8.decode(requestByteBuffer).toString();
        log.info("request: {}", requestBody);

        Thread.sleep(10);

        ByteBuffer responeByteBuffer = ByteBuffer.wrap("This is server".getBytes());
        clientSocket.write(responeByteBuffer);
        clientSocket.close();
    }
}
