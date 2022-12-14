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
package org.hyperledger.bpa.persistence.model.messaging;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import lombok.*;
import org.hyperledger.bpa.impl.messaging.email.EmailCmd;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Wires templates and user information together
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message_trigger_config")
public class MessageTriggerConfig {

    @Id
    @AutoPopulated
    private UUID id;

    @DateCreated
    private Instant createdAt;

    @DateUpdated
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    private MessageTrigger trigger;

    @ManyToOne
    private MessageUserInfo userInfo;

    @ManyToOne
    @Nullable
    private MessageTemplate template;

    public EmailCmd toEmailCmd(@NonNull String defaultSubject, @NonNull String body) {
        if (userInfo == null) {
            throw new IllegalStateException("Object is missing userInfo, is it fully joined?");
        }
        return EmailCmd
                .builder()
                .to(userInfo.getSendTo())
                .textBody(body)
                .subject(template != null && template.getSubject() != null ? template.getSubject() : defaultSubject)
                .build();
    }

}
