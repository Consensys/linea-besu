/*
 * Copyright contributors to Hyperledger Besu.
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
package org.hyperledger.besu.ethereum.api.jsonrpc.execution;

import org.hyperledger.besu.datatypes.rpc.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.authentication.AuthenticationService;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestId;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcUnauthorizedResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.RpcErrorType;

import java.util.Collection;

import io.opentelemetry.api.trace.Span;

public class AuthenticatedJsonRpcProcessor implements JsonRpcProcessor {

  private final JsonRpcProcessor rpcProcessor;
  private final AuthenticationService authenticationService;
  private final Collection<String> noAuthRpcApis;

  public AuthenticatedJsonRpcProcessor(
      final JsonRpcProcessor rpcProcessor,
      final AuthenticationService authenticationService,
      final Collection<String> noAuthRpcApis) {
    this.rpcProcessor = rpcProcessor;
    this.authenticationService = authenticationService;
    this.noAuthRpcApis = noAuthRpcApis;
  }

  @Override
  public JsonRpcResponse process(
      final JsonRpcRequestId id,
      final JsonRpcMethod method,
      final Span metricSpan,
      final JsonRpcRequestContext request) {
    if (authenticationService.isPermitted(request.getUser(), method, noAuthRpcApis)) {
      return rpcProcessor.process(id, method, metricSpan, request);
    }
    return new JsonRpcUnauthorizedResponse(id, RpcErrorType.UNAUTHORIZED);
  }
}
