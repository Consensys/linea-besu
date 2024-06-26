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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.datatypes.rpc.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.RpcErrorType;
import org.hyperledger.besu.ethereum.blockcreation.PoWMiningCoordinator;
import org.hyperledger.besu.ethereum.mainnet.DirectAcyclicGraphSeed;
import org.hyperledger.besu.ethereum.mainnet.EpochCalculator;
import org.hyperledger.besu.ethereum.mainnet.PoWSolverInputs;

import java.util.List;
import java.util.Optional;

import com.google.common.io.BaseEncoding;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EthGetWorkTest {

  private EthGetWork method;
  private final String ETH_METHOD = "eth_getWork";
  private final String hexValue =
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

  @Mock private PoWMiningCoordinator miningCoordinator;

  @BeforeEach
  public void setUp() {
    when(miningCoordinator.getEpochCalculator())
        .thenReturn(new EpochCalculator.DefaultEpochCalculator());
    method = new EthGetWork(miningCoordinator);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(ETH_METHOD);
  }

  @Test
  public void shouldReturnCorrectResultOnGenesisDAG() {
    final JsonRpcRequestContext request = requestWithParams();
    final PoWSolverInputs values =
        new PoWSolverInputs(UInt256.fromHexString(hexValue), Bytes.fromHexString(hexValue), 0);
    final List<String> expectedValue =
        List.of(
            "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
            "0x0000000000000000000000000000000000000000000000000000000000000000",
            "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
            "0x0");
    final JsonRpcResponse expectedResponse =
        new JsonRpcSuccessResponse(request.getRequest().getId(), expectedValue);
    when(miningCoordinator.getWorkDefinition()).thenReturn(Optional.of(values));

    final JsonRpcResponse actualResponse = method.response(request);
    assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
  }

  @Test
  public void shouldReturnCorrectResultOnHighBlockSeed() {
    final JsonRpcRequestContext request = requestWithParams();
    final PoWSolverInputs values =
        new PoWSolverInputs(UInt256.fromHexString(hexValue), Bytes.fromHexString(hexValue), 30000);

    final List<String> expectedValue =
        List.of(
            "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
            "0x"
                + BaseEncoding.base16()
                    .lowerCase()
                    .encode(
                        DirectAcyclicGraphSeed.dagSeed(
                            30000, new EpochCalculator.DefaultEpochCalculator())),
            "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
            "0x7530");
    final JsonRpcResponse expectedResponse =
        new JsonRpcSuccessResponse(request.getRequest().getId(), expectedValue);
    when(miningCoordinator.getWorkDefinition()).thenReturn(Optional.of(values));

    final JsonRpcResponse actualResponse = method.response(request);
    assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
  }

  @Test
  public void shouldReturnCorrectResultOnHighBlockSeedEcip1099() {
    EpochCalculator epochCalculator = new EpochCalculator.Ecip1099EpochCalculator();
    when(miningCoordinator.getEpochCalculator()).thenReturn(epochCalculator);
    method = new EthGetWork(miningCoordinator);
    final JsonRpcRequestContext request = requestWithParams();
    final PoWSolverInputs values =
        new PoWSolverInputs(UInt256.fromHexString(hexValue), Bytes.fromHexString(hexValue), 60000);

    final List<String> expectedValue =
        List.of(
            "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
            "0x"
                + BaseEncoding.base16()
                    .lowerCase()
                    .encode(DirectAcyclicGraphSeed.dagSeed(60000, epochCalculator)),
            "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
            "0xea60");
    final JsonRpcResponse expectedResponse =
        new JsonRpcSuccessResponse(request.getRequest().getId(), expectedValue);
    when(miningCoordinator.getWorkDefinition()).thenReturn(Optional.of(values));

    final JsonRpcResponse actualResponse = method.response(request);
    assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
  }

  @Test
  public void shouldReturnErrorOnNoneMiningNode() {
    final JsonRpcRequestContext request = requestWithParams();
    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.NO_MINING_WORK_FOUND);
    when(miningCoordinator.getWorkDefinition()).thenReturn(Optional.empty());

    final JsonRpcResponse actualResponse = method.response(request);
    assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
  }

  private JsonRpcRequestContext requestWithParams(final Object... params) {
    return new JsonRpcRequestContext(new JsonRpcRequest("2.0", ETH_METHOD, params));
  }
}
