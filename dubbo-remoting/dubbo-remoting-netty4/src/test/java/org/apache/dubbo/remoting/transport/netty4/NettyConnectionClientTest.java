/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.remoting.transport.netty4;

import java.net.InetSocketAddress;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NettyConnectionClientTest {

    @AfterEach
    void clearSocksProxyProperties() {
        System.clearProperty(NettyConnectionClient.SOCKS_PROXY_HOST);
        System.clearProperty(NettyConnectionClient.SOCKS_PROXY_PORT);
    }

    @Test
    void testNoProxySettingShouldNotAddProxyHandler() {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyConnectionClient.addSocks5ProxyIfConfigured(channel.pipeline());
        Assertions.assertNull(channel.pipeline().get(Socks5ProxyHandler.class));
        channel.close();
    }

    @Test
    void testValidProxySettingShouldAddProxyHandler() {
        System.setProperty(NettyConnectionClient.SOCKS_PROXY_HOST, "127.0.0.2");
        System.setProperty(NettyConnectionClient.SOCKS_PROXY_PORT, "1081");

        InetSocketAddress socksProxyAddress = NettyConnectionClient.getSocks5ProxyAddress();
        Assertions.assertNotNull(socksProxyAddress);
        Assertions.assertEquals("127.0.0.2", socksProxyAddress.getHostString());
        Assertions.assertEquals(1081, socksProxyAddress.getPort());

        EmbeddedChannel channel = new EmbeddedChannel();
        NettyConnectionClient.addSocks5ProxyIfConfigured(channel.pipeline());
        Socks5ProxyHandler socks5ProxyHandler = channel.pipeline().get(Socks5ProxyHandler.class);
        Assertions.assertNotNull(socks5ProxyHandler);
        channel.close();
    }

    @Test
    void testInvalidOrMissingProxyPortShouldUseDefaultPort() {
        System.setProperty(NettyConnectionClient.SOCKS_PROXY_HOST, "127.0.0.3");
        InetSocketAddress defaultPortAddress = NettyConnectionClient.getSocks5ProxyAddress();
        Assertions.assertNotNull(defaultPortAddress);
        Assertions.assertEquals(NettyConnectionClient.DEFAULT_SOCKS_PROXY_PORT, defaultPortAddress.getPort());

        EmbeddedChannel channelWithMissingPort = new EmbeddedChannel();
        NettyConnectionClient.addSocks5ProxyIfConfigured(channelWithMissingPort.pipeline());
        Assertions.assertNotNull(channelWithMissingPort.pipeline().get(Socks5ProxyHandler.class));
        channelWithMissingPort.close();

        System.setProperty(NettyConnectionClient.SOCKS_PROXY_PORT, "invalid");
        InetSocketAddress invalidPortAddress = NettyConnectionClient.getSocks5ProxyAddress();
        Assertions.assertNotNull(invalidPortAddress);
        Assertions.assertEquals(NettyConnectionClient.DEFAULT_SOCKS_PROXY_PORT, invalidPortAddress.getPort());

        EmbeddedChannel channelWithInvalidPort = new EmbeddedChannel();
        NettyConnectionClient.addSocks5ProxyIfConfigured(channelWithInvalidPort.pipeline());
        Assertions.assertNotNull(channelWithInvalidPort.pipeline().get(Socks5ProxyHandler.class));
        channelWithInvalidPort.close();
    }
}
