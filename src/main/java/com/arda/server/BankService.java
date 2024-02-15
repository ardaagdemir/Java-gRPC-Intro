package com.arda.server;

import com.arda.models.Balance;
import com.arda.models.BalanceCheckRequest;
import com.arda.models.BankServiceGrpc;
import com.arda.models.WithdrawRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class BankService extends BankServiceGrpc.BankServiceImplBase {

    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        Balance balance = Balance.newBuilder()
                .setAmount(AccountDatabase.getBalance(accountNumber))
                .build();
        //!!!!YALNIZCA BİR RESPONSE GELEBİLİR, BU YÜZDEN BİRDEN FAZLA SATIR EKLENEMEZ
        responseObserver.onNext(balance); //önce akışı tanımla 'sunucuya' gönder,
        responseObserver.onCompleted(); //sunucuya gittiğinde işlemi tamamla ve cevabı döndür
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<com.arda.models.Money> responseObserver) {
        int accountNumber = request.getAccountNumber();
        int amount = request.getAmount(); //10, 20, 30, 40... 100
        int balance = AccountDatabase.getBalance(accountNumber);

        //gRPC validation - status code
        if (balance < amount) {
            Status status = Status.FAILED_PRECONDITION.withDescription("No enough money, Your have balance: " + balance);
            responseObserver.onError(status.asRuntimeException());
            return;
        }

        //streaming response --> Server-Side
        for (int i = 0; i < (amount/10); i++){
            com.arda.models.Money money = com.arda.models.Money.newBuilder().setValue(amount).build();
            responseObserver.onNext(money);
            //Database updated
            AccountDatabase.deductBalance(accountNumber, amount);
            //Sırayla istek attığını görmek için eklendi.
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/

        }
        responseObserver.onCompleted();
    }

    //streaming request --> Client-Side
    @Override
    public StreamObserver<com.arda.models.DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        //Yalnızca bir cevap alınabilir. Metot imzasında request için bir parametre yoktur.
        //Request' de ne olursa olsun artık response'lar CashStreamingRequest içerisinde bulunan balanceStreamObserver nesnesinde saklanacaktır.
        //Bu sayede her istek gönderildiğinde tek bir cevap dönecektir.
        return new CashStreamingRequest(responseObserver);
    }
}
