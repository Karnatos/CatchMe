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

import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
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
		CATCHING_DURATION, CATCHING_DURATION, CATCHING_TIME_CATCH, CATCHING_PERIOD, CATCHING_MONEY_AMOUNT
	};
	
	private WorldGuardPlugin worldguard;
	public catchme parent;
	private Map<ProtectedRegion, CatchArea> catchareas = new HashMap<ProtectedRegion, CatchArea>();
	private List<Player> playersInAreas;
	
	public CatchRegions(WorldGuardPlugin wgp, catchme parent) {
		
		worldguard = wgp;
		this.parent = parent;
		playersInAreas = new ArrayList<Player>();
		
		loadCatchingAreas();
	}
	
	public void playerLookupCatchingArea(PlayerEvent event) {
		
		Location loc = event.getPlayer().getLocation();
		Player player = event.getPlayer();
		
		/* Test if located in a catching area */
		
		Vector v = new Vector(loc.getX(), loc.getY(), loc.getZ());
		
		Iterator<ProtectedRegion> irs = catchareas.keySet().iterator();
		
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
			
			area = catchareas.get(region);
			
			playerEnteringCatchArea(player, area);
		}	
		
		/* Test if leaving a catching area */
		playerLeavingCatchArea(player, area);	
	}

	public void playerLeavingCatchArea(Player player, CatchArea area) {
		/* Look if */
		if(playersInAreas.contains(player)) {
			
			String[] groups = worldguard.getGroups(player);
				
			for(CatchArea a : catchareas.values()) {
					
				if(area == null || area != a)
					a.checkRemovePlayer(player, groups);
			}
				
			if(area == null)
				playersInAreas.remove(player);
		}
	}

	public void playerEnteringCatchArea(Player player, CatchArea area) {

		/* Look for player groups */
		String[] groups = worldguard.getGroups(player);
		
		if(!playersInAreas.contains(player)) {
			playersInAreas.add(player);
		}
		
		area.checkAddPlayer(player, groups);
	}

	public void playerQuitCatchingArea(PlayerQuitEvent event) {
		
		playerLeavingCatchArea(event.getPlayer(), null);
	}
	
	public void loadCatchingAreas() {
		
		/* Load catching areas from WorldGuard */
		List<World> lw = parent.getServer().getWorlds();
		
		Iterator<World> iw = lw.iterator();
		
		while(iw.hasNext()) {
			
			World world = iw.next();
			
			RegionManager regionmanager = worldguard.getGlobalRegionManager().get(world);
			
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
						
						Object f = flagsData.getProperty(CATCHING_AREA.getName());
						State s = CATCHING_AREA.unmarshal(f);
						
						if(s == State.ALLOW) {

							CatchArea area = new CatchArea(this, region.getId());
						
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
							
							catchareas.put(region, area);
						}
					}
		        }
			}
		}
		
		CatchLang.sysMess(CatchLang.sys_loadCatchRegionSuccess);
	}
	
	public List<String> getPlayersGroup(String actualGroup) {

		List<String> ret = new ArrayList<String>();
		/* Load catching areas from WorldGuard */
		List<World> lw = parent.getServer().getWorlds();
		
		Iterator<World> iw = lw.iterator();
		
		while(iw.hasNext()) {
			
			World world = iw.next();
			
			/* search region's flags */
			Permissions perm = (Permissions) parent.getServer().getPluginManager().getPlugin("Permissions");
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
					
					Player p = parent.getServer().getPlayer(user);
					
					
					if(p != null && worldguard.inGroup(p, actualGroup)) {
						ret.add(user);
					}
				}
			}
		}
		
		return ret;
	}
}
