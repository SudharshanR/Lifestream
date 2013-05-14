/*
 * copyright 2012, gash
 *
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.monitor;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eye.Comm.Management;
import eye.Comm.Network;
import eye.Comm.Network.Action;

public class HeartMonitor {
	protected static Logger logger = LoggerFactory.getLogger("monitor");

	private String host;
	private int port;
	protected ChannelFuture channel; // do not use directly call connect()!
	protected ClientBootstrap bootstrap;

	// protected ChannelFactory cf;

	protected HeartMonitor(String host, int port) {
		this.host = host;
		this.port = port;

		initTCP();
	}

	protected void release() {
		// if (cf != null)
		// cf.releaseExternalResources();
	}

	protected void initUDP() {
		NioDatagramChannelFactory cf = new NioDatagramChannelFactory(
				Executors.newCachedThreadPool());
		ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(cf);

		bootstrap.setOption("connectTimeoutMillis", 10000);
		bootstrap.setOption("keepAlive", true);

		// Set up the pipeline factory.
		bootstrap.setPipelineFactory(new MonitorPipeline());
	}

	protected void initTCP() {
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newFixedThreadPool(2)));

		bootstrap.setOption("connectTimeoutMillis", 10000);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		bootstrap.setPipelineFactory(new MonitorPipeline());

	}

	/**
	 * create connection to remote server
	 *
	 * @return
	 */
	final int maxretries = 5;
	int retrycount = 0;
	boolean connected;

	protected Channel connect() throws UnknownHostException {

	    final InetSocketAddress address = new InetSocketAddress(InetAddress.getByName("localhost"), port);
		connected = false;
		// Start the connection attempt.
		while (!connected && retrycount < 10) {
			logger.info("host :::: "+InetAddress.getLocalHost());
			logger.info("port ::::: "+port);
			logger.info("connecting to "+address);
			channel = bootstrap.connect(address);

			channel.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
 
                    if (future.isSuccess()) {
                        logger.info("Successfully connected to : " + address);
                    } else {
                        logger.error("Could not connect to : " + address);
                    }
	            }
	        });

			// wait for the connection to establish
			channel.awaitUninterruptibly();

			if (channel.isDone() && channel.isSuccess()) {
                retrycount = 0;
                connected = true;

				return channel.getChannel();
			}
			else {
                retrycount++;
                channel = null;
                connected = false;
			}
		}

		throw new RuntimeException(
			"Not able to establish connection to server");
		
	}

	public void waitForever() {
		try {

            Channel ch = connect();
            
			Network.Builder n = Network.newBuilder();
			n.setNodeId("monitor");

			logger.info("SOME MESSAGE1");
			n.setAction(Action.NODEJOIN);
			logger.info("SOME MESSAGE2");
			Management.Builder m = Management.newBuilder();
			logger.info("SOME MESSAGE3");
			m.setGraph(n.build());
			ch.write(m.build());

			while (true) {
				Thread.sleep(1000);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
		initialize the HeartMonitor
		and run it.
	*/
	public static HeartMonitor getHeartMonitor(String host, int port) {
		HeartMonitor hm = new HeartMonitor(host, port);
		return hm;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HeartMonitor hm = new HeartMonitor("localhost", 5270);
		hm.waitForever();
	}

}
