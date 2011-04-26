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
	public static final String version = "0.3.0";
	
	/* System messages  */
	public static final String sys_pluginDisabled = "Plugin disabled";
	public static final String sys_worldguardDisabled = "WorldGuard disabled, CatchMe can't be used";
	public static final String sys_permissionsDisabled = "Permissions disabled, Permissions groups not used in CatchMe";
	public static final String sys_iconomyDisabled = "Permissions disabled, money system not used in CatchMe";;
	public static final String sys_listsDisabled = "Lists disabled, Lists groups not used in CatchMe";
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
	public static final String sys_addedSuccess = "added with success";
	public static final String sys_removedSuccess = "removed with success";
	public static final String sys_updatedSuccess = "updated with success";
	public static final String sys_errorSavingConfig = "Error saving config";
	public static final String sys_areaFoundIn = "area(s) found in";
	
	/* Users messages */
	public static final String usr_receiveMoney = "You receive";
	public static final String usr_nowControls = "now controls";
	public static final String usr_from = "from";
	public static final String usr_enterCatchzone = "You enter the catchzone";
	public static final String usr_controlledBy = "controlled by";
	
	public static final String usr_usageInfo = "/cm info <area_name>";
	public static final String usr_usageAddarea = "/cm addarea <area_name>";
	public static final String usr_usageRemovearea = "/cm removearea <area_name>";
	public static final String usr_usageAddgroup = "/cm addgroup <area_name> <group_name>";
	public static final String usr_usageRemovegroup = "/cm removegroup <area_name> <group_name>";
	public static final String usr_usageFlag = "/cm flag <area_name> <flag_name> <value>";
	public static final String usr_usageSave = "/cm save";
	public static final String usr_usageList = "/cm list [#page]";
	public static final String usr_saveSuccess = "CatchMe areas saved";
	
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
