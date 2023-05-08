package io.github.divios.jairanchorca.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.airanchordtos.CertificateResponse;
import io.github.airanchordtos.CertificationRequest;
import io.github.divios.jairanchorca.exceptions.InvalidRequestException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Receiver {

    @Autowired
    private MessageProcessorService messageProcessorService;

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @RabbitListener(queues = "ca_queue", messageConverter = "jackson2Converter", returnExceptions = "true")
    public CertificateResponse receiveMessage(CertificationRequest request) {
        return generateResponse(request);
    }

    private CertificateResponse generateResponse(CertificationRequest request) {
        try {
            return messageProcessorService.processRequest(request);

        } catch (InvalidRequestException e) {
            log.error(e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e);

        } catch (Exception e) {
            log.error("There was an error trying to generate the response: " + e);
            return null;
        }
    }

}