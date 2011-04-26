package org.catchservices.catchme;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.yaml.Configuration;
import com.sk89q.worldguard.util.yaml.ConfigurationNode;

public class CatchRegions {

	public static final StateFlag CATCHING_AREA = new StateFlag("catching-area", false);
	public static final IntegerFlag CATCHING_DURATION = new IntegerFlag("catching-duration");
	public static final IntegerFlag CATCHING_TIME_CATCH = new IntegerFlag("catching-time-catch");
	public static final IntegerFlag CATCHING_PERIOD = new IntegerFlag("catching-period");
	public static final IntegerFlag CATCHING_MONEY_AMOUNT = new IntegerFlag("catching-money-amount");
	
	public static final Flag<?>[] flagsCatchList = new Flag<?>[] {
		CATCHING_AREA, CATCHING_DURATION, CATCHING_DURATION, CATCHING_TIME_CATCH, CATCHING_PERIOD, CATCHING_MONEY_AMOUNT
	};
	
	public catchme parent;
	private Map<String, HashMap<ProtectedRegion, CatchArea>> catchareas = new HashMap<String, HashMap<ProtectedRegion, CatchArea>>();
	private List<Player> playersInAreas;
	
	private Map<String, Configuration> config_map;
	private RegionManager regionmanager;
	
	public CatchRegions(catchme parent) {
		
		this.parent = parent;
		playersInAreas = new ArrayList<Player>();
		config_map = new HashMap<String, Configuration>();
		
		loadCatchingAreas();
	}
	
	public void playerLookupCatchingArea(PlayerEvent event) {
		
		Location loc = event.getPlayer().getLocation();
		Player player = event.getPlayer();
		String world = loc.getWorld().getName();
		
		/* Test if located in a catching area */
		
		Vector v = new Vector(loc.getX(), loc.getY(), loc.getZ());
		
		Iterator<ProtectedRegion> irs = catchareas.get(world).keySet().iterator();
		
		ProtectedRegion region = null;
		boolean isACatchingArea = false;
		
		while(irs.hasNext() && !isACatchingArea) {
			
			region = irs.next();
		
			if(region.contains(v)) {
				
				isACatchingArea = true;
			}
		}
		
		/* Location is a catching area ? */
		CatchArea area = null;
		
		if(isACatchingArea) {
			
			area = catchareas.get(world).get(region);
			
			playerEnteringCatchArea(player, area);
		}	
		
		/* Test if leaving a catching area */
		playerLeavingCatchArea(world, player, area);	
	}

	public void playerLeavingCatchArea(String world, Player player, CatchArea area) {
		/* Look if */
		if(playersInAreas.contains(player)) {
			
			String[] groups = CatchUnifiedGroups.getGroups(player);
				
			for(CatchArea a : catchareas.get(world).values()) {
					
				if(area == null || area != a)
					a.checkRemovePlayer(player, groups);
			}
				
			if(area == null)
				playersInAreas.remove(player);
		}
	}

	public void playerEnteringCatchArea(Player player, CatchArea area) {

		/* Look for player groups */
		String[] groups = CatchUnifiedGroups.getGroups(player);
		
		if(!playersInAreas.contains(player)) {
			playersInAreas.add(player);
		}
		
		area.checkAddPlayer(player, groups);
	}

	public void playerQuitCatchingArea(PlayerQuitEvent event) {
		
		playerLeavingCatchArea(event.getPlayer().getLocation().getWorld().getName(),
								event.getPlayer(), null);
	}

	public void loadCatchingAreas() {
		
		/* Load catching areas from WorldGuard */
		List<World> lw = catchme.bukkit_server.getWorlds();
		
		Iterator<World> iw = lw.iterator();
		
		while(iw.hasNext()) {
			
			World world = iw.next();
			catchareas.put(world.getName(), new HashMap<ProtectedRegion, CatchArea>());
			
			regionmanager = catchme.plugin_worldguard.getGlobalRegionManager().get(world);
			
			/* search region's flags */
			
			File file = new File(parent.getDataFolder(), "worlds" + File.separator + world.getName() + File.separator + "regions.yml");
			
			if(!file.exists()) {
				CatchLang.sysMess(parent.getDataFolder() + File.separator + "worlds" + File.separator + world.getName() 
						+ File.separator + "regions.yml" + " "+ CatchLang.sys_notFound);
				continue;
			}
			else
			{
				CatchLang.sysMess(parent.getDataFolder() + File.separator + "worlds" + File.separator + world.getName() 
						+ File.separator + "regions.yml" + " "+ CatchLang.sys_found);
			}
			
			Configuration config = new Configuration(file);
			config_map.put(world.getName(), config);
			
			try {
				config.load();
			} catch (IOException e) {
				catchme.loadSuccess = false;
				CatchLang.sysMess(CatchLang.sys_failCatchConfigRegion);
				return;
			}

			Map<String, ConfigurationNode> regionsData = config.getNodes("regions");
			if (regionsData != null) {
			
				for(String regname : regionsData.keySet()) {
				
					ProtectedRegion region = regionmanager.getRegion(regname);
			
					if(region == null)
						break;
						
					ConfigurationNode regionData = regionsData.get(regname);
					ConfigurationNode flagsData = regionData.getNode("flags");
				
					if(flagsData != null) {
						
						CatchArea area = new CatchArea(region.getId());
						
						for (Flag<?> flag : flagsCatchList) {
				            Object o = flagsData.getProperty(flag.getName());
				            if (o != null) {
				            	area.setFlag(flag, o);
			            	}
			        	}
						
						/* Add groups of the catching region */
						for(String group : regionData.getStringList("catchgroups", null)) {
							area.addGroup(group);
						}
						
						catchareas.get(world.getName()).put(region, area);
					}
		        }
			}
			
			CatchLang.sysMess(catchareas.size() + " " + CatchLang.sys_areaFoundIn + " " + world.getName());
		}
		
		CatchLang.sysMess(CatchLang.sys_loadCatchRegionSuccess);
	}


	public boolean getInfo(Player p, String area_name) {

		if(regionmanager == null)
			return false;

		String world = p.getWorld().getName();
		
		ProtectedRegion region = regionmanager.getRegion(area_name);
		
		if(region == null || !catchareas.get(world).containsKey(region))
			return false;

		CatchArea area = catchareas.get(world).get(region);

		String[] info = area.getInfo();
		p.sendMessage(info[0]);
		p.sendMessage(info[1]);
		
		return true;
	}
	
	public boolean addArea(Player p, String area_name) {

		if(regionmanager == null)
			return false;

		String world = p.getWorld().getName();
		
		ProtectedRegion region = regionmanager.getRegion(area_name);

		if(region == null || catchareas.get(world).containsKey(region))
			return false;
		
		CatchArea area = new CatchArea(area_name);
		catchareas.get(world).put(region, area);
		
		p.sendMessage(area_name + " " + CatchLang.sys_addedSuccess);
		return true;
	}

	public boolean removeArea(Player p, String area_name) {

		if(regionmanager == null)
			return false;
		
		String world = p.getWorld().getName();
		
		ProtectedRegion region = regionmanager.getRegion(area_name);
		
		if(region == null || !catchareas.get(world).containsKey(region))
			return false;
		
		catchareas.get(world).remove(region);
		
		p.sendMessage(area_name + " " + CatchLang.sys_removedSuccess);
		return true;
	}

	public boolean addGroupRegion(Player p, String area_name, String group_name) {
		
		if(regionmanager == null)
			return false;
		
		String world = p.getWorld().getName();
		
		ProtectedRegion region = regionmanager.getRegion(area_name);
		
		if(region == null || !catchareas.get(world).containsKey(region))
			return false;
		
		if(catchareas.get(world).get(region).addGroup(group_name)) {
			p.sendMessage(area_name + " " + CatchLang.sys_updatedSuccess);
			return true;
		}
			
		return false;
	}

	public boolean removeGroupRegion(Player p, String area_name, String group_name) {
		
		if(regionmanager == null)
			return false;
		
		String world = p.getWorld().getName();
		
		ProtectedRegion region = regionmanager.getRegion(area_name);
		
		if(region == null || !catchareas.get(world).containsKey(region))
			return false;
		
		if(catchareas.get(world).get(region).removeGroup(group_name)) {
			
			p.sendMessage(area_name + " " + CatchLang.sys_updatedSuccess);
			return true;
		}
		
		return false;
	}

	public <T> boolean addFlagRegion(Player p, String area_name, String flag_name, String value) {

		String world = p.getWorld().getName();
		
		if(regionmanager == null)
			return false;
		
		ProtectedRegion region = regionmanager.getRegion(area_name);
		
		if(!catchareas.get(world).containsKey(region))
			return false;
		
		for(Flag<?> f : flagsCatchList) {
			
			if(f.getName().equals(flag_name)) {
				
				Object o;
				try {
					o = f.parseInput(catchme.plugin_worldguard, p, value);
				} catch (InvalidFlagFormat e) {
					return false;
				}
				
				catchareas.get(world).get(region).setFlag(f, o);
				p.sendMessage(area_name + " " + CatchLang.sys_updatedSuccess);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean list(Player p, Integer page) {

		String world = p.getWorld().getName();
		
		List<ProtectedRegion> lpr = new ArrayList<ProtectedRegion>();
		lpr.addAll(catchareas.get(world).keySet());
		
		p.sendMessage("Page "+page+"/"+(((lpr.size() - lpr.size()%10)/10)+1));
		
		// 10 per pages
		int from = (page-1)*10;
		
		if(from > lpr.size()-1) {	
			return true;	
		}
		
		int to  = Math.min((page)*10, lpr.size());
		
		for(ProtectedRegion r : lpr.subList(from, to)) {
			p.sendMessage(r.getId());
		}
		
		return true;
	}
	
	public void save() {

		for(String world : config_map.keySet()) {
			
			saveWorld(world, config_map.get(world));
		}
	}
	
	public void saveWorld(String world, Configuration config) {
		
		if(config == null)
			return;
		
		config.clear();
		
		/* Save regions */
		for(ProtectedRegion region : catchareas.get(world).keySet()) {
			
			CatchArea area = catchareas.get(world).get(region);
			
			ConfigurationNode regionData = config.addNode("regions."+region.getId());
			
			Map<String, Object> flags_map = area.getFlagsMap();
			List<String> groups_list = area.getGroupsList();
			
			regionData.setProperty("flags", flags_map);
			regionData.setProperty("catchgroups", groups_list);
		}
		
		try {
			config.save();
		} catch (IOException e) {
			CatchLang.sysMess(CatchLang.sys_errorSavingConfig + " " + world);
		}
	}
}
