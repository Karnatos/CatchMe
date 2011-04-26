package org.catchservices.catchme;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CatchMeCommand implements CommandExecutor {

	private catchme parent;
	
	public CatchMeCommand(catchme parent) {
		this.parent = parent;
	}
	
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] arg3) {
		
		if(!arg1.getName().equals("cm")) {
			return false;
		}
		
		/* Commands lists :
		 * /cm info <area_name>
		 * /cm addarea <area_name>
		 * /cm removearea <area_name>
		 * /cm addgroup <area_name> <group_name>
		 * /cm removegroup <area_name> <group_name>
		 * /cm flag <area_name> <flag_name> <value>
		 * /cm save
		 * /cm list [#page]
		 * */
		Player p;

		if (sender instanceof Player) {
			p = (Player) sender;
		}
		else
		{
			/* Not implemented yet */
			return false;
		}

		/* Check permissions */
		if(catchme.plugin_permsHandler != null && !catchme.plugin_permsHandler.has(p, "catchme.cm")) {
			p.sendMessage("No permissions");
			return true;
		}
		
		/* Check command */
		
		if(arg3.length > 1 && arg3[0].equals("info")) {
			
			if(!parent.getCatchRegions().getInfo(p, arg3[1]))
				p.sendMessage(CatchLang.usr_usageInfo);
			
			return true;
		}
		
		if(arg3.length > 1 && arg3[0].equals("addarea")) {
			
			if(!parent.getCatchRegions().addArea(p, arg3[1]))
				p.sendMessage(CatchLang.usr_usageAddarea);
			
			return true;
		}

		if(arg3.length > 1 && arg3[0].equals("removearea")) {
			
			if(!parent.getCatchRegions().removeArea(p, arg3[1]))
				p.sendMessage(CatchLang.usr_usageRemovearea);
			
			return true;
		}

		if(arg3.length > 2 && arg3[0].equals("addgroup")) {
	
			if(!parent.getCatchRegions().addGroupRegion(p, arg3[1], arg3[2]))
				p.sendMessage(CatchLang.usr_usageAddgroup);
			
			return true;
		}

		if(arg3.length > 2 && arg3[0].equals("removegroup")) {
		
			if(!parent.getCatchRegions().removeGroupRegion(p, arg3[1], arg3[2]))
				p.sendMessage(CatchLang.usr_usageRemovegroup);
			
			return true;
		}

		if(arg3.length > 3 && arg3[0].equals("flag")) {
			
			if(!parent.getCatchRegions().addFlagRegion(p, arg3[1], arg3[2], arg3[3]))
				p.sendMessage(CatchLang.usr_usageFlag);
			
			return true;
		}
		
		if(arg3.length > 0 && arg3[0].equals("save")) {
			
			parent.getCatchRegions().save();
			p.sendMessage(CatchLang.usr_saveSuccess);
			
			return true;
		}

		if(arg3.length > 0 && arg3[0].equals("list")) {
			
			Integer page = 1;
			
			if(arg3.length > 1)
				page = Integer.parseInt(arg3[1]);
				
			if(!parent.getCatchRegions().list(p, page))
				p.sendMessage(CatchLang.usr_usageList);
			
			return true;
		}
		
		if(arg3.length > 0 && arg3[0].equals("info"))
			p.sendMessage(CatchLang.usr_usageInfo);
		else if(arg3.length > 0 && arg3[0].equals("addarea"))
			p.sendMessage(CatchLang.usr_usageAddarea);
		else if(arg3.length > 0 && arg3[0].equals("removearea"))
			p.sendMessage(CatchLang.usr_usageRemovearea);
		else if(arg3.length > 0 && arg3[0].equals("addgroup"))
			p.sendMessage(CatchLang.usr_usageAddgroup);
		else if(arg3.length > 0 && arg3[0].equals("removegroup"))
			p.sendMessage(CatchLang.usr_usageRemovegroup);
		else if(arg3.length > 0 && arg3[0].equals("flag"))
			p.sendMessage(CatchLang.usr_usageFlag);
		else
		{
			p.sendMessage(CatchLang.usr_usageInfo);
			p.sendMessage(CatchLang.usr_usageAddarea);
			p.sendMessage(CatchLang.usr_usageRemovearea);
			p.sendMessage(CatchLang.usr_usageAddgroup);
			p.sendMessage(CatchLang.usr_usageRemovegroup);
			p.sendMessage(CatchLang.usr_usageFlag);
			p.sendMessage(CatchLang.usr_usageSave);
			p.sendMessage(CatchLang.usr_usageList);
		}
		
		return true;
	}

}
