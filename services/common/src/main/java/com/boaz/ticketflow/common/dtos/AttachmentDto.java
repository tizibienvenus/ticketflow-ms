package com.boaz.ticketflow.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AttachmentDto {
    private String filename;
    private String contentType;
    private String base64Content; // ou byte[] si on utilise multipart
}