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
package poke.demo;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import eye.Comm.Request;

import poke.client.ClientConnection;

public class Jab {	
	private String host;
	private int port;
	private ByteString bs;
	private String tag;
	private int count;
	protected static Logger logger = LoggerFactory.getLogger("server");
	public Jab(String tag, String host, int port) {
		this.tag = tag;
		this.host = host;
		this.port = port;
		
	}

	public void run() throws IOException {
		ClientConnection cc = ClientConnection
				.initConnection(host, port);
		for (int i = 0; i < 3; i++) {
			count++;
			cc.poke(tag, count);
		}
	}

	public static void callFromClient(ByteString bs)
	{
		System.out.println("-------------->Reached Call From client-------------->");
		Jab jab = new Jab("jab","localhost",5170);
		try {
			jab.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void forward(Request request) throws IOException {
		ClientConnection cc = ClientConnection
				.initConnection(host, port);
		cc.forwardRequest(request);
	}
	
	public static void main(String[] args) {
		try {
			Jab jab = new Jab("jab","localhost",5170);
			jab.run();
			/*logger.info("Before hitting------------>");
			System.out.println("Before Hitting----------->");
			URL url = new URL("http://localhost:8080/trying/NewFile.jsp");
			URLConnection conn = url.openConnection();
			conn.connect();
			logger.info("After hitting------------>");*/
			//Thread.sleep(5000);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
