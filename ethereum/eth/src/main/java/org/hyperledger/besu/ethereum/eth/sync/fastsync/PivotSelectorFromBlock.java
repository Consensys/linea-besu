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
package org.hyperledger.besu.ethereum.eth.sync.fastsync;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.consensus.merge.ForkchoiceEvent;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.eth.manager.EthContext;
import org.hyperledger.besu.ethereum.eth.manager.task.WaitForPeersTask;
import org.hyperledger.besu.ethereum.eth.sync.PivotBlockSelector;
import org.hyperledger.besu.ethereum.eth.sync.tasks.RetryingGetHeaderFromPeerByHashTask;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PivotSelectorFromBlock implements PivotBlockSelector {
  private static final Logger LOG = LoggerFactory.getLogger(PivotSelectorFromBlock.class);
  private final ProtocolContext protocolContext;
  private final ProtocolSchedule protocolSchedule;
  private final EthContext ethContext;
  private final MetricsSystem metricsSystem;
  private final GenesisConfigOptions genesisConfig;
  private final Supplier<Optional<ForkchoiceEvent>> forkchoiceStateSupplier;
  private final Runnable cleanupAction;

  private long lastNoFcuReceivedInfoLog = System.currentTimeMillis();
  private static final long NO_FCU_RECEIVED_LOGGING_THRESHOLD = 60000L;

  private volatile Optional<BlockHeader> maybeCachedHeadBlockHeader = Optional.empty();

  public PivotSelectorFromBlock(
      final ProtocolContext protocolContext,
      final ProtocolSchedule protocolSchedule,
      final EthContext ethContext,
      final MetricsSystem metricsSystem,
      final GenesisConfigOptions genesisConfig,
      final Supplier<Optional<ForkchoiceEvent>> forkchoiceStateSupplier,
      final Runnable cleanupAction) {
    this.protocolContext = protocolContext;
    this.protocolSchedule = protocolSchedule;
    this.ethContext = ethContext;
    this.metricsSystem = metricsSystem;
    this.genesisConfig = genesisConfig;
    this.forkchoiceStateSupplier = forkchoiceStateSupplier;
    this.cleanupAction = cleanupAction;
  }

  @Override
  public CompletableFuture<Void> prepareRetry() {
    // nothing to do
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void close() {
    cleanupAction.run();
  }

  @Override
  public long getMinRequiredBlockNumber() {
    return genesisConfig.getTerminalBlockNumber().orElse(0L);
  }

  @Override
  public long getBestChainHeight() {
    final long localChainHeight = protocolContext.getBlockchain().getChainHeadBlockNumber();

    return Math.max(
        forkchoiceStateSupplier
            .get()
            .map(ForkchoiceEvent::getHeadBlockHash)
            .map(
                headBlockHash ->
                    maybeCachedHeadBlockHeader
                        .filter(
                            cachedBlockHeader -> cachedBlockHeader.getHash().equals(headBlockHash))
                        .map(BlockHeader::getNumber)
                        .orElseGet(
                            () -> {
                              LOG.debug(
                                  "Downloading chain head block header by hash {}", headBlockHash);
                              try {
                                return waitForPeers(1)
                                    .thenCompose(unused -> downloadBlockHeader(headBlockHash))
                                    .thenApply(
                                        blockHeader -> {
                                          maybeCachedHeadBlockHeader = Optional.of(blockHeader);
                                          return blockHeader.getNumber();
                                        })
                                    .get(20, TimeUnit.SECONDS);
                              } catch (Throwable t) {
                                LOG.debug(
                                    "Error trying to download chain head block header by hash {}",
                                    headBlockHash,
                                    t);
                              }
                              return null;
                            }))
            .orElse(0L),
        localChainHeight);
  }

  @Override
  public Optional<FastSyncState> selectNewPivotBlock() {
    final Optional<ForkchoiceEvent> maybeForkchoice = forkchoiceStateSupplier.get();
    if (maybeForkchoice.isPresent()) {
      Optional<Hash> pivotHash = getPivotHash(maybeForkchoice.get());
      if (pivotHash.isPresent()) {
        LOG.info("Selecting new pivot block: {}", pivotHash);
        return Optional.of(new FastSyncState(pivotHash.get()));
      }
    }
    if (lastNoFcuReceivedInfoLog + NO_FCU_RECEIVED_LOGGING_THRESHOLD < System.currentTimeMillis()) {
      lastNoFcuReceivedInfoLog = System.currentTimeMillis();
      LOG.info(
          "Waiting for consensus client, this may be because your consensus client is still syncing");
    }
    LOG.debug("No finalized block hash announced yet");
    return Optional.empty();
  }

  private CompletableFuture<BlockHeader> downloadBlockHeader(final Hash hash) {
    return RetryingGetHeaderFromPeerByHashTask.byHash(
            protocolSchedule, ethContext, hash, 0, metricsSystem)
        .getHeader()
        .whenComplete(
            (blockHeader, throwable) -> {
              if (throwable != null) {
                LOG.debug("Error downloading block header by hash {}", hash);
              } else {
                LOG.atDebug()
                    .setMessage("Successfully downloaded pivot block header by hash {}")
                    .addArgument(blockHeader::toLogString)
                    .log();
              }
            });
  }

  private CompletableFuture<Void> waitForPeers(final int count) {
    final WaitForPeersTask waitForPeersTask =
        WaitForPeersTask.create(ethContext, count, metricsSystem);
    return waitForPeersTask.run();
  }

  protected abstract Optional<Hash> getPivotHash(final ForkchoiceEvent forkchoiceEvent);
}
