package org.catchservices.catchme;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.util.yaml.Configuration;
import com.sk89q.worldguard.util.yaml.ConfigurationNode;

public class CatchLang {

	/* prefix */
	public static final String prefix = "[CatchMe]";
	public static final String version = "0.1";
	
	/* System messages  */
	public static final String sys_puglinDisabled = "Plugin disabled";
	public static final String sys_worldguardDisabled = "WorldGuard disabled, CatchMe can't be used";
	public static final String sys_catchmeEnabled = " v"+version+" Enabled";	
	public static final String sys_failCatchConfigRegion = "Failed to load CatchMe region config file";
	public static final String sys_failWorldguardConfigUsers = "Failed to load WorldGuard users config file";
	public static final String sys_loadCatchRegionSuccess = "Load catching areas succeed";
	public static final String sys_group = "group";
	public static final String sys_control = "controls";
	public static final String sys_addMoneyToGroup = "Add money to group";
	public static final String sys_failLangConfigLoad = "Failed to load lang config file";
	public static final String sys_langConfigLoaded = "Lang config file loaded";
	public static final String sys_failLangFoundFile = "Lang file not found";
	public static final String sys_notFound = "not found";
	public static final String sys_found = "found";
	
	/* Users messages */
	public static final String usr_receiveMoney = "You receive";
	public static final String usr_nowControls = "now controls";
	public static final String usr_from = "from";
	public static final String usr_enterCatchzone = "You enter the catchzone";
	public static final String usr_controlledBy = "controlled by";
	
	private static Map<String, String> configLangList = new HashMap<String, String>();
	
	public static void loadLangList() {
		
		configLangList.put("receive-money", usr_receiveMoney);
		configLangList.put("now-controls", usr_nowControls);
		configLangList.put("from", usr_from);
		configLangList.put("enter-catchzone", usr_enterCatchzone);
		configLangList.put("controlled-by", usr_controlledBy);
	}
	
	public static void loadLangFile(JavaPlugin root) {
		
		
		loadLangList();
		
		/* load at CatchMe/lang/lang.yml */
		File file = new File(root.getDataFolder(), "lang" + File.separator + "lang.yml");
		
		if(!file.exists()) {
			sysMess(sys_failLangFoundFile);
			return;
		}
		
		Configuration config = new Configuration(file);
		
		try {
			config.load();
		} catch (IOException e) {
			sysMess(sys_failLangConfigLoad);
			return;
		}

		ConfigurationNode langData = config.getNode("lang");
		
		if(langData != null) {
			
			for(String k : configLangList.keySet()) {
				
				String wordData = langData.getString(k);
				
				if(wordData != null) {
					configLangList.put(k, wordData);
				}
			}
		}
		
		sysMess(sys_langConfigLoaded);
	}
	
	public static void sysMess(String mess) {
		
		System.out.println(prefix+" "+mess);
	}
	
	public static void playerMess(Player p, String mess) {
		
		p.sendMessage(mess);
	}
	
	public static void broadMess(Server serv, String mess) {
		
		serv.broadcastMessage(mess);
	}

	public static String get(String key) {
		
		return configLangList.get(key);
	}
	
	public static void put(String key, String value) {
		
		configLangList.put(key, value);
	}
}
