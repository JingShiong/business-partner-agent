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
package org.hyperledger.bpa.controller.api.issuer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hyperledger.bpa.api.aries.SchemaAPI;
import org.hyperledger.bpa.persistence.model.BPACredentialDefinition;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredDef {
    private UUID id;
    private String schemaId;
    private String credentialDefinitionId;
    private String tag;
    private Boolean supportRevocation;
    private Long createdAt;
    private SchemaAPI schema;
    private String displayText;

    public static CredDef from(BPACredentialDefinition db) {
        SchemaAPI schemaAPI = SchemaAPI.from(db.getSchema(), false, false);
        String displayText = String.format("%s (%s) - %s", schemaAPI.getLabel(), schemaAPI.getVersion(), db.getTag());
        return CredDef
                .builder()
                .id(db.getId())
                .schemaId(db.getSchema().getSchemaId())
                .credentialDefinitionId(db.getCredentialDefinitionId())
                .schema(SchemaAPI.from(db.getSchema(), true, false))
                .tag(db.getTag())
                .supportRevocation(db.getIsSupportRevocation())
                .createdAt(db.getCreatedAt().toEpochMilli())
                .displayText(displayText)
                .build();
    }
}