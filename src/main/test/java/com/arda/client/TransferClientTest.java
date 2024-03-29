package com.arda.client;

import com.arda.models.TransferServiceGrpc;
import com.arda.models.TransferStreamingResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransferClientTest {

    private TransferServiceGrpc.TransferServiceStub stub;
    @Before
    public void setup() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 5555)
                .usePlaintext()
                .build();
        this.stub = TransferServiceGrpc.newStub(managedChannel);
    }


    @Test
    public void transfer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        TransferStreamingResponse response = new TransferStreamingResponse(countDownLatch);
        StreamObserver<com.arda.models.TransferRequest> requestStreamObserver = this.stub.transfer(response);

        for (int i = 0; i < 100; i++) {
            com.arda.models.TransferRequest request = com.arda.models.TransferRequest.newBuilder()
                    .setFromAccount(ThreadLocalRandom.current().nextInt(1, 11))
                    .setToAccount(ThreadLocalRandom.current().nextInt(1, 11))
                    .setAmount(ThreadLocalRandom.current().nextInt(1, 21))
                    .build();
            requestStreamObserver.onNext(request);
        }

        requestStreamObserver.onCompleted();
        countDownLatch.await();
    }
}
