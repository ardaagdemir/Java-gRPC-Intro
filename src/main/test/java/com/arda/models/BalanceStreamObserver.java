package com.arda.models;

import com.arda.models.Balance;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class BalanceStreamObserver implements StreamObserver<com.arda.models.Balance> {

    private CountDownLatch countDownLatch;

    public BalanceStreamObserver(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onNext(Balance balance) {
        System.out.println("Final Balance: " + balance.getAmount());
        countDownLatch.countDown();
    }

    @Override
    public void onError(Throwable throwable) {
        countDownLatch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("Server is done");
        countDownLatch.countDown();
    }
}
