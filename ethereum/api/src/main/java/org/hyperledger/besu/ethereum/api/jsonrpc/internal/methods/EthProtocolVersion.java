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
package org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods;

import org.hyperledger.besu.datatypes.rpc.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.results.Quantity;
import org.hyperledger.besu.ethereum.eth.EthProtocol;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Capability;

import java.util.OptionalInt;
import java.util.Set;

public class EthProtocolVersion implements JsonRpcMethod {

  private final Integer highestEthVersion;

  public EthProtocolVersion(final Set<Capability> supportedCapabilities) {
    final OptionalInt version =
        supportedCapabilities.stream()
            .filter(cap -> EthProtocol.NAME.equals(cap.getName()))
            .mapToInt(Capability::getVersion)
            .max();
    highestEthVersion = version.isPresent() ? version.getAsInt() : null;
  }

  @Override
  public String getName() {
    return RpcMethod.ETH_PROTOCOL_VERSION.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequestContext requestContext) {
    return new JsonRpcSuccessResponse(
        requestContext.getRequest().getId(),
        highestEthVersion == null ? null : Quantity.create(highestEthVersion));
  }
}
