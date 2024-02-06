/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.transaction;

import org.hyperledger.besu.datatypes.AccessListEntry;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Transaction;
import org.hyperledger.besu.datatypes.Wei;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

// Represents parameters for a eth_call or eth_estimateGas JSON-RPC methods.
public class CallParameter {

  private final Address from;

  private final Address to;

  private final long gasLimit;

  private final Optional<Wei> maxPriorityFeePerGas;

  private final Optional<Wei> maxFeePerGas;

  private final Wei gasPrice;

  private final Wei value;

  private final Bytes payload;

  private final Optional<List<AccessListEntry>> accessList;

  public CallParameter(
      final Address from,
      final Address to,
      final long gasLimit,
      final Wei gasPrice,
      final Wei value,
      final Bytes payload) {
    this.from = from;
    this.to = to;
    this.gasLimit = gasLimit;
    this.accessList = Optional.empty();
    this.maxPriorityFeePerGas = Optional.empty();
    this.maxFeePerGas = Optional.empty();
    this.gasPrice = gasPrice;
    this.value = value;
    this.payload = payload;
  }

  public CallParameter(
      final Address from,
      final Address to,
      final long gasLimit,
      final Wei gasPrice,
      final Optional<Wei> maxPriorityFeePerGas,
      final Optional<Wei> maxFeePerGas,
      final Wei value,
      final Bytes payload,
      final Optional<List<AccessListEntry>> accessList) {
    this.from = from;
    this.to = to;
    this.gasLimit = gasLimit;
    this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    this.maxFeePerGas = maxFeePerGas;
    this.gasPrice = gasPrice;
    this.value = value;
    this.payload = payload;
    this.accessList = accessList;
  }

  public Address getFrom() {
    return from;
  }

  public Address getTo() {
    return to;
  }

  public long getGasLimit() {
    return gasLimit;
  }

  public Wei getGasPrice() {
    return gasPrice;
  }

  public Optional<Wei> getMaxPriorityFeePerGas() {
    return maxPriorityFeePerGas;
  }

  public Optional<Wei> getMaxFeePerGas() {
    return maxFeePerGas;
  }

  public Wei getValue() {
    return value;
  }

  public Bytes getPayload() {
    return payload;
  }

  public Optional<List<AccessListEntry>> getAccessList() {
    return accessList;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final CallParameter that = (CallParameter) o;
    return gasLimit == that.gasLimit
        && Objects.equals(from, that.from)
        && Objects.equals(to, that.to)
        && Objects.equals(gasPrice, that.gasPrice)
        && Objects.equals(maxPriorityFeePerGas, that.maxPriorityFeePerGas)
        && Objects.equals(maxFeePerGas, that.maxFeePerGas)
        && Objects.equals(value, that.value)
        && Objects.equals(payload, that.payload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        from, to, gasLimit, gasPrice, maxPriorityFeePerGas, maxFeePerGas, value, payload);
  }

  @Override
  public String toString() {
    return "CallParameter{"
        + "from="
        + from
        + ", to="
        + to
        + ", gasLimit="
        + gasLimit
        + ", maxPriorityFeePerGas="
        + maxPriorityFeePerGas
        + ", maxFeePerGas="
        + maxFeePerGas
        + ", gasPrice="
        + gasPrice
        + ", value="
        + value
        + ", payload="
        + payload
        + ", accessList="
        + accessList
        + '}';
  }

  public static CallParameter fromTransaction(final Transaction tx) {
    return new CallParameter(
        tx.getSender(),
        tx.getTo().orElse(null),
        tx.getGasLimit(),
        tx.getGasPrice().map(Wei::fromQuantity).orElse(Wei.ZERO),
        tx.getMaxPriorityFeePerGas().map(Wei::fromQuantity),
        tx.getMaxFeePerGas().map(Wei::fromQuantity),
        Wei.fromQuantity(tx.getValue()),
        tx.getPayload(),
        tx.getAccessList());
  }
}
