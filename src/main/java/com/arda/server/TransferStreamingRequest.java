package com.arda.server;

import com.arda.models.TransferRequest;
import com.arda.models.TransferResponse;
import com.arda.models.TransferStatus;
import io.grpc.stub.StreamObserver;

public class TransferStreamingRequest implements StreamObserver<com.arda.models.TransferRequest> {

    private StreamObserver<com.arda.models.TransferResponse> transferResponseStreamObserver;

    public TransferStreamingRequest(StreamObserver<TransferResponse> transferResponseStreamObserver) {
        this.transferResponseStreamObserver = transferResponseStreamObserver;
    }

    //Bazı işlemler tamamlanır tamamlanmaz, cevabın dönmesi gerekmektedir.
    @Override
    public void onNext(TransferRequest transferRequest) {
        int fromAccount = transferRequest.getFromAccount();
        int toAccount = transferRequest.getToAccount();
        int amount = transferRequest.getAmount();
        int balance = AccountDatabase.getBalance(fromAccount);
        TransferStatus transferStatus = TransferStatus.FAILED;

        if (balance >= amount && fromAccount != toAccount) {
            AccountDatabase.deductBalance(fromAccount, amount);
            AccountDatabase.addBalance(toAccount, amount);
            transferStatus = TransferStatus.SUCCESS;
        }
        com.arda.models.Account fromAccountInfo = com.arda.models.Account.newBuilder()
                .setAccountNumber(fromAccount)
                .setAmount(AccountDatabase.getBalance(fromAccount))
                .build();
        com.arda.models.Account toAccountInfo = com.arda.models.Account.newBuilder()
                .setAccountNumber(toAccount)
                .setAmount(AccountDatabase.getBalance(toAccount))
                .build();

        TransferResponse transferResponse = TransferResponse.newBuilder()
                .setStatus(transferStatus)
                .addAccounts(fromAccountInfo)
                .addAccounts(toAccountInfo)
                .build();
        this.transferResponseStreamObserver.onNext(transferResponse);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {
        AccountDatabase.printAccountDetails();
        this.transferResponseStreamObserver.onCompleted();
    }
}
