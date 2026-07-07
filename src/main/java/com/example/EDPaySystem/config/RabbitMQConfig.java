package com.example.EDPaySystem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(RabbitMQProperties.EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(RabbitMQProperties.DLX_NAME);
    }

    // Main Queues

    @Bean
    public Queue bankQueue() {
        return QueueBuilder.durable(RabbitMQProperties.BANK_QUEUE).build();
    }

    @Bean
    public Queue upiQueue() {
        return QueueBuilder.durable(RabbitMQProperties.UPI_QUEUE).build();
    }

    @Bean
    public Queue cardQueue() {
        return QueueBuilder.durable(RabbitMQProperties.CARD_QUEUE).build();
    }

    @Bean
    public Queue walletQueue() {
        return QueueBuilder.durable(RabbitMQProperties.WALLET_QUEUE).build();
    }

    // Retry Queues

    @Bean
    public Queue bankRetryQueue() {
        return QueueBuilder.durable(RabbitMQProperties.BANK_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQProperties.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", RabbitMQProperties.BANK_ROUTING_KEY)
                .withArgument("x-message-ttl", RabbitMQProperties.RETRY_DELAY_MS)
                .build();
    }

    @Bean
    public Queue upiRetryQueue() {
        return QueueBuilder.durable(RabbitMQProperties.UPI_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQProperties.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", RabbitMQProperties.UPI_ROUTING_KEY)
                .withArgument("x-message-ttl", RabbitMQProperties.RETRY_DELAY_MS)
                .build();
    }

    @Bean
    public Queue cardRetryQueue() {
        return QueueBuilder.durable(RabbitMQProperties.CARD_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQProperties.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", RabbitMQProperties.CARD_ROUTING_KEY)
                .withArgument("x-message-ttl", RabbitMQProperties.RETRY_DELAY_MS)
                .build();
    }

    @Bean
    public Queue walletRetryQueue() {
        return QueueBuilder.durable(RabbitMQProperties.WALLET_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQProperties.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", RabbitMQProperties.WALLET_ROUTING_KEY)
                .withArgument("x-message-ttl", RabbitMQProperties.RETRY_DELAY_MS)
                .build();
    }

    // Dead Letter Queues

    @Bean
    public Queue bankDlq() {
        return QueueBuilder.durable(RabbitMQProperties.BANK_DLQ).build();
    }

    @Bean
    public Queue upiDlq() {
        return QueueBuilder.durable(RabbitMQProperties.UPI_DLQ).build();
    }

    @Bean
    public Queue cardDlq() {
        return QueueBuilder.durable(RabbitMQProperties.CARD_DLQ).build();
    }

    @Bean
    public Queue walletDlq() {
        return QueueBuilder.durable(RabbitMQProperties.WALLET_DLQ).build();
    }

    // Main Bindings

    @Bean
    public Binding bankBinding(
            @Qualifier("bankQueue") Queue bankQueue,
            @Qualifier("exchange") DirectExchange exchange) {

        return BindingBuilder.bind(bankQueue)
                .to(exchange)
                .with(RabbitMQProperties.BANK_ROUTING_KEY);
    }

    @Bean
    public Binding upiBinding(
            @Qualifier("upiQueue") Queue upiQueue,
            @Qualifier("exchange") DirectExchange exchange) {

        return BindingBuilder.bind(upiQueue)
                .to(exchange)
                .with(RabbitMQProperties.UPI_ROUTING_KEY);
    }

    @Bean
    public Binding cardBinding(
            @Qualifier("cardQueue") Queue cardQueue,
            @Qualifier("exchange") DirectExchange exchange) {

        return BindingBuilder.bind(cardQueue)
                .to(exchange)
                .with(RabbitMQProperties.CARD_ROUTING_KEY);
    }

    @Bean
    public Binding walletBinding(
            @Qualifier("walletQueue") Queue walletQueue,
            @Qualifier("exchange") DirectExchange exchange) {

        return BindingBuilder.bind(walletQueue)
                .to(exchange)
                .with(RabbitMQProperties.WALLET_ROUTING_KEY);
    }

    // Retry Bindings

    @Bean
    public Binding bankRetryBinding(
            @Qualifier("bankRetryQueue") Queue queue,
            @Qualifier("dlx") DirectExchange dlx) {

        return BindingBuilder.bind(queue)
                .to(dlx)
                .with(RabbitMQProperties.BANK_ROUTING_KEY + ".retry");
    }

    @Bean
    public Binding upiRetryBinding(
            @Qualifier("upiRetryQueue") Queue queue,
            @Qualifier("dlx") DirectExchange dlx) {

        return BindingBuilder.bind(queue)
                .to(dlx)
                .with(RabbitMQProperties.UPI_ROUTING_KEY + ".retry");
    }

    @Bean
    public Binding cardRetryBinding(
            @Qualifier("cardRetryQueue") Queue queue,
            @Qualifier("dlx") DirectExchange dlx) {

        return BindingBuilder.bind(queue)
                .to(dlx)
                .with(RabbitMQProperties.CARD_ROUTING_KEY + ".retry");
    }

    @Bean
    public Binding walletRetryBinding(
            @Qualifier("walletRetryQueue") Queue queue,
            @Qualifier("dlx") DirectExchange dlx) {

        return BindingBuilder.bind(queue)
                .to(dlx)
                .with(RabbitMQProperties.WALLET_ROUTING_KEY + ".retry");
    }

    // DLQ Bindings

    @Bean
    public Binding bankDlqBinding(
            @Qualifier("bankDlq") Queue queue,
            @Qualifier("dlx") DirectExchange dlx) {

        return BindingBuilder.bind(queue)
                .to(dlx)
                .with(RabbitMQProperties.BANK_ROUTING_KEY + ".dlq");
    }

    @Bean
    public Binding upiDlqBinding(
            @Qualifier("upiDlq") Queue queue,
            @Qualifier("dlx") DirectExchange dlx) {

        return BindingBuilder.bind(queue)
                .to(dlx)
                .with(RabbitMQProperties.UPI_ROUTING_KEY + ".dlq");
    }

    @Bean
    public Binding cardDlqBinding(
            @Qualifier("cardDlq") Queue queue,
            @Qualifier("dlx") DirectExchange dlx) {

        return BindingBuilder.bind(queue)
                .to(dlx)
                .with(RabbitMQProperties.CARD_ROUTING_KEY + ".dlq");
    }

    @Bean
    public Binding walletDlqBinding(
            @Qualifier("walletDlq") Queue queue,
            @Qualifier("dlx") DirectExchange dlx) {

        return BindingBuilder.bind(queue)
                .to(dlx)
                .with(RabbitMQProperties.WALLET_ROUTING_KEY + ".dlq");
    }

    // Message Converter

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(new ObjectMapper());
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true);

        template.setReturnsCallback(returned -> {
            System.err.println(
                    "Message UNROUTABLE - returned by broker: "
                            + returned.getMessage());
        });

        return template;
    }
}