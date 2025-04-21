package com.grizz.wooman.reactorpattern;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
@RequiredArgsConstructor
public class AcceptorCopy implements EventHandlerCopy {
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;

    @SneakyThrows
    @Override
    public void handle() {
        SocketChannel clientSocket = serverSocketChannel.accept();
//        new TcpEventHandlerCopy(selector, clientSocket);
        new HttpEventHandlerCopy(selector, clientSocket);
    }
}
