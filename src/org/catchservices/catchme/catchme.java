package org.catchservices.catchme;

import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class catchme extends JavaPlugin {
	
	private final CatchPlayerListener playerListener = new CatchPlayerListener(this);
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private CatchRegions catchregions;

	// NOTE: There should be no need to define a constructor any more for more info on moving from
	// the old constructor see:

	public void onDisable() {

		// NOTE: All registered events are automatically unregistered when a plugin is disabled

		/* Code Disable CatchMe */
		CatchLang.sysMess(CatchLang.sys_puglinDisabled);
	}

	public void onEnable() {

	        PluginManager pm = getServer().getPluginManager();
	        
	        /* WorldGuard load and verification */
	        WorldGuardPlugin wgp =  (WorldGuardPlugin) pm.getPlugin("WorldGuard");
	        
	        if(wgp == null) {
	        	CatchLang.sysMess(CatchLang.sys_worldguardDisabled);
	        	return;
	        }
	        
	        /* Register events */
	        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
	        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
	        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);

	        // Register commands
	        /*getCommand("pos").setExecutor(new SamplePosCommand(this));
	        getCommand("debug").setExecutor(new SampleDebugCommand(this));*/

	        /* Code Enable CatchMe */

	        /* Load Catching Regions from WorldGuard */
	        catchregions = new CatchRegions(wgp, this);
	        CatchLang.loadLangFile(this);
	        
	        CatchLang.sysMess(CatchLang.sys_catchmeEnabled);
		}

	    public boolean isDebugging(final Player player) {
	        if (debugees.containsKey(player)) {
	            return debugees.get(player);
	        } else {
	            return false;
	        }
	    }

	    public void setDebugging(final Player player, final boolean value) {
	        debugees.put(player, value);
	    }	
	    
	    public CatchRegions getCatchRegions() {
	    	
	    	return catchregions;
	    }
}
