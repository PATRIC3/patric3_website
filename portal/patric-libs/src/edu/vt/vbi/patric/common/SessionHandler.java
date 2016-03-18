/*
 * Copyright 2015. Virginia Polytechnic Institute and State University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package edu.vt.vbi.patric.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SessionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandler.class);

	public static String PREFIX = "JBSess";

	private static volatile SessionHandler instance;

	private final int expiration = 60 * 60; // 1 hour

	private JedisPool pool;

	private SessionHandler() {
	}

	public static SessionHandler getInstance() {
		if (null == instance) {
			synchronized (SessionHandler.class) {
				if (null == instance) {
					instance = new SessionHandler();
					instance.connect();
				}
			}
		}
		return instance;
	}

	private void connect() {

		try (InputStream is = getClass().getClassLoader().getResourceAsStream("redis_config.properties")) {
			Properties prop = new Properties();
			if (is != null) {
				prop.load(is);
				String host = prop.getProperty("host", "localhost");
				int port = Integer.parseInt(prop.getProperty("port", "6379"));
				int timeout = Integer.parseInt(prop.getProperty("timeout", "60"));
				String password = prop.getProperty("password");
				int database = Integer.parseInt(prop.getProperty("database", "0"));
				int poolSize = Integer.parseInt(prop.getProperty("poolsize", "32"));

				JedisPoolConfig poolConfig = new JedisPoolConfig();
				poolConfig.setMaxActive(poolSize);

				pool = new JedisPool(poolConfig, host, port, timeout, password, database);
				LOGGER.debug("connecting Redis Server: {},{},{},{},{}", host, port, timeout, password, database);
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public String get(String key) {
		try {
			Jedis jedis = pool.getResource();
			String val = jedis.get(key);
			pool.returnResource(jedis);
			return val;
		}
		catch (JedisException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	public void set(String key, String val) {
		try {
			Jedis jedis = pool.getResource();
			jedis.setex(key, expiration, val);
			pool.returnResource(jedis);
		}
		catch (JedisException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}
