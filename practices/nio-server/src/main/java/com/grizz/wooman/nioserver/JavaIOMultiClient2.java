package com.grizz.wooman.nioserver;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *  1000개의 비동기 클라이언트 요청을 보냄
 *  runAsync: Runnable 수행 (값 반환 X), CompletableFuture<Void> 반환
 * - 각 요청은 별도 쓰레드(executorService)에서 socket 연결 → 요청 → 응답 처리
 * - 각 요청 작업이 CompletableFuture<Void>로 리턴되고 List에 수집됨
 * - allOf(...).join()을 통해 모든 작업이 끝날 때까지 대기
 *   - join은 비동기 작업에서 끝날 때까지 대기.
 *   - 작업 완료 타이밍을 정확히 측정하기 위함.
 *   - runAsync만 작동한다면, main스레드는 다음 줄로 이동할 것
 * - futures.toArray(new CompletableFuture[0])
 *   - 빈 배열을 넘기면 알아서 최적화, 0이 안전하고 빠름
 */
@Slf4j
public class JavaIOMultiClient2 {
    private static ExecutorService executorService = Executors.newFixedThreadPool(50);
    @SneakyThrows
    public static void main(String[] args) {

        log.info("start main");
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0 ; i < 1000; i++){
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress("localhost", 8080));

                    OutputStream out = socket.getOutputStream();
                    String requestBody = "This is client";
                    out.write(requestBody.getBytes());
                    out.flush();

                    InputStream in = socket.getInputStream();
                    byte[] responseBytes = new byte[1024];
                    in.read(responseBytes);
                    log.info("result: {}", new String(responseBytes).trim());
                } catch (IOException e) {
                }
            }, executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
        log.info("end main");
        long endTime = System.currentTimeMillis();
        log.info("duration : {}", (endTime-startTime)/1000.0);
    }
}
