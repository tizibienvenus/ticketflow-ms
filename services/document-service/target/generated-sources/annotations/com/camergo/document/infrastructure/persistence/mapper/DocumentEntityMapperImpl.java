package com.camergo.document.infrastructure.persistence.mapper;

import com.camergo.document.domain.model.Document;
import com.camergo.document.infrastructure.persistence.entity.DocumentEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-19T08:53:55+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260416-1330, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class DocumentEntityMapperImpl implements DocumentEntityMapper {

    @Override
    public DocumentEntity toEntity(Document domain) {
        if ( domain == null ) {
            return null;
        }

        DocumentEntity.DocumentEntityBuilder documentEntity = DocumentEntity.builder();

        documentEntity.contentType( domain.getContentType() );
        documentEntity.createdAt( domain.getCreatedAt() );
        documentEntity.deleted( domain.isDeleted() );
        documentEntity.deletedAt( domain.getDeletedAt() );
        documentEntity.deletedBy( domain.getDeletedBy() );
        documentEntity.expirationDate( domain.getExpirationDate() );
        documentEntity.fileName( domain.getFileName() );
        documentEntity.fileSizeBytes( domain.getFileSizeBytes() );
        documentEntity.fileUrl( domain.getFileUrl() );
        documentEntity.id( domain.getId() );
        documentEntity.rejectionReason( domain.getRejectionReason() );
        documentEntity.status( domain.getStatus() );
        documentEntity.type( domain.getType() );
        documentEntity.updatedAt( domain.getUpdatedAt() );
        documentEntity.userId( domain.getUserId() );
        documentEntity.version( domain.getVersion() );

        return documentEntity.build();
    }

    @Override
    public Document toDomain(DocumentEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Document.DocumentBuilder document = Document.builder();

        document.contentType( entity.getContentType() );
        document.createdAt( entity.getCreatedAt() );
        document.deleted( entity.isDeleted() );
        document.deletedAt( entity.getDeletedAt() );
        document.deletedBy( entity.getDeletedBy() );
        document.expirationDate( entity.getExpirationDate() );
        document.fileName( entity.getFileName() );
        document.fileSizeBytes( entity.getFileSizeBytes() );
        document.fileUrl( entity.getFileUrl() );
        document.id( entity.getId() );
        document.rejectionReason( entity.getRejectionReason() );
        document.status( entity.getStatus() );
        document.type( entity.getType() );
        document.updatedAt( entity.getUpdatedAt() );
        document.userId( entity.getUserId() );
        document.version( entity.getVersion() );

        return document.build();
    }
}
