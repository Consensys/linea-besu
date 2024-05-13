package org.hyperledger.besu.plugin.services.rpc;

import org.hyperledger.besu.datatypes.rpc.JsonRpcResponse;

public interface InMemoryJsonRpcService {

  PluginRpcResponse call(String method, Object[] params);
}
