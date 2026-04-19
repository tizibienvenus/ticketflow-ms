package com.boaz.ticketflow.common.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class Attachment {
    private final String filename;
    private final String contentType;
    private final byte[] content; // ou InputStream
}