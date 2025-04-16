package com.grizz.wooman.nioserver;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public class JavaNIOBlockingServer {
    @SneakyThrows
    public static void main(String[] args) {
        log.info("start main");
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {               // new대신 open()
            serverSocket.bind(new InetSocketAddress("localhost", 8080));

            while (true) {
                SocketChannel clientSocket = serverSocket.accept();                         // accept 하며 소켓이 채널로 바뀜 + blocking 지점

                ByteBuffer requestByteBuffer = ByteBuffer.allocateDirect(1024);     
                clientSocket.read(requestByteBuffer);
                requestByteBuffer.flip();                                                   // 읽기모드 전환
                String requestBody = StandardCharsets.UTF_8.decode(requestByteBuffer).toString();
                log.info("request: {}", requestBody);

                ByteBuffer responeByteBuffer = ByteBuffer.wrap("This is server".getBytes());        // heap byte Buffer 생성 후 할당
                clientSocket.write(responeByteBuffer);
                clientSocket.close();
            }
        }
    }
}
