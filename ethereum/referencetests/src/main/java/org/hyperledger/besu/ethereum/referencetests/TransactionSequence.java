package org.hyperledger.besu.ethereum.referencetests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransactionSequence(@JsonProperty("exception") String exception,
                                  @JsonProperty("rawBytes") String rawBytes,
                                  @JsonProperty("valid") boolean valid) {
}
