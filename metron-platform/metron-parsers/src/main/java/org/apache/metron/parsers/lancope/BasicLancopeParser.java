/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.metron.parsers.lancope;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.metron.parsers.BasicParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class BasicLancopeParser extends BasicParser {
	// Sample Lancope Message
	// {"message":"<131>Jul 17 15:59:01 smc-01 StealthWatch[12365]: 2014-07-17T15:58:30Z 10.40.10.254 0.0.0.0 Minor High Concern Index The host's concern index has either exceeded the CI threshold or rapidly increased. Observed 36.55M points. Policy maximum allows up to 20M points.","@version":"1","@timestamp":"2014-07-17T15:56:05.992Z","type":"syslog","host":"10.122.196.201"}

	private static final Logger _LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void configure(Map<String, Object> parserConfig) {

	}

	@Override
	public void init() {

	}

	//@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> parse(byte[] msg) {

		JSONObject payload = null;
		List<JSONObject> messages = new ArrayList<>();
		try {
			
			String raw_message = new String(msg, "UTF-8");
			
			payload = (JSONObject) JSONValue.parse(raw_message);
			
			

			String message = payload.get("message").toString();
			String[] parts = message.split(" ");
			payload.put("ip_src_addr", parts[6]);
			payload.put("ip_dst_addr", parts[7]);

			String fixed_date = parts[5].replace('T', ' ');
			fixed_date = fixed_date.replace('Z', ' ').trim();

			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			Date date;

			date = formatter.parse(fixed_date);
			long timestamp = date.getTime();
			payload.put("timestamp", timestamp);

			payload.remove("@timestamp");
			payload.remove("message");
			payload.put("original_string", message);

			messages.add(payload);
			return messages;
		} catch (Exception e) {

			_LOG.error("Unable to parse message: {}", payload.toJSONString());
			return null;
		}
	}

	
}
