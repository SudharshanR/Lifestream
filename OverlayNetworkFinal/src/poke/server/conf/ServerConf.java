
package poke.server.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerConf{

	protected static Logger logger = LoggerFactory.getLogger("ServerConf");

	// this may get error.
	private static String serverid;
	public void setServerid(String serverid){
		this.serverid=serverid;
	}
	public static String getServerid(){
		return serverid;
	}




   	private List<Node> node;
   	private List<ResourceConf> routing;

	public List<ResourceConf> getRouting() {
		return routing;
	}

	public void setRouting(List<ResourceConf> conf) {
		this.routing = conf;
	}

 	public List<Node> getNode(){
		logger.info("Node getter..");
		return this.node;
	}
	public void setNode(List<Node> node){
		logger.info("Node setter..");
		this.node = node;
	}

	//-------------------------------------------------------------------
	//		UTIL functions
	//-------------------------------------------------------------------
	private volatile HashMap<String, Node> idToNode;
	private volatile HashMap<Integer, ResourceConf> idToResource;

	public ResourceConf findResourceById(int id) {
		return resourceMap().get(id);
	}

	public Node findNodeById(String id) {
		logger.info("Looking for the node by id..");
		return serverMap().get(id);
	}


	private HashMap<Integer, ResourceConf> resourceMap() {
		if (idToResource != null)
			return idToResource;

		if (idToResource == null) {
			logger.info("Populating resource map..");

			synchronized (this) {
				if (idToResource == null) {
					idToResource = new HashMap<Integer, ResourceConf>();
					if (routing != null) {
						for (ResourceConf entry : routing) {
							idToResource.put(entry.id, entry);
						}
					}
				}
			}
		}

		return idToResource;
	}


	private HashMap<String, Node> serverMap() {
		if (idToNode != null)
			return idToNode;

		if (idToNode == null) {
			logger.info("....There is no server map ready yet.");
			logger.info("....Populating server map..");

			synchronized (this) {
				if (idToNode == null) {
					idToNode = new HashMap<String, Node>();
					if (node != null) {
						for (Node entry : node) {
							logger.info("..........Server " + entry.nodeid + " was added to map..");
							idToNode.put(entry.getNodeid(), entry);
						}
					}
				}
			}
		}

		return idToNode;
	}

	public void addResource(ResourceConf entry) {
		if (entry == null)
			return;
		else if (routing == null)
			routing = new ArrayList<ResourceConf>();

		routing.add(entry);
	}

	public void addServer(Node entry) {
		if (entry == null)
			return;
		else if (node == null)
			node = new ArrayList<Node>();

		node.add(entry);
	}

	//-------------------------------------------------------------------

	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class Node{
		private String nodeid;
		private String ip;
		private int port;
		private int mgmtport;
		private String storage;
		private List<String> neighbor;

		public Node() {}

		public Node(String nodeid, String ip, int port, int mgmtport, String storage) {
			this.nodeid = nodeid;
			this.ip = ip;
			this.port = port;
			this.mgmtport = mgmtport;
			this.storage = storage;
			this.neighbor = neighbor;
		}

		public String getIp(){
			return this.ip;
		}
		public void setIp(String ip){
			this.ip = ip;
		}
		public String getNodeid(){
			return this.nodeid;
		}
		public void setNodeid(String nodeid){
			this.nodeid = nodeid;
		}
		public int getPort(){
			return this.port;
		}
		public void setPort(int port){
			this.port = port;
		}
		public int getMgmtport(){
			return this.mgmtport;
		}
		public void setMgmtport(int mgmtport){
			this.mgmtport = mgmtport;
		}
		public String getStorage(){
			return this.storage;
		}
		public void setStorage(String storage){
			this.storage = storage;
		}
		public List<String> getNeighbor(){
			return this.neighbor;
		}
		public void setNeighbor(List<String> neighbor){
			this.neighbor = neighbor;
		}
	}


	/**
	 * command (request) delegation
	 *
	 * @author gash1
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class ResourceConf {
		private int id;
		private String name;
		private String clazz;
		private boolean enabled;

		public ResourceConf() {
		}

		public ResourceConf(int id, String name, String clazz) {
			this.id = id;
			this.name = name;
			this.clazz = clazz;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getClazz() {
			return clazz;
		}

		public void setClazz(String clazz) {
			this.clazz = clazz;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
}
