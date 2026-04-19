package com.camergo.document.interfaces.mapper;

import com.camergo.document.application.dto.response.DocumentResponse;
import com.camergo.document.domain.model.Document;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    DocumentResponse toResponse(Document document);
}
