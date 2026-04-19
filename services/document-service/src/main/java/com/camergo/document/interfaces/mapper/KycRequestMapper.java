package com.camergo.document.interfaces.mapper;

import com.camergo.document.application.dto.response.KycRequestResponse;
import com.camergo.document.domain.model.KycRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = DocumentMapper.class)
public interface KycRequestMapper {

    @Mapping(source = "pendingDocuments", target = "pendingDocuments")
    KycRequestResponse toResponse(KycRequest kycRequest);
}
