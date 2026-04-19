package com.camergo.document.infrastructure.persistence.mapper;

import com.camergo.document.domain.model.Document;
import com.camergo.document.infrastructure.persistence.entity.DocumentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentEntityMapper {

    DocumentEntity toEntity(Document domain);

    Document toDomain(DocumentEntity entity);
}
