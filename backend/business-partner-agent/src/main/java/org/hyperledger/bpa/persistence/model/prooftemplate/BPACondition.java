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
package org.hyperledger.bpa.persistence.model.prooftemplate;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.core.annotation.Introspected;
import lombok.*;
import org.hyperledger.aries.api.present_proof_v2.DIFField;
import org.hyperledger.bpa.controller.api.prooftemplates.ValueCondition;
import org.hyperledger.bpa.impl.verification.prooftemplates.ValidAttributeCondition;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Introspected
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ValidAttributeCondition
public class BPACondition {
    @NonNull
    private ValueOperators operator;
    @NonNull
    private String value;

    public ValueCondition toRepresentation() {
        return new ValueCondition(operator, value);
    }

    public static BPACondition fromRepresentation(ValueCondition condition) {
        return new BPACondition(condition.getOperator(), condition.getValue());
    }

    public DIFField.Filter toDifFieldFilter() {
        DIFField.Filter.FilterBuilder b = DIFField.Filter.builder();
        switch (operator) {
        case EQUALS -> b._const(value);
        case LESS_THAN -> b.exclusiveMaximum(value);
        case GREATER_THAN -> b.exclusiveMinimum(value);
        case LESS_THAN_OR_EQUAL_TO -> b.maximum(value);
        case GREATER_THAN_OR_EQUAL_TO -> b.minimum(value);
        }
        return b.build();
    }
}
