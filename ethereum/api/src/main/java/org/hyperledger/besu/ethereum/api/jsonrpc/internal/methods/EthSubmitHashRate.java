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
import org.hyperledger.besu.ethereum.blockcreation.MiningCoordinator;

import org.apache.tuweni.bytes.Bytes;

public class EthSubmitHashRate implements JsonRpcMethod {

  private final MiningCoordinator miningCoordinator;

  public EthSubmitHashRate(final MiningCoordinator miningCoordinator) {
    this.miningCoordinator = miningCoordinator;
  }

  @Override
  public String getName() {
    return RpcMethod.ETH_SUBMIT_HASHRATE.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequestContext requestContext) {
    final String hashRate = requestContext.getRequiredParameter(0, String.class);
    final String id = requestContext.getRequiredParameter(1, String.class);
    return new JsonRpcSuccessResponse(
        requestContext.getRequest().getId(),
        miningCoordinator.submitHashRate(
            id, Bytes.fromHexStringLenient(hashRate).toBigInteger().longValue()));
  }
}
