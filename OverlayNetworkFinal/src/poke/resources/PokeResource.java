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
package poke.resources;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Hibernate.HibernateTest;

import com.google.protobuf.ByteString;



import poke.client.ClientConnection;
import poke.demo.Jab;
import poke.server.Server;
import poke.server.conf.ServerConf.Node;
import poke.server.resources.Resource;
import poke.server.resources.ResourceFactory;
import poke.server.resources.ResourceUtil;
import eye.Comm.Header.Builder;
import eye.Comm.Header.ReplyStatus;
import eye.Comm.Finger;
import eye.Comm.Header;
import eye.Comm.Payload;
import eye.Comm.Request;
import eye.Comm.Response;
//
import eye.Comm.PayloadReply;
import eye.Comm.Document;

public class PokeResource  implements Resource {
	protected static Logger logger = LoggerFactory.getLogger("server");

	public PokeResource() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see poke.server.resources.Resource#process(eye.Comm.Finger)
	 */
	public Response process(Request request) {
		// TODO add code to process the message/event received
		String tag = request.getBody().getFinger().getTag();
		logger.info("poke: " + tag);

		Response res = null;

System.out.println("-----------destination------>"+request.getBody().getFinger().getDestination());
System.out.println("-----------current---------->"+Server.getCurrentNode());
		if(request.getBody().getFinger().getDestination().equalsIgnoreCase(Server.getCurrentNode()))
		{
			logger.info("Inside IF part---->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>-------------------->");
			
			byte[] bArayy = request.getBody().getFinger().getImage().toByteArray();
			HibernateTest ht=new HibernateTest();
			ht.persistImage(request.getBody().getFinger().getTag(),bArayy);
			
			
			Response.Builder r = Response.newBuilder();
			r.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
					ReplyStatus.SUCCESS, null));
			// TODO add code to set boudy for the reply. this is were the error is coming form
			// need to have paylode set
			eye.Comm.PayloadReply.Builder pr = PayloadReply.newBuilder();
			Document.Builder docb = Document.newBuilder();
			
			r.setBody(pr.build());
			//
			logger.info("Before reply is built------------------------>");
			Response reply = r.build();
			logger.info("Did i make it here after repy? ");
			for(String node : request.getHeader().getVisitedNodeIdList()){
				logger.info("Visitednodes are --------------> "+node);
			}
			//request.getHeader().g
		//	if(request.getBody().getFinger().getDestination() == Server.getServID())
			//	logger.info("Inside current node ::::::::::::::::::::::::::");
			//else
				//logger.info("Not current node :::::::::::::::::::::::::::::");
			return reply;
		}	
		else if(!request.getHeader().getVisitedNodeIdList().contains(Server.getCurrentNode()))
		{
			
			Request.Builder r = request.toBuilder();

			eye.Comm.Header.Builder h = request.getHeader().toBuilder();
			//h.setVisitedNodes(h.getVisitedNodes()+","+Server.getCurrentNode());
			h.addVisitedNodeId(Server.getCurrentNode());
			r.setHeader(h.build());
			eye.Comm.Request req = r.build();

			//logger.info("Request.getOrigin ----------------->"+request.getHeader().get);
			System.out.println("array list size--------------------------->>>>"+Server.connectedNodes.get(Server.getCurrentNode()).size());
			//request.getHeader().getVisitedNodeIdList().add(Server.getCurrentNode());
			for (Node neighborNode : Server.connectedNodes.get(Server.getCurrentNode()))
			{
				//if(!request.getHeader().getVisitedNodeIdList().contains(neighborNode)){
				{
					Jab jab = new Jab("jab",neighborNode.getIp(),neighborNode.getPort());
					try {
						logger.info("Forwarding to :::::"+neighborNode.getPort());
						jab.forward(req);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			
				}
			}
			return null;
		}
		else
			return null;		
	}
}
