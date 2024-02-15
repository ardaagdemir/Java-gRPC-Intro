package com.arda.models;

import com.arda.models.Money;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class MoneyStreamingResponse implements StreamObserver<com.arda.models.Money> {

    private CountDownLatch countDownLatch;

    public MoneyStreamingResponse(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onNext(Money money) {
        System.out.println("Received async: " + money.getValue());
        countDownLatch.countDown();
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println(throwable.getMessage());
        countDownLatch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("Server is done!!");
    }
}
