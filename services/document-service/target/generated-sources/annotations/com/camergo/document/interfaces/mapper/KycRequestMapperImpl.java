package com.camergo.document.interfaces.mapper;

import com.camergo.document.application.dto.response.DocumentResponse;
import com.camergo.document.application.dto.response.KycRequestResponse;
import com.camergo.document.domain.model.Document;
import com.camergo.document.domain.model.KycRequest;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-19T08:53:54+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260416-1330, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class KycRequestMapperImpl implements KycRequestMapper {

    @Autowired
    private DocumentMapper documentMapper;

    @Override
    public KycRequestResponse toResponse(KycRequest kycRequest) {
        if ( kycRequest == null ) {
            return null;
        }

        KycRequestResponse.KycRequestResponseBuilder kycRequestResponse = KycRequestResponse.builder();

        kycRequestResponse.pendingDocuments( documentListToDocumentResponseList( kycRequest.getPendingDocuments() ) );
        kycRequestResponse.oldestSubmissionDate( kycRequest.getOldestSubmissionDate() );
        kycRequestResponse.totalPendingCount( kycRequest.getTotalPendingCount() );
        kycRequestResponse.userId( kycRequest.getUserId() );

        return kycRequestResponse.build();
    }

    protected List<DocumentResponse> documentListToDocumentResponseList(List<Document> list) {
        if ( list == null ) {
            return null;
        }

        List<DocumentResponse> list1 = new ArrayList<DocumentResponse>( list.size() );
        for ( Document document : list ) {
            list1.add( documentMapper.toResponse( document ) );
        }

        return list1;
    }
}
