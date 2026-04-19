package com.camergo.document.interfaces.mapper;

import com.camergo.document.application.dto.response.DocumentResponse;
import com.camergo.document.domain.model.Document;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-19T08:53:55+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260416-1330, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class DocumentMapperImpl implements DocumentMapper {

    @Override
    public DocumentResponse toResponse(Document document) {
        if ( document == null ) {
            return null;
        }

        DocumentResponse.DocumentResponseBuilder documentResponse = DocumentResponse.builder();

        documentResponse.contentType( document.getContentType() );
        documentResponse.createdAt( document.getCreatedAt() );
        documentResponse.expirationDate( document.getExpirationDate() );
        documentResponse.fileName( document.getFileName() );
        documentResponse.fileSizeBytes( document.getFileSizeBytes() );
        documentResponse.fileUrl( document.getFileUrl() );
        documentResponse.id( document.getId() );
        documentResponse.rejectionReason( document.getRejectionReason() );
        documentResponse.status( document.getStatus() );
        documentResponse.type( document.getType() );
        documentResponse.updatedAt( document.getUpdatedAt() );
        documentResponse.userId( document.getUserId() );
        documentResponse.version( document.getVersion() );

        return documentResponse.build();
    }
}
