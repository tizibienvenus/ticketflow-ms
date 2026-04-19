package com.camergo.document.infrastructure;

import com.camergo.document.application.dto.request.KycRequestQuery;
import com.camergo.document.domain.model.DocumentStatus;
import com.camergo.document.domain.model.KycRequest;
import com.camergo.document.domain.repository.KycRequestRepository;
import com.camergo.document.infrastructure.persistence.KycRequestAggregationAdapter;
import com.camergo.document.infrastructure.persistence.mapper.DocumentEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycRequestAggregationAdapterTest {

    @Mock MongoTemplate mongoTemplate;
    @Mock DocumentEntityMapper entityMapper;
    @InjectMocks KycRequestAggregationAdapter adapter;

    @Test
    @DisplayName("findGroupedByUser: returns empty page when statuses set is empty")
    void emptyStatuses_returnsEmptyPage() {
        Page<KycRequest> result = adapter.findGroupedByUser(Set.of(), PageRequest.of(0, 10));
        assertThat(result).isEmpty();
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    @DisplayName("findGroupedByUser: returns empty page when mongo returns null")
    void mongoReturnsNull_returnsEmptyPage() {
        AggregationResults<org.bson.Document> emptyResult = mock(AggregationResults.class);
        when(emptyResult.getUniqueMappedResult()).thenReturn(null);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("documents"), eq(org.bson.Document.class)))
                .thenReturn(emptyResult);

        Page<KycRequest> result = adapter.findGroupedByUser(
                Set.of(DocumentStatus.PENDING), PageRequest.of(0, 10));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findGroupedByUser: null statuses set returns empty page")
    void nullStatuses_returnsEmptyPage() {
        Page<KycRequest> result = adapter.findGroupedByUser(null, PageRequest.of(0, 10));
        assertThat(result).isEmpty();
    }
}
