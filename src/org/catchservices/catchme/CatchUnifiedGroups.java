package org.catchservices.catchme;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mcteam.factions.FPlayer;
import org.mcteam.factions.Faction;

import com.herocraftonline.dthielke.lists.PrivilegedList;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldguard.util.yaml.Configuration;

public class CatchUnifiedGroups {

	public static String[] getGroups(Player player) {
		
		List<String> grouplist = new ArrayList<String>();
		
		if(catchme.plugin_perms != null) {
			for(String group : catchme.plugin_worldguard.getGroups(player)) {
				grouplist.add(group);
			}
		}
		
		if(catchme.plugin_lists != null)
		{
			for(String group : getListsGroups(player)) {
				grouplist.add(group);
			}
		}
		
		if(catchme.plugin_factions != null)
		{
			FPlayer fp = FPlayer.find(player.getName());
			Faction f = fp.getFaction();
			
			if(f != null) {
				grouplist.add(f.getTag());
			}
		}
		
		String[] ret = {};
		ret = grouplist.toArray(ret);
		
		return ret;
	}
	
	private static String[] getListsGroups(Player player) {
	  
		List<String> groupsl = new ArrayList<String>();
		
		for(PrivilegedList pl : catchme.plugin_lists.getLists(player.getName())) {

			groupsl.add(pl.getName());
		}
		
		String[] groups = {};
		groups = groupsl.toArray(groups);
		return groups;
	}

	public static boolean inGroup(Player p, String actualGroup) {

		if(catchme.plugin_perms != null) {
			if(catchme.plugin_worldguard.inGroup(p, actualGroup)) {
				return true;
			}
		}
		
		if(catchme.plugin_lists != null) {
			if(inListsGroup(p, actualGroup)) {
				return true;
			}
		}
		
		if(catchme.plugin_factions != null) {
			if(inFactionsGroup(p, actualGroup)) {
				return true;
			}
		}
		
		return false;
		
	}

	private static boolean inListsGroup(Player p, String actualGroup) {

		PrivilegedList pl = catchme.plugin_lists.getList(actualGroup);
		
		if(pl != null) {
			
			return pl.contains(p.getName());
		}
		
		return false;
	}
	
	private static boolean inFactionsGroup(Player p, String actualGroup) {
		
		Faction f = Faction.findByTag(actualGroup);
		
		if(f != null) {
			FPlayer fp = FPlayer.find(p.getName());

			if(fp != null) {
				return f.isInvited(fp);
			}
		}
		
		return false;
	}

	public static List<String> getPlayersGroup(String actualGroup) {
		
		
		List<String> playerslist = new ArrayList<String>();
		
		if(catchme.plugin_perms != null) {
			playerslist.addAll(getPlayersWorldGuardGroup(actualGroup));
		}
		
		if(catchme.plugin_lists != null) {
			playerslist.addAll(catchme.plugin_lists.getList(actualGroup).getUsers().keySet());
		}
		
		if(catchme.plugin_factions != null) {
			playerslist.addAll(getPlayersFactionsGroup(actualGroup));
		}
		
		return playerslist;
	}

	private static List<String> getPlayersFactionsGroup(String actualGroup) {
		
		List<String> ret = new ArrayList<String>();
		
		Faction f = Faction.findByTag(actualGroup);
		
		if(f != null) {
		
			ArrayList <FPlayer> fp = f.getFPlayers();
			for(FPlayer p : fp) {
				ret.add(p.getName());
			}
		}
		
		return ret;
	}
	
	private static List<String> getPlayersWorldGuardGroup(String actualGroup) {

		List<String> ret = new ArrayList<String>();
		/* Load catching areas from WorldGuard */
		List<World> lw = catchme.bukkit_server.getWorlds();
		
		Iterator<World> iw = lw.iterator();
		
		while(iw.hasNext()) {
			
			World world = iw.next();
			
			/* search region's flags */
			Permissions perm = catchme.plugin_perms;
			File file = new File(perm.getDataFolder(), world.getName() + ".yml");
			Configuration config = new Configuration(file);
			
			try {
				config.load();
			} catch (IOException e) {
				CatchLang.sysMess(CatchLang.sys_failWorldguardConfigUsers);
				return ret;
			}

			List<String> usersData = config.getKeys("users");

			if(usersData != null) {
				
				for(String user : usersData) {
					
					Player p = catchme.bukkit_server.getPlayer(user);
					
					
					if(p != null && CatchUnifiedGroups.inGroup(p, actualGroup)) {
						ret.add(user);
					}
				}
			}
		}
		
		return ret;
	}

}
