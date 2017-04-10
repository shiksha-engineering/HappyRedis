package com.happyRedis;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.json.simple.JSONObject;

/**
 * 
 * @author ankit
 * 
 *         Class to Generate Data for Redis Monitoring & generate Reports for
 *         HappyRedis
 */
public class Process {
	
	long prevCommandsProcessed = 0;
	/**
	 * Main Function
	 */
	public static void main(String s[]) {
		Process processor = new Process();

		// Hit the Redis Every 1 minute
		while (true) {
			processor.fetchDataFromRedis();
			try {
				Thread.sleep(60000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Function To fetch data from Redis
	 */
	public void fetchDataFromRedis() {
		try {
			RedisLibrary redisLib = RedisLibrary.getInstance();
			
			// Fetch Redis Metrices
			String infoStats = redisLib.getInfo(Constants.REDIS_HOST,
					Constants.REDIS_PORT, "stats");
			String memoryStats = redisLib.getInfo(Constants.REDIS_HOST,
					Constants.REDIS_PORT, "memory");
			String clientStats = redisLib.getInfo(Constants.REDIS_HOST,
					Constants.REDIS_PORT, "clients");
			String persistanceStats = redisLib.getInfo(Constants.REDIS_HOST,
					Constants.REDIS_PORT, "persistance");
			
			// Parse the data
			HashMap<String, String> infoStatsData = parseInfoData(infoStats);
			HashMap<String, String> infoMemoryData = parseInfoData(memoryStats);
			HashMap<String, String> infoClientsData = parseInfoData(clientStats);

			if (infoStatsData == null || infoMemoryData == null
					|| infoClientsData == null) {
				prevCommandsProcessed = 0;
				return;
			}
			
			// Used Memory
			String used_memory = infoMemoryData.get("used_memory");
			double used_memory_int_mb = Double.parseDouble(used_memory);
			double used_memory_human = Math.round(used_memory_int_mb
					/ (1024 * 1024));
			
			// Peak Memory
			String peak_memory = infoMemoryData.get("used_memory_peak");
			double peak_memory_int_mb = Long.parseLong(peak_memory);
			double peak_memory_human = Math.round(peak_memory_int_mb
					/ (1024 * 1024));

			String instantaneous_ops_per_sec = infoStatsData
					.get("instantaneous_ops_per_sec");
			
			
			// Throughput per 1 minute(Commands executed Per minute)
			long totalCommProcessed = Long.parseLong(infoStatsData.get("total_commands_processed"));
			long throughPut = 0;
			
			
			if(prevCommandsProcessed == 0){
				throughPut = 0;
			}else{
				throughPut = totalCommProcessed - prevCommandsProcessed;
				if(throughPut < 0){
					throughPut = 0;
				}
			}
			
			prevCommandsProcessed = totalCommProcessed;
			
			String mem_fragmentation_ratio = infoMemoryData
					.get("mem_fragmentation_ratio");
			
			
			// Hit ratio (hits/(hits+miss))
			long hits = Long.parseLong(infoStatsData.get("keyspace_hits"));
			long miss = Long.parseLong(infoStatsData.get("keyspace_misses"));
			float hitRatio = (float) hits / ((float) hits + (float) miss);
			
			// Evicted Keys
			String evicted_keys = infoStatsData.get("evicted_keys");
			
			// Total Keys
			long total_keys = redisLib.getTotalKeys(Constants.REDIS_HOST,
					Constants.REDIS_PORT);
			
			// CLients
			String connectedClients = infoClientsData.get("connected_clients");
			String rejectedConnections = infoStatsData
					.get("rejected_connections");
			String blockedClients = infoClientsData.get("blocked_clients");

			JSONObject obj = new JSONObject();

			obj.put("USED_MEMORY", used_memory);
			obj.put("USED_MEMORY_HUMAN", used_memory_human);
			obj.put("PEAK_MEMORY", peak_memory_human);
			obj.put("INSTANTANEOUS_OPS_PER_SEC", instantaneous_ops_per_sec);
			obj.put("THROUGHPUT", throughPut);
			obj.put("MEM_FRAGMENTATION_RATIO", mem_fragmentation_ratio);
			obj.put("HIT_RATIO", hitRatio);
			obj.put("EVICTED_KEYS", evicted_keys);
			obj.put("TOTAL_KEYS", "" + total_keys);
			obj.put("CONNECTED_CLIENTS", connectedClients);

			obj.put("REJECTED_CONNECTIONS", rejectedConnections);
			obj.put("KEY_MISS", miss + "");
			obj.put("BLOCKED_CLIENTS", blockedClients);

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar time = Calendar.getInstance();
			time.add(Calendar.MILLISECOND,
					-time.getTimeZone().getOffset(time.getTimeInMillis()));
			Date date = time.getTime();
			String reportDate = df.format(date);
			String[] dateObjArr = reportDate.split(" ");
			String formattedDate = dateObjArr[0] + "T" + dateObjArr[1] + "Z";
			obj.put("DATETIME", formattedDate);
			String json = obj.toJSONString();
			System.out.println(json);
			new HelperFunctions().sendJsonPostRequest(
					Constants.ELASTIC_SEARCH_URL, json, 100000);
			redisLib.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, String> parseInfoData(String data) {
		HashMap<String, String> result = new HashMap<String, String>();
		if (data == null) {
			return null;
		}
		try {
			StringTokenizer st = new StringTokenizer(data, "\n");
			while (st.hasMoreElements()) {
				String element = st.nextElement().toString();
				if (element.contains(":")) {
					String elementArray[] = element.split(":");
					result.put(elementArray[0].trim(), elementArray[1].trim());
				}
			}
			return result;
		} catch (Exception e) {
			return null;
		}

	}

}
