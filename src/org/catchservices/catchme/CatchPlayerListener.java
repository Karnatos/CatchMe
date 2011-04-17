package org.catchservices.catchme;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CatchPlayerListener extends PlayerListener {

	catchme parent;
	
	public CatchPlayerListener(catchme parent) {
		this.parent = parent;
	}
	
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if(event.isCancelled())
        {
            return;
        }
		
		/* Test if located in a catching area */
		if(parent.getCatchRegions() != null && !event.getFrom().equals(event.getTo()))
			parent.getCatchRegions().playerLookupCatchingArea(event);
	}
	
	public void onPlayerJoin(PlayerJoinEvent event)
	{		
		/* Test if spawning in a catching area */
		if(parent.getCatchRegions() != null)
			parent.getCatchRegions().playerLookupCatchingArea(event);
	}
	
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		/* Test if leaving a catching area */
		if(parent.getCatchRegions() != null)
			parent.getCatchRegions().playerQuitCatchingArea(event);
	}
}
