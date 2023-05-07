package io.github.divios.jairanchorca.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CertificateResponse {

    private String ca_pub_key;
    private String signature;

}
