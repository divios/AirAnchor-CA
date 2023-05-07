package io.github.divios.jairanchorca.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CertificateRequestHeader {

    private String distinguied_name;
    private String sender_public_key;
    private String nonce;

}
