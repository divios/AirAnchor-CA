package io.github.divios.jairanchorca.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.divios.jairanchorca.exceptions.InvalidRequestException;
import io.github.divios.jairanchorca.models.CertificateResponse;
import io.github.divios.jairanchorca.models.CertificationRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sawtooth.sdk.signing.Context;
import sawtooth.sdk.signing.Secp256k1PublicKey;

@Slf4j
@Service
public class MessageProcessorService {

    @Autowired
    private Context context;

    @Autowired
    private KeyWrapperService keyWrapperService;

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    public CertificateResponse processRequest(CertificationRequest request) {
        validateRequest(request);

        var toFirm = objectMapper.writeValueAsBytes(request);
        var caSignature = keyWrapperService.firm(toFirm);

        return CertificateResponse.builder()
                .ca_pub_key(keyWrapperService.getPubKey())
                .signature(caSignature)
                .build();
    }

    @SneakyThrows
    private void validateRequest(CertificationRequest request) {
        var senderPubKeyStr = request.getHeader().getSender_public_key();
        var senderRequestSignature = request.getSignature();

        Secp256k1PublicKey senderPubKey = getPubKey(senderPubKeyStr);

        verifySignature(request, senderRequestSignature, senderPubKey);
    }

    private Secp256k1PublicKey getPubKey(String senderPubKeyStr) {
        Secp256k1PublicKey senderPubKey;
        try {
            return Secp256k1PublicKey.fromHex(senderPubKeyStr);     // validate pub is valid

        } catch (Exception e) {
            throw new InvalidRequestException("Invalid request public key: " + senderPubKeyStr);
        }
    }

    private void verifySignature(CertificationRequest request,
                                    String senderRequestSignature,
                                    Secp256k1PublicKey senderPubKey
    ) {
        try {
            boolean result = context.verify(senderRequestSignature,                       // validate signature
                    objectMapper.writeValueAsBytes(request.getHeader()), senderPubKey);

            if (!result)
                throw new InvalidRequestException("Invalid signature: " + senderRequestSignature);

        } catch (Exception e) {
            throw new InvalidRequestException("Invalid signature: " + senderRequestSignature);
        }
    }

}
