package org.hyperledger.besu.services;

import org.hyperledger.besu.plugin.services.rpc.InMemoryJsonRpcService;
import org.hyperledger.besu.plugin.services.rpc.PluginRpcResponse;

public class InMemoryJsonRpcServiceImpl implements InMemoryJsonRpcService {

  @Override
  public PluginRpcResponse call(final String method, final Object[] params) {
    return null;
  }
}
