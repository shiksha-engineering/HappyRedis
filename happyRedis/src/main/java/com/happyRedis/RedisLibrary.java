package com.happyRedis;

import java.util.ArrayList;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Slowlog;

public class RedisLibrary {
	
	private RedisLibrary(){}
	private static RedisLibrary redisLib;
	Jedis jedis;
	
	// Get Instance for Redis
	public static synchronized RedisLibrary getInstance(){
		if(redisLib == null){
			return new RedisLibrary();
		}else{
			return redisLib;
		}
	}
	
	/**
	 * Function to get Redis Connection
	 * @param host
	 * @param port
	 * @return Jedis connection object
	 */
	public Jedis getJedis(String host,int port){
		if(jedis == null){
			jedis = new Jedis(host, port);
			
		}
		return jedis;
	}
	
	
	
	public String getInfo(String host, int port,String command){
		try{
			Jedis jedis = getJedis(host, port);
			String r = jedis.info(command);
			return r;
		}
		catch(JedisConnectionException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public long getTotalKeys(String host, int port){
		try{
			Jedis jedis = getJedis(host, port);
			long r = jedis.dbSize();
			return r;
		}
		catch(JedisConnectionException e){
			return -1;
		}
	}
	
	
	
	public void closeConnection(){
		try{
			jedis.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
