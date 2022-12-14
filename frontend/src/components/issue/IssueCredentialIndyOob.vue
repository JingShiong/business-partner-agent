<!--
 Copyright (c) 2020-2022 - for information on the respective copyright owner
 see the NOTICE file and/or the repository at
 https://github.com/hyperledger-labs/business-partner-agent

 SPDX-License-Identifier: Apache-2.0
-->
<template>
  <v-container>
    <v-card class="mx-auto">
      <v-card-title class="bg-light">{{
        $t("component.issueOobCredential.title")
      }}</v-card-title>
      <v-card-text>
        <v-stepper v-model="currentStep" flat outlined>
          <v-stepper-header>
            <v-stepper-step :complete="currentStep > 1" step="1">
              Settings
            </v-stepper-step>
            <v-divider></v-divider>
            <v-stepper-step :complete="currentStep > 2" step="2">
              QR Code / URL
            </v-stepper-step>
          </v-stepper-header>
          <v-stepper-items>
            <v-stepper-content step="1">
              <br />
              <v-select
                :label="$t('component.issueOobCredential.credDefLabel')"
                return-object
                v-model="credDef"
                :items="credDefList"
                item-value="id"
                item-text="displayText"
                outlined
                :disabled="this.$props.credDefId !== undefined"
                dense
                @change="credDefSelected"
              ></v-select>
              <v-card v-if="credDefLoaded && expertMode" class="my-4" outlined>
                <v-card-title class="bg-light" style="font-size: small"
                  >{{ $t("component.issueCredential.expertLoad.title") }}
                  <v-btn
                    icon
                    @click="expertLoad.show = !expertLoad.show"
                    style="margin-left: auto"
                  >
                    <v-icon v-if="expertLoad.show">$vuetify.icons.up</v-icon>
                    <v-icon v-else>$vuetify.icons.down</v-icon>
                  </v-btn>
                </v-card-title>
                <v-expand-transition>
                  <div v-show="expertLoad.show">
                    <v-card-text>
                      <v-row>
                        <v-textarea
                          rows="5"
                          outlined
                          dense
                          v-model="expertLoad.data"
                          :placeholder="
                            $t(
                              'component.issueCredential.expertLoad.dataPlaceholder'
                            )
                          "
                        ></v-textarea>
                      </v-row>
                      <v-row>
                        <v-file-input
                          v-model="expertLoad.file"
                          :label="
                            $t(
                              'component.issueCredential.expertLoad.filePlaceholder'
                            )
                          "
                          outlined
                          dense
                          @change="uploadExpertLoadFile"
                          :accept="expertLoad.fileAccept"
                          prepend-icon="$vuetify.icons.attachment"
                        ></v-file-input>
                      </v-row>
                    </v-card-text>
                    <v-card-actions>
                      <v-layout align-start justify-start>
                        <v-radio-group
                          v-model="expertLoad.type"
                          row
                          @change="expertLoadTypeChanged"
                        >
                          <v-radio label="JSON" value="json"></v-radio>
                          <v-radio label="CSV" value="csv"></v-radio>
                        </v-radio-group>
                      </v-layout>
                      <v-layout align-end justify-end>
                        <v-bpa-button
                          color="secondary"
                          @click="clearExpertLoad()"
                          :disabled="!expertLoadEnabled"
                          >{{
                            $t(
                              "component.issueCredential.expertLoad.buttons.clear"
                            )
                          }}
                        </v-bpa-button>
                        <v-bpa-button
                          color="primary"
                          @click="parseExpertLoad()"
                          :disabled="!expertLoadEnabled"
                          >{{
                            $t(
                              "component.issueCredential.expertLoad.buttons.load"
                            )
                          }}
                        </v-bpa-button>
                      </v-layout>
                    </v-card-actions>
                  </div>
                </v-expand-transition>
              </v-card>
              <v-card v-if="credDefLoaded" outlined>
                <v-card-title class="bg-light" style="font-size: small">{{
                  $t("component.issueOobCredential.attributesTitle")
                }}</v-card-title>
                <v-card-text>
                  <v-row>
                    <v-col>
                      <v-text-field
                        v-for="field in credDef.schema.schemaAttributeNames"
                        :key="field"
                        :label="field"
                        v-model="credentialFields[field]"
                        outlined
                        dense
                        @blur="enableSubmit"
                        @keyup="enableSubmit"
                      ></v-text-field>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-card>
              <v-card class="my-4" outlined>
                <v-card-title class="bg-light" style="font-size: small">
                  {{ $t("component.issueOobCredential.options.title") }}
                </v-card-title>
                <v-card-text>
                  <v-col>
                    <v-list-item>
                      <v-list-item-content>
                        <v-list-item-title
                          class="grey--text text--darken-2 font-weight-medium"
                        >
                          {{ $t("view.addPartner.setName") }}
                        </v-list-item-title>
                      </v-list-item-content>
                      <v-list-item-action>
                        <v-text-field
                          :label="$t('view.addPartnerbyURL.labelName')"
                          v-model="alias"
                          outlined
                          dense
                        >
                        </v-text-field>
                      </v-list-item-action>
                    </v-list-item>
                  </v-col>
                  <div v-if="expertMode">
                    <v-col>
                      <v-list-item>
                        <v-list-item-content>
                          <v-list-item-title
                            class="grey--text text--darken-2 font-weight-medium"
                          >
                            {{ $t("view.addPartner.setTags") }}
                          </v-list-item-title>
                        </v-list-item-content>
                        <v-list-item-action>
                          <v-autocomplete
                            multiple
                            v-model="selectedTags"
                            :items="tags"
                            item-text="name"
                            item-value="id"
                            chips
                            deletable-chips
                          >
                          </v-autocomplete>
                        </v-list-item-action>
                      </v-list-item>
                    </v-col>
                    <v-col>
                      <v-list-item>
                        <v-list-item-content>
                          <v-list-item-title
                            class="grey--text text--darken-2 font-weight-medium"
                            >{{
                              $t("view.addPartner.trustPing")
                            }}</v-list-item-title
                          >
                        </v-list-item-content>
                        <v-list-item-action>
                          <v-switch v-model="trustPing"></v-switch>
                        </v-list-item-action>
                      </v-list-item>
                    </v-col>
                    <v-col v-if="expertMode">
                      <v-list-item>
                        <v-list-item-content>
                          <v-list-item-title
                            class="grey--text text--darken-2 font-weight-medium"
                            >{{ $t("button.useV2") }}</v-list-item-title
                          >
                        </v-list-item-content>
                        <v-list-item-action>
                          <v-switch v-model="useV2Exchange"></v-switch>
                        </v-list-item-action>
                      </v-list-item>
                    </v-col>
                  </div>
                </v-card-text>
              </v-card>
            </v-stepper-content>
            <v-stepper-content step="2">
              <qrcode-vue
                class="d-flex justify-center"
                :value="invitationUrl"
                :size="400"
                level="H"
              ></qrcode-vue>
              <v-expansion-panels class="mt-4">
                <v-expansion-panel>
                  <v-expansion-panel-header>
                    <span class="grey--text text--darken-2 font-weight-medium">
                      {{ $t("component.issueOobCredential.urlLabel") }}</span
                    >
                  </v-expansion-panel-header>
                  <v-expansion-panel-content>
                    <span class="font-weight-light">{{ invitationUrl }}</span>
                  </v-expansion-panel-content>
                </v-expansion-panel>
              </v-expansion-panels>
            </v-stepper-content>
          </v-stepper-items>
        </v-stepper>
      </v-card-text>
      <v-card-actions>
        <v-layout align-end justify-end>
          <v-bpa-button
            v-show="currentStep === 1"
            color="secondary"
            @click="cancel()"
            >{{ $t("button.cancel") }}</v-bpa-button
          >
          <v-bpa-button
            v-show="currentStep === 1"
            :loading="this.isBusy"
            color="primary"
            @click="submit()"
            :disabled="createDisabled"
            >{{ $t("button.create") }}</v-bpa-button
          >
          <v-bpa-button
            v-show="currentStep === 2"
            :loading="this.isBusy"
            color="primary"
            @click="$emit('success')"
            >{{ $t("button.close") }}</v-bpa-button
          >
        </v-layout>
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script lang="ts">
import Vue from "vue";
import { EventBus } from "@/main";
import {
  issuerService,
  IssueOobCredentialRequest,
  APICreateInvitationResponse,
  Tag,
  CredDef,
} from "@/services";
import VBpaButton from "@/components/BpaButton";
import * as textUtils from "@/utils/textUtils";
import * as CSV from "csv-string";
import QrcodeVue from "qrcode.vue";
import { ExchangeVersion } from "@/constants";

export default {
  name: "IssueCredentialIndyOob",
  components: { VBpaButton, QrcodeVue },
  props: {
    credDefId: String,
    open: Boolean,
  },
  mounted() {
    this.load();
  },
  data: () => {
    return {
      isLoading: false,
      isBusy: false,
      credDef: {},
      credential: {},
      credentialFields: {},
      currentStep: 1,
      alias: "",
      selectedTags: new Array<Tag>(),
      // Disable trust ping for invitation to mobile wallets by default
      trustPing: false,
      useV2Exchange: false,
      invitationUrl: "",
      createDisabled: true,
      expertLoad: {
        show: false,
        data: "",
        file: undefined as File,
        type: "json",
        fileAccept: "text/plain,application/json",
      },
    };
  },
  computed: {
    expertMode() {
      return this.$store.getters.getExpertMode;
    },
    credDefList: {
      get() {
        return this.$store.getters.getCredDefSelectList;
      },
    },
    credDefLoaded: {
      get() {
        return this.credDef?.schema?.schemaAttributeNames?.length;
      },
    },
    expertLoadEnabled() {
      return this.expertLoad.data?.trim().length > 0;
    },
    tags() {
      return this.$store.getters.getTags ? this.$store.getters.getTags : [];
    },
  },
  watch: {
    credDefId(value: string) {
      if (value) {
        this.credDef = this.credDefList.find(
          (credDef: CredDef) => credDef.id === value
        );
        this.credDefSelected();
      }
    },
    open(value: boolean) {
      if (value) {
        if (!this.credDef?.schema?.schemaAttributeNames) {
          this.credDef = this.credDefList.find(
            (credDef: CredDef) => credDef.id === this.$props.credDefId
          );
          this.credDefSelected();
        }
        this.clearExpertLoad();
        this.expertLoad.show = false;
      }
    },
  },
  methods: {
    async load() {
      this.isLoading = true;
      this.credDef = {};

      if (this.$props.credDefId) {
        this.credDef = this.credDefList.find(
          (credDef: CredDef) => credDef.id === this.$props.credDefId
        );
      }

      this.isLoading = false;
    },
    async issueOobCredentialOffer(): Promise<APICreateInvitationResponse> {
      let document: any = {};

      for (const x of this.credDef.schema.schemaAttributeNames)
        document[x] = "";
      Object.assign(document, this.credentialFields);

      const tags: Tag[] = this.tags.filter((tag: Tag) =>
        this.selectedTags.includes(tag.id)
      );

      const data: IssueOobCredentialRequest = {
        alias: this.alias,
        tag: tags,
        trustPing: this.trustPing,
        exchangeVersion: this.useV2Exchange
          ? ExchangeVersion.V2
          : ExchangeVersion.V1,
        credDefId: this.credDef.id,
        document: document,
      };
      try {
        const resp = await issuerService.issueOobCredentialOfferCreate(data);
        return resp.data;
      } catch (error) {
        EventBus.$emit("error", this.$axiosErrorMessage(error));
      }
    },
    async submit() {
      this.isBusy = true;
      try {
        const oobInvitation: APICreateInvitationResponse =
          await this.issueOobCredentialOffer();

        this.isBusy = false;

        if (oobInvitation) {
          EventBus.$emit(
            "success",
            this.$t("component.issueOobCredential.successMessage")
          );

          this.invitationUrl = oobInvitation.invitationUrl;
          this.credDef = {};
          this.createDisabled = true;
          this.currentStep = 2;
        }
      } catch (error) {
        this.isBusy = false;
        EventBus.$emit("error", this.$axiosErrorMessage(error));
      }
    },
    cancel() {
      this.credDef = {};
      this.credentialFields = {};
      this.alias = "";
      this.selectedTags = [];
      this.trustPing = false;
      this.clearExpertLoad();
      this.currentStep = 1;
      this.$emit("cancelled");
    },
    credDefSelected() {
      this.credentialFields = {};
      for (const x of this.credDef.schema.schemaAttributeNames)
        Vue.set(this.credentialFields, x, "");
      this.createDisabled = true;
    },
    enableSubmit() {
      let enabled = false;
      if (
        this.credDef &&
        this.credDef.schema.schemaAttributeNames &&
        this.credDef.schema.schemaAttributeNames.length > 0
      ) {
        enabled = this.credDef.schema.schemaAttributeNames.some(
          (attributeName: string) =>
            this.credentialFields[attributeName] &&
            this.credentialFields[attributeName]?.trim().length > 0
        );
      }
      this.createDisabled = !enabled;
    },
    expertLoadTypeChanged(value: string) {
      this.expertLoad.fileAccept =
        value === "json"
          ? "text/plain,application/json"
          : "text/plain,text/csv";
    },
    uploadExpertLoadFile(file: File) {
      this.expertLoad.file = file;
      if (file) {
        try {
          let reader = new FileReader();
          reader.readAsText(file, "UTF-8");
          reader.addEventListener("load", (event_) => {
            this.expertLoad.data = event_.target.result;
          });
          reader.addEventListener("error", () => {
            EventBus.$emit(
              "error",
              `${this.$t(
                "component.issueCredential.expertLoad.errorMessages.readFile"
              )} '${file.name}'.`
            );
          });
        } catch (error) {
          EventBus.$emit(
            "error",
            `${this.$t(
              "component.issueCredential.expertLoad.errorMessages.readFile"
            )} '${file.name}'. ${error.message}`
          );
        }
      }
    },
    clearExpertLoad() {
      this.expertLoad = {
        show: true,
        data: "",
        file: undefined,
        type: "json",
        fileAccept: "text/plain,application/json",
      };
    },
    parseExpertLoad() {
      if (this.expertLoad.data) {
        let object;
        let formatErrorMessage = this.$t(
          "component.issueCredential.expertLoad.errorMessages.format.json"
        );
        if (this.expertLoad.type === "json") {
          object = this.jsonToObject(this.expertLoad.data);
        } else {
          formatErrorMessage = this.$t(
            "component.issueCredential.expertLoad.errorMessages.format.csv"
          );
          object = this.csvToObject(this.expertLoad.data);
        }

        if (object) {
          let count = 0;
          for (const x of this.credDef.schema.schemaAttributeNames) {
            if (
              object[x] &&
              !(
                Object.prototype.toString.call(object[x]) ===
                  "[object Object]" ||
                Object.prototype.toString.call(object[x]) ===
                  "[object Function]"
              )
            ) {
              count = count + 1;
              Vue.set(this.credentialFields, x, object[x].toString());
            }
          }
          if (count) {
            this.enableSubmit();
          } else {
            EventBus.$emit(
              "error",
              this.$t(
                "component.issueCredential.expertLoad.errorMessages.attributes"
              )
            );
          }
        } else {
          let errorMessage = this.$t(
            "component.issueCredential.expertLoad.errorMessages.parse"
          );
          EventBus.$emit("error", `${errorMessage} ${formatErrorMessage}`);
        }
      }
    },
    jsonToObject(data: string) {
      let object;
      if (data && Object.prototype.toString.call(data) === "[object String]") {
        try {
          object = JSON.parse(data);
        } catch {
          console.log("Error converting JSON string to Object");
        }
      }
      return object;
    },
    csvToObject(data: string) {
      let object: any;
      if (data && Object.prototype.toString.call(data) === "[object String]") {
        try {
          const array: string[][] = CSV.parse(data);
          if (array?.length > 1) {
            const names = array[0];
            const values = array[1];
            const namesOk = names.every((value) =>
              textUtils.isValidSchemaAttributeName(value)
            );
            if (namesOk) {
              object = {};
              for (const [index, a] of names.entries()) {
                object[a] = values[index];
              }
            }
          }
        } catch {
          console.log("Error converting CSV string to Object");
        }
      }
      return object;
    },
  },
};
</script>
