<!--
 Copyright (c) 2020 - for information on the respective copyright owner
 see the NOTICE file and/or the repository at
 https://github.com/hyperledger-labs/organizational-agent

 SPDX-License-Identifier: Apache-2.0
-->

<template>
  <v-container>
    <v-data-table
      :loading="isLoading"
      :hide-default-footer="items.length < 10"
      :headers="headers"
      :items="items"
      :sort-by="['updatedAt']"
      :sort-desc="[true]"
      single-select
      @click:row="openItem"
    >
      <template v-slot:[`item.indicator`]="{ item }">
        <new-message-icon
          :type="'presentation'"
          :id="item.id"
        ></new-message-icon>
      </template>
      <template v-slot:[`item.label`]="{ item }">
        {{
          item.proofRequest && item.proofRequest.name
            ? item.proofRequest.name
            : item.typeLabel
        }}
      </template>

      <template v-slot:[`item.role`]="{ item }">
        {{ item.role | capitalize }}
      </template>
      <template v-slot:[`item.state`]="{ item }">
        <span>
          {{ (item.state ? item.state.replace("_", " ") : "") | capitalize }}
        </span>
        <v-icon v-if="item.valid" color="green">$vuetify.icons.check</v-icon>
        <v-icon
          v-if="isStateVerified(item) && !item.valid && !item.problemReport"
          color="error"
          small
        >
          $vuetify.icons.connectionAlert
        </v-icon>
        <v-tooltip v-if="item.problemReport" top>
          <template v-slot:activator="{ on, attrs }">
            <v-icon color="error" small v-bind="attrs" v-on="on">
              $vuetify.icons.connectionAlert
            </v-icon>
          </template>
          <span>{{ item.problemReport }}</span>
        </v-tooltip>
      </template>
      <template v-slot:[`item.updatedAt`]="{ item }">
        {{ item.updatedAt | formatDateLong }}
      </template>
    </v-data-table>
    <v-dialog v-if="dialog" v-model="dialog" scrollable max-width="1000px">
      <v-card>
        <v-card-title class="bg-light">
          <span class="headline">Presentation Exchange</span>
          <v-layout justify-end>
            <v-btn depressed color="red" icon @click="deleteItem">
              <v-icon dark>$vuetify.icons.delete</v-icon>
            </v-btn>
          </v-layout>
        </v-card-title>
        <v-card-text>
          <v-skeleton-loader
            v-if="isWaitingForMatchingCreds"
            type="list-item-three-line"
          />
          <PresentationRecord
            v-else
            v-bind:record="record"
          ></PresentationRecord>

          <v-alert
            v-if="
              !isWaitingForMatchingCreds &&
              isStateRequestReceived &&
              !record.canBeFullfilled
            "
            dense
            border="left"
            type="warning"
          >
            Request can't be fullfilled with credentials in wallet
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-bpa-button color="secondary" @click="closeItem(record)"
            >Close</v-bpa-button
          >
          <span v-if="isStateRequestReceived">
            <v-bpa-button color="secondary" @click="decline"
              >Decline</v-bpa-button
            >
            <v-bpa-button
              :loading="isBusy"
              color="primary"
              :disabled="!isReadyToApprove"
              @click="approve"
              >Accept</v-bpa-button
            >
          </span>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import { proofExService } from "@/services";
import { EventBus } from "@/main";
import { PresentationExchangeStates, RequestTypes } from "@/constants";
import NewMessageIcon from "@/components/NewMessageIcon";
import PresentationRecord from "@/components/PresentationRecord";
import VBpaButton from "@/components/BpaButton";
export default {
  props: {
    items: Array,
    openItemById: String,
  },
  mounted() {
    // Open Item directly. Is used for links from notifications/activity
    if (this.openItemById) {
      // FIXME: items observable is typically not resolved yet. Then items is empty
      const item = this.items.find((item) => item.id === this.openItemById);
      if (item) {
        this.openItem(item);
      } else {
        // Load record separately if items have not be resolved
        proofExService.getProofExRecord(this.openItemById).then((resp) => {
          if (resp.data) {
            this.openItem(resp.data);
          }
        });
      }
    }
  },
  data: () => {
    return {
      selected: [],
      record: {},
      dialog: false,
      isBusy: false,
      isLoading: false,
      isWaitingForMatchingCreds: false,
      headers: [
        {
          text: "",
          value: "indicator",
          sortable: false,
          filterable: false,
        },
        {
          text: "Name",
          value: "label",
        },
        {
          text: "Role",
          value: "role",
        },
        {
          text: "Updated At",
          value: "updatedAt",
        },
        {
          text: "State",
          value: "state",
        },
      ],
    };
  },
  computed: {
    isStateRequestReceived() {
      return (
        this.record.state &&
        this.record.state === PresentationExchangeStates.REQUEST_RECEIVED
      );
    },
    isReadyToApprove() {
      if (Object.hasOwnProperty.call(this.record, "proofRequest")) {
        const groupsWithCredentials = RequestTypes.map((type) => {
          return Object.values(this.record.proofRequest[type]).map((group) => {
            return Object.hasOwnProperty.call(group, "selectedCredential");
          });
        });
        return groupsWithCredentials.flat().reduce((x, y) => x && y);
      } else {
        return false;
      }
    },
  },
  methods: {
    async approve() {
      const payload = this.prepareApprovePayload();
      try {
        await proofExService.approveProofRequest(this.record.id, payload);
        EventBus.$emit("success", "Presentation request accepted");
        this.dialog = false;
        this.$emit("changed");
      } catch (e) {
        EventBus.$emit("error", this.$axiosErrorMessage(e));
      }
    },
    async decline() {
      try {
        await proofExService.declineProofRequest(this.record.id);
        EventBus.$emit("success", "Presentation request declined");
        this.dialog = false;
        this.$emit("changed");
      } catch (e) {
        EventBus.$emit("error", this.$axiosErrorMessage(e));
      }
    },
    openItem(item) {
      this.record = item;
      this.dialog = true;
      this.addProofData();
      this.$emit("openItem", item);
    },
    closeItem() {
      this.dialog = false;
      this.record = {};
    },
    isStateVerified(item) {
      return item && item.state === PresentationExchangeStates.VERIFIED;
    },
    async deleteItem() {
      try {
        const resp = await proofExService.deleteProofExRecord(this.record.id);
        if (resp.status === 200) {
          const idx = this.items.findIndex(
            (item) => item.id === this.record.id
          );
          this.items.splice(idx, 1);
          EventBus.$emit("success", "Presentation record deleted");
          this.dialog = false;
        }
      } catch (e) {
        EventBus.$emit("error", this.$axiosErrorMessage(e));
      }
    },
    addProofData() {
      if (
        Object.hasOwnProperty.call(this.record, "proofRequest") &&
        Object.hasOwnProperty.call(this.record, "proofData")
      ) {
        RequestTypes.map((type) => {
          Object.entries(this.record.proofRequest[type]).map(
            ([groupName, group]) => {
              if (
                Object.hasOwnProperty.call(this.record.proofData, groupName)
              ) {
                group.proofData = this.record.proofData[groupName];
              }
            }
          );
        });
      }
    },
    prepareApprovePayload() {
      // based on ACA-Py structure https://github.com/hyperledger/aries-cloudagent-python/blob/a304568fc3238fe447eacca17d3dd6eb71545904/aries_cloudagent/protocols/present_proof/v1_0/manager.py#L244
      const payload = {
        requested_attributes: {},
        requested_predicates: {},
      };

      RequestTypes.map((type) => {
        Object.entries(this.record.proofRequest[type]).map(
          ([groupName, group]) => {
            if (type === "requestedAttributes") {
              payload.requested_attributes[groupName] = {
                cred_id: group.selectedCredential.credentialInfo.referent,
                revealed: true,
              };
            } else if (type === "requestedPredicates") {
              payload.requested_predicates = {
                cred_id: group.selectedCredential.credentialInfo.referent,
              };
            }
          }
        );
      });

      return payload;
    },
    // Checks if proof request can be fullfilled
    canBeFullfilled() {
      const canAttrsFullfilled = Object.values(
        this.record.proofRequest.requestedAttributes
      )
        .map((attrGroup) => {
          return (
            Object.hasOwnProperty.call(attrGroup, "matchingCredentials") &&
            attrGroup.matchingCredentials.length > 0
          );
        })
        .reduce((x, y) => {
          return x && y;
        }, true);

      const canPredicatesFullfilled = Object.values(
        this.record.proofRequest.requestedPredicates
      )
        .map((attrGroup) => {
          return attrGroup.matchingCredentials;
        })
        .reduce((x, y) => {
          return x && y;
        }, true);

      return canAttrsFullfilled && canPredicatesFullfilled;
    },
    getMatchingCredentials() {
      this.isWaitingForMatchingCreds = true;
      proofExService.getMatchingCredentials(this.record.id).then((result) => {
        const matchingCreds = result.data;
        // Match to request
        matchingCreds.forEach((cred) => {
          cred.presentationReferents.forEach((c) => {
            const attr = this.record.proofRequest.requestedAttributes[c];
            const pred = this.record.proofRequest.requestedPredicates[c];
            if (attr) {
              if (!Object.hasOwnProperty.call(attr, "matchingCredentials")) {
                attr.matchingCredentials = [];
              }
              const hasMatchingCred = attr.matchingCredentials.some((item) => {
                return (
                  item.credentialInfo.referent === cred.credentialInfo.referent
                );
              });
              if (!hasMatchingCred) {
                attr.matchingCredentials.push(cred);
              }
            } else if (pred) {
              if (!Object.hasOwnProperty.call(pred, "matchingCredentials")) {
                pred.matchingCredentials = [];
              }

              const hasMatchingPred = pred.matchingCredentials.some((item) => {
                return (
                  item.credentialInfo.referent === cred.credentialInfo.referent
                );
              });
              if (!hasMatchingPred) {
                pred.matchingCredentials.push(cred);
              }
            }
          });
        });

        this.record.canBeFullfilled = this.canBeFullfilled();

        this.isWaitingForMatchingCreds = false;
      });
    },
  },
  watch: {
    dialog(visible) {
      if (visible) {
        this.$store.commit("presentationNotificationSeen", {
          id: this.record.id,
        });
        if (this.record.state === "request_received") {
          this.getMatchingCredentials();
        }
      }
    },
  },
  components: {
    NewMessageIcon,
    PresentationRecord,
    VBpaButton,
  },
};
</script>