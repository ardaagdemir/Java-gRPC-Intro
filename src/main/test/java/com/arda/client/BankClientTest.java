package com.arda.client;

import com.arda.models.*;
import com.arda.models.Balance;
import com.arda.models.BalanceCheckRequest;
import com.arda.models.BankServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankClientTest {

    //BlockingStub --> senkron çalışır. Request'ler sırayla gönderilir.
    public BankServiceGrpc.BankServiceBlockingStub blockingStub;
    //Stub --> asenkron çalışır. Request'ler anında gönderilir.
    private BankServiceGrpc.BankServiceStub stub;

    @Before
    public void setup() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 5555) //client'dan uzak bir sunucuya bağlanmak için kullanılır
                .usePlaintext() //güvenli olmayan düz metin bağlantısını ifade eder
                .build();
        /*
        Stub, gRPC tarafından otomatik olarak oluşturulan bir sınıftır ve uzak servisle iletişim kurmak için kullanılan yöntemleri (metotları) içerir.
        gRPC, protobuf (Protocol Buffers) adlı bir dil kullanır ve bu dil ile tanımlanan servislerin istemci ve sunucu tarafında kullanılacak sınıfları otomatik olarak üretebilir.
        Bu üretilen sınıflar, istemci tarafında 'stub' olarak adlandırılır.
        Stub, gRPC ile otomatik olarak oluşturulan sınıflardan oluşur.
        Stub, gerçek ağ iletişimini yönetir, veriyi uygun formata dönüştürür ve kullanıcı tarafından gerekli olan diğer ayrıntıları ele alır.

        BlockingStub --> senkron çağrılar için kullanılr. Çağrının tamamlanmasını bekler ve cevap alana kadar işlemi durdurur.
        Stub --> asenkron çağrılar için kullanılır.
        */
        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        this.stub = BankServiceGrpc.newStub(managedChannel);
    }

    @Test
    public void balanceTest() {
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest
                .newBuilder()
                .setAccountNumber(5)
                .build();
        Balance balance = blockingStub.getBalance(balanceCheckRequest);

        System.out.println("Received balance: " + balance.getAmount());
    }

    @Test
    public void withdrawTest() {
        com.arda.models.WithdrawRequest request = com.arda.models.WithdrawRequest.newBuilder()
                .setAccountNumber(2)
                .setAmount(10)
                .build();
        //sync olarak çalışır, stream'leri döndürür
        this.blockingStub.withdraw(request)
                .forEachRemaining(money -> System.out.println("Received: " + money.getValue()));
    }

    @Test
    public void withdrawAsyncTest() throws InterruptedException {
        //İsteklerin 1 saniye beklemesini sağlar.
        CountDownLatch latch = new CountDownLatch(1);
        com.arda.models.WithdrawRequest request = com.arda.models.WithdrawRequest.newBuilder()
                .setAccountNumber(8)
                .setAmount(40)
                .build();
        //async = eşzamanlı olarak çalışır, hiçbir şey döndürmez
        this.stub.withdraw(request, new MoneyStreamingResponse(latch));
        latch.await();
        //Stub' ı uyutmanın doğru yolu bu değildir. Bunun yerine bir Count ile saymak gerekir.
        //Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
    }

    @Test
    public void cashStreamingRequest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //Birden fazla istek atılacağı için burada blockingStub kullanmak mantıklı değildir.
        //Zaten blockingStub çağırıldığında içerisinde cashDeposit metodu bulunmamaktadır.
        StreamObserver<com.arda.models.DepositRequest> streamObserver = this.stub.cashDeposit(new BalanceStreamObserver(countDownLatch));
        for (int i = 0; i < 10; i++) {
            com.arda.models.DepositRequest depositRequest = com.arda.models.DepositRequest.newBuilder().setAccountNumber(8).setAmount(10).build();
            //her işlem sırasında streamObserver' ı sunucuya gönder
            streamObserver.onNext(depositRequest);
        }
        //sunucuya istek 10 kere gittiğinde işlemi bitir ve cevabı döndür
        //streamObserver.onCompleted();
        countDownLatch.await();
    }
}
