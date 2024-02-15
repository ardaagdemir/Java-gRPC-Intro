package com.arda.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Create a new server to listen on port 5555
        Server server = ServerBuilder.forPort(5555)
                .addService(new BankService())
                .addService(new TransferService())
                .build();
        //port üzerinde server' ı başlatır
        server.start();
        //sonlandırır
        server.awaitTermination();
    }
}
