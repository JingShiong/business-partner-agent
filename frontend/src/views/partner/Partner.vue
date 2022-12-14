<!--
 Copyright (c) 2020-2022 - for information on the respective copyright owner
 see the NOTICE file and/or the repository at
 https://github.com/hyperledger-labs/business-partner-agent

 SPDX-License-Identifier: Apache-2.0
-->

<template>
  <v-container>
    <v-card class="mx-auto mb-4">
      <!-- Title Bar -->
      <v-card-title class="bg-light">
        <v-btn depressed color="secondary" icon @click="$router.go(-1)">
          <v-icon dark>$vuetify.icons.prev</v-icon>
        </v-btn>
        <span>{{ partner.name }}</span>
        <PartnerStateIndicator
          v-if="partner.state"
          v-bind:state="partner.state"
        ></PartnerStateIndicator>
        <v-chip class="ml-2" v-for="tag in partner.tag" :key="tag.id">{{
          tag.name
        }}</v-chip>
        <v-layout align-center justify-end>
          <v-btn icon disabled>
            <v-icon small dark>$vuetify.icons.identity</v-icon>
          </v-btn>
          <span
            class="grey--text text--darken-2 font-weight-medium text-caption pl-1 pr-4"
            >{{ partner.did }}</span
          >
          <v-dialog v-model="updatePartnerDialog" max-width="600px">
            <template v-slot:activator="{ on, attrs }">
              <v-btn icon v-bind="attrs" v-on="on" color="primary">
                <v-icon dark>$vuetify.icons.pencil</v-icon>
              </v-btn>
            </template>
            <UpdatePartner
              v-bind:partner="partner"
              @success="onUpdatePartner"
              @cancelled="updatePartnerDialog = false"
            />
          </v-dialog>

          <v-tooltip top>
            <template v-slot:activator="{ on, attrs }">
              <v-bpa-button
                color="primary"
                v-bind="attrs"
                v-on="on"
                icon
                @click="refreshPartner()"
              >
                <v-icon dark>$vuetify.icons.refresh</v-icon>
              </v-bpa-button>
            </template>
            <span>{{ $t("view.partner.refreshProfile") }}</span>
          </v-tooltip>

          <v-btn depressed color="red" icon @click="deletePartner()">
            <v-icon dark>$vuetify.icons.delete</v-icon>
          </v-btn>
        </v-layout>
      </v-card-title>
      <!-- Title Bar -->
      <v-progress-linear v-if="isLoading" indeterminate></v-progress-linear>

      <v-card-text>
        <template
          v-if="partnerBpaState === PartnerStates.CONNECTION_REQUEST_RECEIVED"
        >
          <v-banner two-line>
            <v-avatar slot="icon" color="white" size="40">
              <v-icon icon="$vuetify.icons.connectionAlert" color="primary">
                $vuetify.icons.connectionAlert
              </v-icon>
            </v-avatar>
            <v-row>
              <span class="font-weight-medium">
                {{ $t("view.partner.requestReceived") }}
              </span>
            </v-row>
            <v-row>{{
              $t("view.partner.requestReceivedSubtitle", { alias: this.alias })
            }}</v-row>
            <template v-slot:actions>
              <v-bpa-button color="secondary" @click="deletePartner">
                {{ $t("view.partner.removePartner") }}
              </v-bpa-button>
              <v-bpa-button color="primary" @click="acceptPartnerRequest">
                {{ $t("view.partner.acceptPartner") }}
              </v-bpa-button>
            </template>
          </v-banner>
        </template>
        <template
          v-if="partnerBpaState === PartnerStates.CONNECTION_REQUEST_SENT"
        >
          <v-banner two-line>
            <v-avatar slot="icon" color="white" size="40">
              <v-icon icon="$vuetify.icons.connectionWaiting" color="primary">
                $vuetify.icons.connectionWaiting
              </v-icon>
            </v-avatar>

            <v-row>
              <span class="font-weight-medium">{{
                $t("view.partner.requestSent")
              }}</span>
            </v-row>
            <v-row>{{ $t("view.partner.requestSentSubtitle") }}</v-row>
          </v-banner>
        </template>

        <Profile v-if="isReady" v-bind:partner="partner"></Profile>
      </v-card-text>

      <v-expansion-panels v-if="expertMode" accordion flat>
        <v-expansion-panel>
          <v-expansion-panel-header
            class="grey--text text--darken-2 font-weight-medium bg-light"
            >{{ $t("showRawData") }}</v-expansion-panel-header
          >
          <v-expansion-panel-content class="bg-light">
            <vue-json-pretty :data="rawData"></vue-json-pretty>
          </v-expansion-panel-content>
        </v-expansion-panel>
      </v-expansion-panels>
    </v-card>

    <!-- Presentation Exchanges -->
    <v-card v-if="partner.ariesSupport" class="mb-4">
      <v-card-title class="bg-light">
        {{ $t("view.partner.presentationExchanges.title") }}
        <v-layout justify-end>
          <v-bpa-button
            color="primary"
            icon
            @click="refreshPresentationRecords"
          >
            <v-icon dark>$vuetify.icons.refresh</v-icon>
          </v-bpa-button>
        </v-layout>
      </v-card-title>
      <PresentationExList
        ref="presExList"
        v-if="isReady"
        v-bind:partnerId="this.id"
        v-bind:openItemById="this.presExId"
        @changed="refreshPresentationRecords"
        @presRawData="getRecords"
      />
      <v-card-actions>
        <v-bpa-button small color="secondary" @click="sendPresentation">{{
          $t("view.partner.presentationExchanges.button.send")
        }}</v-bpa-button>
        <v-bpa-button small color="primary" @click="requestPresentation">{{
          $t("view.partner.presentationExchanges.button.request")
        }}</v-bpa-button>
      </v-card-actions>
      <v-expansion-panels v-if="expertMode" accordion flat>
        <v-expansion-panel>
          <v-expansion-panel-header
            class="grey--text text--darken-2 font-weight-medium bg-light"
            >{{ $t("showRawData") }}</v-expansion-panel-header
          >
          <v-expansion-panel-content class="bg-light">
            <vue-json-pretty :data="presentationExRecords"></vue-json-pretty>
          </v-expansion-panel-content>
        </v-expansion-panel>
      </v-expansion-panels>
    </v-card>

    <!-- Credential Exchanges -->
    <v-card v-if="partner.ariesSupport" class="mb-4">
      <v-card-title class="bg-light"
        >{{ $t("view.partner.credentialExchanges.title") }}
        <v-layout justify-end>
          <v-bpa-button
            color="primary"
            icon
            @click="refreshIssuedCredentialRecords"
          >
            <v-icon dark>$vuetify.icons.refresh</v-icon>
          </v-bpa-button>
        </v-layout>
      </v-card-title>
      <CredExList
        ref="credExList"
        v-if="isReady"
        header-role
        v-bind:partnerId="id"
        v-bind:openItemById="credExId"
        @changed="refreshIssuedCredentialRecords"
        @credRawData="getCreds"
      ></CredExList>
      <v-card-actions>
        <v-dialog v-model="issueCredentialDialog" persistent max-width="600px">
          <template v-slot:activator="{ on, attrs }">
            <v-bpa-button color="secondary" small v-bind="attrs" v-on="on">{{
              $t("view.partner.credentialExchanges.button.issueCredential")
            }}</v-bpa-button>
          </template>
          <v-card class="mx-auto">
            <v-card-title class="bg-light"
              >{{ $t("component.issueCredential.title") }}
            </v-card-title>
            <credential-type-tabs>
              <template v-slot:indy>
                <IssueCredentialIndy
                  :partnerId="id"
                  hide-title
                  @success="onCredentialIssued"
                  @cancelled="issueCredentialDialog = false"
                >
                </IssueCredentialIndy>
              </template>
              <template v-slot:json-ld>
                <IssueCredentialJsonLd
                  :partnerId="id"
                  hide-title
                  @success="onCredentialIssued"
                  @cancelled="issueCredentialDialog = false"
                ></IssueCredentialJsonLd>
              </template>
            </credential-type-tabs>
          </v-card>
        </v-dialog>
        <v-bpa-button
          style="margin-left: 8px"
          small
          color="primary"
          @click="requestCredential"
          >{{
            $t("view.partner.credentialExchanges.button.requestCredential")
          }}</v-bpa-button
        >
      </v-card-actions>
      <v-expansion-panels v-if="expertMode" accordion flat>
        <v-expansion-panel>
          <v-expansion-panel-header
            class="grey--text text--darken-2 font-weight-medium bg-light"
            >{{ $t("showRawData") }}</v-expansion-panel-header
          >
          <v-expansion-panel-content class="bg-light">
            <vue-json-pretty :data="credExRecords"></vue-json-pretty>
          </v-expansion-panel-content>
        </v-expansion-panel>
      </v-expansion-panels>
    </v-card>

    <v-dialog v-model="attentionPartnerStateDialog" max-width="600px">
      <v-card>
        <v-card-title class="headline"
          >{{ $t("view.partner.stateDialog.title", { state: partner.state }) }}
        </v-card-title>
        <v-card-text>
          {{ $t("view.partner.stateDialog.text", { state: partner.state }) }}
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-bpa-button
            color="secondary"
            @click="attentionPartnerStateDialog = false"
            >{{ $t("view.partner.stateDialog.no") }}</v-bpa-button
          >
          <v-bpa-button color="primary" @click="proceed">{{
            $t("view.partner.stateDialog.yes")
          }}</v-bpa-button>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script lang="ts">
import Profile from "@/components/Profile.vue";
import PartnerStateIndicator from "@/components/PartnerStateIndicator.vue";
import { CredentialTypes, PartnerStates } from "@/constants";
import { getPartnerState } from "@/utils/partnerUtils";
import { EventBus } from "@/main";
import {
  AriesProofExchange,
  CredEx,
  PartnerAPI,
  PartnerCredential,
  partnerService,
} from "@/services";
import CredExList from "@/components/CredExList.vue";
import PresentationExList from "@/components/PresentationExList.vue";
import UpdatePartner from "@/components/UpdatePartner.vue";
import VBpaButton from "@/components/BpaButton";
import store from "@/store";
import IssueCredentialIndy from "@/components/issue/IssueCredentialIndy.vue";
import CredentialTypeTabs from "@/components/helper/CredentialTypeTabs.vue";
import IssueCredentialJsonLd from "@/components/issue/IssueCredentialJsonLd.vue";

export default {
  name: "Partner",
  props: {
    id: String,
    presExId: String,
    credExId: String,
  },
  components: {
    IssueCredentialJsonLd,
    VBpaButton,
    Profile,
    PresentationExList,
    PartnerStateIndicator,
    CredExList,
    IssueCredentialIndy,
    UpdatePartner,
    CredentialTypeTabs,
  },
  created() {
    EventBus.$emit("title", this.$t("view.partner.title"));
    this.getPartner();
    this.$store.commit("partnerNotificationSeen", { id: this.id });
  },
  data: () => {
    return {
      isReady: false,
      isBusy: false,
      isLoading: true,
      attentionPartnerStateDialog: false,
      updatePartnerDialog: false,
      goTo: {},
      alias: "",
      did: "",
      partner: {} as PartnerAPI,
      partnerBpaState: {
        value: "",
        label: "",
      },
      rawData: {} as PartnerAPI,
      credentials: new Array<PartnerCredential>(),
      credExRecords: new Array<CredEx>(),
      presentationExRecords: new Array<AriesProofExchange>(),
      PartnerStates: PartnerStates,
      issueCredentialDialog: false,
    };
  },
  computed: {
    expertMode() {
      return this.$store.getters.getExpertMode;
    },
    isActive() {
      return this.partnerBpaState === PartnerStates.ACTIVE_OR_RESPONSE;
    },
  },
  methods: {
    proceed() {
      this.attentionPartnerStateDialog = false;
      this.$router.push(this.goTo);
    },
    // Presentations
    requestPresentation() {
      if (this.isActive) {
        this.$router.push({
          name: "RequestPresentation",
          params: {
            id: this.id,
          },
        });
      } else {
        this.attentionPartnerStateDialog = true;
        this.goTo = {
          name: "RequestPresentation",
          params: {
            id: this.id,
          },
        };
      }
    },
    sendPresentation() {
      if (this.isActive) {
        this.$router.push({
          name: "SendPresentation",
          params: {
            id: this.id,
          },
        });
      } else {
        this.attentionPartnerStateDialog = true;
        this.goTo = {
          name: "SendPresentation",
          params: {
            id: this.id,
          },
        };
      }
    },
    refreshPresentationRecords() {
      this.$refs.presExList.loadPresentationRecords();
    },
    getRecords(presExRecords: AriesProofExchange[]) {
      this.presentationExRecords = presExRecords;
    },
    refreshIssuedCredentialRecords() {
      this.$refs.credExList.loadCredentials();
    },
    getCreds(credExRecords: CredEx[]) {
      this.credExRecords = credExRecords;
    },
    requestCredential() {
      if (this.isActive) {
        this.$router.push({
          name: "RequestCredential",
          params: {
            id: this.id,
          },
        });
      } else {
        this.attentionPartnerStateDialog = true;
        this.goTo = {
          name: "RequestCredential",
          params: {
            id: this.id,
          },
        };
      }
    },
    getPartner() {
      console.log("Getting partner...");
      this.isLoading = true;
      partnerService
        .getPartnerById(this.id)
        .then((result) => {
          console.log(result);
          if (Object.prototype.hasOwnProperty.call(result, "data")) {
            this.rawData = result.data;
            this.partner = result.data;
            this.partnerBpaState = getPartnerState(this.partner);
            this.alias = this.partner.name;
            this.did = this.partner.did;
            this.isReady = true;
            this.isLoading = false;

            console.log(this.partner);
          }
        })
        .catch((error) => {
          this.isLoading = false;
          EventBus.$emit("error", this.$axiosErrorMessage(error));
        });
    },
    deletePartner() {
      partnerService
        .removePartner(this.id)
        .then((result) => {
          console.log(result);
          if (result.status === 200) {
            store.dispatch("loadPartners");
            EventBus.$emit(
              "success",
              this.$t("view.partner.eventSuccessPartnerDelete")
            );
            this.$router.push({
              name: "Partners",
            });
          }
        })
        .catch((error) => {
          EventBus.$emit("error", this.$axiosErrorMessage(error));
        });
    },
    acceptPartnerRequest() {
      partnerService
        .acceptPartnerRequest(this.id)
        .then((result) => {
          console.log(result);
          if (result.status === 200) {
            EventBus.$emit(
              "success",
              this.$t("view.partner.eventSuccessConnectionAccepted")
            );
            // allow a little time for the partner state to change, so the remove/accept panel will not be displayed
            setTimeout(() => this.getPartner(), 1000);
          }
        })
        .catch((error) => {
          EventBus.$emit("error", this.$axiosErrorMessage(error));
        });
    },
    refreshPartner() {
      this.isLoading = true;
      partnerService
        .refreshPartner(this.id)
        .then(async (result) => {
          if (
            result.status === 200 &&
            Object.prototype.hasOwnProperty.call(result, "data")
          ) {
            console.log(result.data);
            this.rawData = result.data;
            this.partner = result.data;

            if (
              Object.prototype.hasOwnProperty.call(this.partner, "credential")
            ) {
              // Show only creds other than OrgProfile in credential list
              this.credentials = this.partner.credential.filter(
                (cred: PartnerCredential) => {
                  return cred.type !== CredentialTypes.PROFILE.type;
                }
              );
            }

            this.partnerBpaState = getPartnerState(this.partner);
            this.alias = this.partner.name;
            this.did = this.partner.did;
            console.log(this.partner);
            this.isReady = true;
            this.isLoading = false;
          }
        })
        .catch((error) => {
          this.isLoading = false;
          EventBus.$emit("error", this.$axiosErrorMessage(error));
        });
    },
    onUpdatePartner() {
      this.getPartner();
      this.updatePartnerDialog = false;
    },
    onCredentialIssued() {
      this.issueCredentialDialog = false;
      this.refreshIssuedCredentialRecords();
    },
  },
};
</script>

<style scoped>
.bg-light {
  background-color: #fafafa;
}
</style>
