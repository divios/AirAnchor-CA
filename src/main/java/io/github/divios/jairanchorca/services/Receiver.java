package io.github.divios.jairanchorca.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.divios.jairanchorca.exceptions.InvalidRequestException;
import io.github.divios.jairanchorca.models.CertificateResponse;
import io.github.divios.jairanchorca.models.CertificationRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
    @RabbitListener(queues = "ca_queue")
    public byte[] receiveMessage(CertificationRequest request) {
        var response = generateResponse(request);

        return objectMapper.writeValueAsBytes(response);
    }

    private CertificateResponse generateResponse(CertificationRequest request) {
        try {
            return messageProcessorService.processRequest(request);

        } catch (InvalidRequestException e) {
            log.error(e.getMessage());
            return null;

        } catch (Exception e) {
            log.error("There was an error trying to generate the response: " + e);
            return null;
        }
    }

}