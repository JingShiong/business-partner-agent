/*
 * Copyright (c) 2020-2022 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository at
 * https://github.com/hyperledger-labs/business-partner-agent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperledger.bpa.persistence.repository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;
import org.hyperledger.aries.api.jsonld.VerifiableCredential;
import org.hyperledger.aries.api.jsonld.VerifiablePresentation;
import org.hyperledger.aries.api.present_proof.PresentationExchangeRecord;
import org.hyperledger.aries.api.present_proof.PresentationExchangeState;
import org.hyperledger.bpa.persistence.model.PartnerProof;
import org.hyperledger.bpa.persistence.model.converter.ExchangePayload;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface PartnerProofRepository extends PageableRepository<PartnerProof, UUID> {

    @Override
    @NonNull
    @Join(value = "proofTemplate", type = Join.Type.LEFT_FETCH)
    Optional<PartnerProof> findById(@NonNull UUID id);

    @Override
    @Deprecated // only use paging for exchanges
    @NonNull
    @Join(value = "proofTemplate", type = Join.Type.LEFT_FETCH)
    Iterable<PartnerProof> findAll();

    @NonNull
    @Join(value = "proofTemplate", type = Join.Type.LEFT_FETCH)
    @Join(value = "partner", type = Join.Type.LEFT_FETCH)
    @Join(value = "credentialExchange", type = Join.Type.LEFT_FETCH)
    Optional<PartnerProof> findByPresentationExchangeId(String presentationExchangeId);

    @NonNull
    @Join(value = "proofTemplate", type = Join.Type.LEFT_FETCH)
    Optional<PartnerProof> findByThreadId(String threadId);

    @NonNull
    @Join(value = "proofTemplate", type = Join.Type.LEFT_FETCH)
    Page<PartnerProof> findByPartnerId(@NonNull UUID partnerId, @NonNull Pageable pageable);

    @NonNull
    Page<PartnerProof.DeletePartnerProofDTO> getByPartnerId(@NonNull UUID partnerId, @NonNull Pageable pageable);

    void updateProblemReport(@Id UUID id, String problemReport);

    long updateReceivedProof(@Id UUID id, Boolean valid, PresentationExchangeState state,
            ExchangePayload<Map<String, PresentationExchangeRecord.RevealedAttributeGroup>, VerifiablePresentation<VerifiableCredential>> proof);

    Long countByStateEquals(PresentationExchangeState state);

    Long countByStateEqualsAndCreatedAtAfter(PresentationExchangeState state, Instant createdAt);

    Long deleteByPartnerId(UUID partnerId);
}
