package com.example.EDPaySystem.config;

public class RabbitMQProperties {

    // Exchanges
    public static final String EXCHANGE_NAME = "payment.exchange";
    public static final String DLX_NAME = "payment.dlx";

    // Main Queues
    public static final String BANK_QUEUE = "bank.queue";
    public static final String UPI_QUEUE = "upi.queue";
    public static final String CARD_QUEUE = "card.queue";
    public static final String WALLET_QUEUE = "wallet.queue";

    // Retry Queues
    public static final String BANK_RETRY_QUEUE = "bank.retry.queue";
    public static final String UPI_RETRY_QUEUE = "upi.retry.queue";
    public static final String CARD_RETRY_QUEUE = "card.retry.queue";
    public static final String WALLET_RETRY_QUEUE = "wallet.retry.queue";

    // Dead Letter Queues
    public static final String BANK_DLQ = "bank.dlq";
    public static final String UPI_DLQ = "upi.dlq";
    public static final String CARD_DLQ = "card.dlq";
    public static final String WALLET_DLQ = "wallet.dlq";

    // Routing Keys
    public static final String BANK_ROUTING_KEY = "BANK_TRANSFER";
    public static final String UPI_ROUTING_KEY = "UPI";
    public static final String CARD_ROUTING_KEY = "CARD";
    public static final String WALLET_ROUTING_KEY = "WALLET";

    // Retry Config
    public static final int MAX_RETRY_COUNT = 3;
    public static final int RETRY_DELAY_MS = 5000;
}