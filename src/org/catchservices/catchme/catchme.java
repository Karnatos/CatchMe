package org.catchservices.catchme;

import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.herocraftonline.dthielke.lists.Lists;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class catchme extends JavaPlugin {
	
	public static boolean loadSuccess = true;
	
	private CatchPlayerListener playerListener;
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private CatchRegions catchregions = null;
	
	/* External plugins */
	public static PluginManager pm = null;
	public static Lists plugin_lists = null;
	public static WorldGuardPlugin plugin_worldguard = null;
	public static Permissions plugin_perms = null;
	public static PermissionHandler plugin_permsHandler = null;
	public static iConomy plugin_iconomy = null;
	public static Server bukkit_server = null;

	public void onDisable() {

		// NOTE: All registered events are automatically unregistered when a plugin is disabled

		/* Code Disable CatchMe */
		if(catchregions != null)
			catchregions.save();
		
		CatchLang.sysMess(CatchLang.sys_pluginDisabled);
	}

	public void onEnable() {
			
		bukkit_server = getServer();
	    pm = getServer().getPluginManager();

		/* Code Enable CatchMe */

        loadWorldGuardPlugin();
	        
	    if(!loadSuccess) {
	    	CatchLang.sysMess(CatchLang.sys_pluginDisabled);
	      	return;
	    }
	        
	    loadPermissionsPlugin();
	    loadListsPlugin();
	    loadiConomyPlugin();
	        
	    /* Register commands */
	    getCommand("cm").setExecutor(new CatchMeCommand(this));
	    
	    /* Register events */
	    playerListener = new CatchPlayerListener(this);
	    pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
	    pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
	    pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
	        
	    playerListener = new CatchPlayerListener(this);
	        
	    /* Load Catching Regions from WorldGuard */
	    catchregions = new CatchRegions(this);
	        
	    /* Load Lang file */
	    CatchLang.loadLangFile(this);
	        
	    if(loadSuccess)
	    	CatchLang.sysMess(CatchLang.sys_catchmeEnabled);
	    else
	    {
	      	CatchLang.sysMess(CatchLang.sys_pluginDisabled);
	       	catchregions = null;
	    }
	}
	
	public void loadWorldGuardPlugin() {
		Plugin plugin =  pm.getPlugin("WorldGuard");
		if (plugin != null) {
			//if (plugin.isEnabled()) {
				plugin_worldguard = (WorldGuardPlugin) plugin;
				CatchLang.sysMess(plugin_worldguard.getDescription().getName() + " " + plugin_worldguard.getDescription().getVersion() + " " + CatchLang.sys_found);
				/*}
			else
			{
				loadSuccess = false;
			}*/
		}
		else
		{
			/* Fatal */
			CatchLang.sysMess(CatchLang.sys_worldguardDisabled);
			CatchLang.sysMess(CatchLang.sys_pluginDisabled);
			
			loadSuccess = false;
		}
	}
	
	public void loadPermissionsPlugin() {
		Plugin plugin = pm.getPlugin("Permissions");
		if (plugin != null) {
			//if (plugin.isEnabled()) {
				plugin_perms = (Permissions) plugin;
				plugin_permsHandler = plugin_perms.getHandler();
				CatchLang.sysMess(plugin_perms.getDescription().getName() + " " + plugin_perms.getDescription().getVersion() + " " + CatchLang.sys_found);
			//}
		}
		else
		{
			CatchLang.sysMess(CatchLang.sys_permissionsDisabled);
		}
	}

	public void loadListsPlugin() {
		Plugin plugin = pm.getPlugin("Lists");
		if (plugin != null) {
			//if (plugin.isEnabled()) {
				plugin_lists = (Lists) plugin;
				CatchLang.sysMess(plugin_lists.getDescription().getName() + " " + plugin_lists.getDescription().getVersion() + " " + CatchLang.sys_found);
			//}
        }
		else
		{
			CatchLang.sysMess(CatchLang.sys_listsDisabled);
		}
	}
	
	public void loadiConomyPlugin() {
		Plugin plugin = pm.getPlugin("iConomy");
		if (plugin != null) {
			//if (plugin.isEnabled()) {
				plugin_iconomy = (iConomy) plugin;
				CatchLang.sysMess(plugin_iconomy.getDescription().getName() + " " + plugin_iconomy.getDescription().getVersion() + " " + CatchLang.sys_found);
			//}
        }
		else
		{
			CatchLang.sysMess(CatchLang.sys_iconomyDisabled);
		}
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
