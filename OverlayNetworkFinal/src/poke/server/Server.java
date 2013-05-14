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
package poke.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.AdaptiveReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.conf.JsonUtil;
import poke.server.conf.ServerConf;
import poke.server.conf.ServerConf.Node;
import poke.server.management.ManagementDecoderPipeline;
import poke.server.management.ManagementQueue;
import poke.server.management.ServerHeartbeat;
import poke.server.resources.ResourceFactory;
import poke.server.routing.ServerDecoderPipeline;
import poke.monitor.HeartMonitor;
import poke.demo.Jab;

/**
 * Note high surges of messages can close down the channel if the handler cannot
 * process the messages fast enough. This design supports message surges that
 * exceed the processing capacity of the server through a second thread pool
 * (per connection or per server) that performs the work. Netty's boss and
 * worker threads only processes new connections and forwarding requests.
 * <p>
 * Reference Proactor pattern for additional information.
 *
 * @author gash
 *
 */
public class Server {
	protected static Logger logger = LoggerFactory.getLogger("server");

	protected static final ChannelGroup allChannels = new DefaultChannelGroup(
			"server");
	protected static HashMap<Integer, Bootstrap> bootstrap = new HashMap<Integer, Bootstrap>();
	protected static HashMap<String, HeartMonitor> hbMonitor = new HashMap<String, HeartMonitor>();
	protected ChannelFactory cf, mgmtCF;
	protected ServerConf conf;
	protected static String currentNode;
	public static HashMap<String,ArrayList<Node>> connectedNodes=new HashMap<String,ArrayList<Node>>();
	public static ArrayList<Node> neighborNodes = new ArrayList<Node>();
	

	protected ServerHeartbeat heartbeat;


	/**
	 * static because we need to get a handle to the factory from the shutdown
	 * resource
	 */
	public static void shutdown() {
		try {
			ChannelGroupFuture grp = allChannels.close();
			grp.awaitUninterruptibly(5, TimeUnit.SECONDS);
			for (Bootstrap bs : bootstrap.values())
				bs.getFactory().releaseExternalResources();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		logger.info("Server shutdown");
		System.exit(0);
	}

	/**
	 * initialize the server with a configuration of it's resources
	 *
	 * @param cfg
	 */
	public Server(File cfg, String serverid) {
		setCurrentNode(serverid);
		init(cfg,serverid);
	}

	public static String getCurrentNode() {
		return currentNode;
	}

	public static void setCurrentNode(String currentNode) {
		Server.currentNode = currentNode;
	}

	private void init(File cfg, String serverid) {
		// resource initialization - how message are processed
		BufferedInputStream br = null;
		try {
			byte[] raw = new byte[(int) cfg.length()];
			br = new BufferedInputStream(new FileInputStream(cfg));
			br.read(raw);
			conf = JsonUtil.decode(new String(raw), ServerConf.class);

			if (conf == null){
				logger.info("ServerConf was not loaded from file..");
				System.exit(5);
			}
			else
			{
				logger.info("ServerConf was loaded from file..");
			}

			ResourceFactory.initialize(conf);
		} catch (Exception e) {
		}

		// communication - external (TCP) using asynchronous communication
		cf = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		// communication - internal (UDP)
		// mgmtCF = new
		// NioDatagramChannelFactory(Executors.newCachedThreadPool(),
		// 1);

		// internal using TCP - a better option
		mgmtCF = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newFixedThreadPool(2));

	}

	public void release() {
		if (heartbeat != null)
			heartbeat.release();
	}

	private void createPublicBoot(int port) {
		// construct boss and worker threads (num threads = number of cores)

		ServerBootstrap bs = new ServerBootstrap(cf);

		// Set up the pipeline factory.
		bs.setPipelineFactory(new ServerDecoderPipeline());

		// tweak for performance
		bs.setOption("child.tcpNoDelay", true);
		bs.setOption("child.keepAlive", true);
		bs.setOption("receiveBufferSizePredictorFactory",
				new AdaptiveReceiveBufferSizePredictorFactory(1024 * 2,
						1024 * 4, 1048576));

		bootstrap.put(port, bs);

		// Bind and start to accept incoming connections.
		Channel ch = bs.bind(new InetSocketAddress(port));
		allChannels.add(ch);

		// We can also accept connections from a other ports (e.g., isolate read
		// and writes)

		logger.info("Starting server, listening on port = " + port);
	}

	private void createManagementBoot(int port) {
		// construct boss and worker threads (num threads = number of cores)

		// UDP: not a good option as the message will be dropped
		// ConnectionlessBootstrap bs = new ConnectionlessBootstrap(mgmtCF);

		// TCP
		ServerBootstrap bs = new ServerBootstrap(mgmtCF);

		// Set up the pipeline factory.
		bs.setPipelineFactory(new ManagementDecoderPipeline());

		// tweak for performance
		// bs.setOption("tcpNoDelay", true);
		bs.setOption("child.tcpNoDelay", true);
		bs.setOption("child.keepAlive", true);

		bootstrap.put(port, bs);

		// Bind and start to accept incoming connections.
		Channel ch = bs.bind(new InetSocketAddress(port));
		allChannels.add(ch);

		logger.info("Starting server, listening on port = " + port);
	}

	// Changed
	protected void run(String serverid) throws IOException {

		logger.info("Server[" + serverid + "] started...");

		Node currentNode = conf.findNodeById(serverid);
		if (currentNode != null){
			int port = currentNode.getPort();
			int mport = currentNode.getMgmtport();

			// storage initialization
			// TODO storage init


			// start communication
			createPublicBoot(port);
			createManagementBoot(mport);

			// start management
			ManagementQueue.startup();

			// ------------------------------
			// Register the HB monitor and start monitoring
			// 1) get the list of neighbor
			
			
			
			for (String neighborid : currentNode.getNeighbor())
			{
				Node neighbor = conf.findNodeById(neighborid);	
				neighborNodes.add(neighbor);
				System.out.println("Printing Size Inside-------------------->"+neighborNodes.size());
			}
			
			
			logger.info("Out of loop---------------------->");
			connectedNodes.put(currentNode.getNodeid(), neighborNodes);
			System.out.println("\n --------->"+connectedNodes.size());
			
			
			for (String neighborid : currentNode.getNeighbor())
			{
				Node neighbor = conf.findNodeById(neighborid);	
				
			
				// 2) register the HB monitor and start HB monitor
				logger.info("neighbor.getNodeid() ========== "+neighbor.getNodeid());
				if(neighbor.getIp()==null)
					logger.info("neighbor.getIp() IS NULL :::::::::::::::::::::::");
				else
					logger.info("neighbor.getIp() NOT NULL :::::::::::::::::::::::");	
				HeartMonitor monitor = HeartMonitor.getHeartMonitor(neighbor.getIp(), neighbor.getMgmtport());
				hbMonitor.put(neighbor.getNodeid(), monitor);

				// 3) start monitoring
				monitor.waitForever();
			}
			/*logger.info("Out of loop---------------------->");
			connectedNodes.put(currentNode.getNodeid(), neighborNodes);
			System.out.println("\n --------->"+connectedNodes.size());*/

			// Start my heart beat
			heartbeat = ServerHeartbeat.getInstance(currentNode.getNodeid());
			heartbeat.start();
			logger.info("Server ready");


			/*// send Jab to all of the neighbors
			for (String neighborid : currentNode.getNeighbor())
			{
				Node neighbor = conf.findNodeById(neighborid);
				logger.info("bEFORE POPULATING HASH MAP----------------------->");
				//neighborNodes.add(neighbor);
				
				Jab jab = new Jab("Find server",neighbor.getIp(),neighbor.getPort());
				jab.run();
							
				// TODO : 1) set timeout.
				// *** How to set the timeout for message ***
				// ChannelPipeline p = ...;
				// Timer timer = new HashedWheelTimer();
				// p.addLast("timeout", new ReadTimeoutHandler(timer, 30));
				// p.addLast("handler", new MyHandler());
				// To shut down, call releaseExternalResources() or Timer.stop().

				// TODO : 2) change Jab/create new meesage to request an image
				// this may require us to change the protobuf message structure as well
				
			}  */  
			/*logger.info("Out of loop---------------------->");
			connectedNodes.put(currentNode.getNodeid(), neighborNodes);
			System.out.println("\n --------->"+connectedNodes.size());*/
		}
		else
		{
			logger.info("ERROR: Server[" + serverid + "] could not be started...");
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: java "
					+ Server.class.getClass().getName() + " conf-file server-id");
			System.exit(1);
		}

		File cfg = new File(args[0]);
		if (!cfg.exists()) {
			Server.logger.error("configuration file does not exist: " + cfg);
			System.exit(2);
		}

		String serverid = args[1];
		if (serverid.equals("")) {
			Server.logger.error("serverid should be specified when starting your server");
			System.exit(3);
		}
		else
		{
			System.out.println("server id = " + serverid);
		}

		Server svr = new Server(cfg, serverid);
		svr.run(serverid);
	}
}
