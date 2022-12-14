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
package org.hyperledger.bpa.impl.aries.proof;

import io.micronaut.core.annotation.Nullable;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hyperledger.acy_py.generated.model.DIFHolder;
import org.hyperledger.aries.api.present_proof_v2.DIFField;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class BaseLDManager {

    private static final String START_PATH = "$.";

    private static final String CREDENTIAL_SUBJECT_PATH = START_PATH + "credentialSubject.";

    List<DIFHolder> buildDifHolder(@NonNull Set<UUID> fields) {
        return fields
                .stream().map(e -> DIFHolder.builder()
                        .directive(DIFHolder.DirectiveEnum.REQUIRED)
                        .fieldId(List.of(e.toString()))
                        .build())
                .collect(Collectors.toList());
    }

    ImmutablePair<UUID, DIFField> pair(@NonNull String path, @Nullable DIFField.Filter filter) {
        UUID key = UUID.randomUUID();
        DIFField f = DIFField.builder()
                .id(key.toString())
                .path(List.of("issuer".equals(path) ? START_PATH + path : CREDENTIAL_SUBJECT_PATH + path))
                .filter(filter)
                .build();
        return new ImmutablePair<>(key, f);
    }
}
