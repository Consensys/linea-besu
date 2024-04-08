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
package org.hyperledger.besu.ethereum.blockcreation;

import org.hyperledger.besu.ethereum.core.BlockHeader;

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBlockScheduler {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractBlockScheduler.class);
  private static final long DEFAULT_EARLY_START_MS = 500;

  protected final Clock clock;

  protected AbstractBlockScheduler(final Clock clock) {
    this.clock = clock;
  }

  public BlockCreationTimeResult waitUntilNextBlockCanBeMined(final BlockHeader parentHeader)
      throws InterruptedException {
    final BlockCreationTimeResult result = getNextTimestamp(parentHeader);

    LOG.trace("{}", result);

    final long delay = (result.timestampForHeader() * 1000) - System.currentTimeMillis();
    LOG.trace("Start creation delay {}", delay);
    if (delay > DEFAULT_EARLY_START_MS) {
      Thread.sleep(delay - DEFAULT_EARLY_START_MS);
      LOG.trace("Start creation slept {}", delay - DEFAULT_EARLY_START_MS);
    }

    return result;
  }

  public abstract BlockCreationTimeResult getNextTimestamp(final BlockHeader parentHeader);

  public record BlockCreationTimeResult(
      long timestampForHeader, long earliestBlockTransmissionMillis) {
    @Override
    public String toString() {
      return "BlockCreationTimeResult{"
          + "timestampForHeader="
          + timestampForHeader
          + "("
          + Instant.ofEpochSecond(timestampForHeader)
          + ")"
          + ", earliestBlockTransmissionMillis="
          + earliestBlockTransmissionMillis
          + "("
          + Instant.ofEpochMilli(earliestBlockTransmissionMillis)
          + ")"
          + '}';
    }
  }
}
