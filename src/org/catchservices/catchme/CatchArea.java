package org.catchservices.catchme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

public class CatchArea {

	private String region_name;
	private Integer duration; /* number of periods */
	private Integer period; /* Time in seconds before earning money */
	private Integer money_amount; /* Amount of money earnt */
	private Integer time_catch; /* Time before activing control */
	private Timer timer_catching;
	private Timer timer_caugth;
	private Integer actual_duration;
	private boolean region_active;
	
	private Map<String, ArrayList<Player>> playerslist;
	private String actual_group; 
	
	public static final Integer DEFAULT_DURATION = 12; // 12 periods
	public static final Integer DEFAULT_PERIOD = 15*60; // 15 minutes
	public static final Integer DEFAULT_TIME_CATCH = 30; // 30 seconds
	public static final Integer DEFAULT_MONEY_AMOUNT = 5; // 5 Coins
	
	@SuppressWarnings("unchecked")
	private Map<Flag, Object> flags_map;
	
	@SuppressWarnings("unchecked")
	public CatchArea(String name) {
		
		region_name = name;
		this.duration = DEFAULT_DURATION;
		this.period = DEFAULT_PERIOD;
		this.time_catch = DEFAULT_TIME_CATCH;
		this.money_amount = DEFAULT_MONEY_AMOUNT;
		timer_caugth = null;
		timer_catching = null;
		actual_group = null;
		actual_duration = 0;
		region_active = false;
		
		playerslist = new HashMap<String, ArrayList<Player>>();
		flags_map = new HashMap<Flag, Object>();
	}
	
	public void setActive(State val) {
		if(val == State.ALLOW)
			this.region_active = true;
		else
			this.region_active = false;
	}
	
	public void setDuration(int val) {
		this.duration = val;
	}
	
	public void setPeriod(int val) {
		this.period = val;
	}

	public void setTimeCatch(int val) {
		this.time_catch = val;
	}

	public void setMoneyAmount(int val) {
		this.money_amount = val;
	}

	public <T> boolean setFlag(Flag<T> flag, Object o) {
		
		T val = flag.unmarshal(o);
		if (val == null) {
			// can't parse
			return false;
		}
		
		if(flag.equals(CatchRegions.CATCHING_AREA))
		{
			setActive((State)val);
		}
		else if(flag.equals(CatchRegions.CATCHING_DURATION))
		{
			setDuration((Integer)val);
		}
		else if(flag.equals(CatchRegions.CATCHING_PERIOD))
		{
			setPeriod((Integer)val);
		}
		else if(flag.equals(CatchRegions.CATCHING_TIME_CATCH))
		{
			setTimeCatch((Integer)val);
		}
		else if(flag.equals(CatchRegions.CATCHING_MONEY_AMOUNT))
		{
			setMoneyAmount((Integer)val);
		}
		else
		{
			// Flag ignored
		}
		
		flags_map.put(flag, val);
		
		return true;
	}
	
	public boolean addGroup(String group) {
		
		if(playerslist.containsKey(group))
			return false;
		
		playerslist.put(group, new ArrayList<Player>());
		
		return true;
	}

	public void checkAddPlayer(Player player, String[] groups) {
		
		/* Check if player already listed */
		ArrayList<Player> players;
		
		for(Integer i=0; i<groups.length;i++) {
			
			if((players = playerslist.get(groups[i])) != null && !players.contains(player)) {
				
				/* Add player */
				players.add(player);
				
				if(region_active) {
				
					if(actual_group != null) {
						CatchLang.playerMess(player, CatchLang.get("enter-catchzone")+" "+region_name+" " 
								+ CatchLang.get("controlled-by") + " " + actual_group);
					}
					else
					{
						CatchLang.playerMess(player, CatchLang.get("enter-catchzone")+" "+region_name);
					}
					
					checkStartTime();
				}
			}
		}
	}
	
	public void checkRemovePlayer(Player player, String[] groups) {
		
		/* Check if player already listed */
		ArrayList<Player> players;
		
		for(Integer i=0; i<groups.length;i++) {
			
			if((players = playerslist.get(groups[i])) != null && players.contains(player)) {
			
			/* Remove player */
			players.remove(player);
			
			if(timer_catching != null && players.size() == 0)
				stopCatching();
			}
		}
	}

	public void checkStartTime() {

		Integer nbGroupsWithPlayers = 0;
		String lastGroupWithPlayers = "";
		for(String key : playerslist.keySet()) {
			ArrayList<Player> players = playerslist.get(key);
			
			if(players.size() > 0) {
				nbGroupsWithPlayers++;
				lastGroupWithPlayers = key;
			}
		}
		
		if(timer_catching == null && nbGroupsWithPlayers == 1 
				&& (actual_group == null || !lastGroupWithPlayers.equals(actual_group))) {
			startTimer();
		}
		else if(timer_catching != null && nbGroupsWithPlayers != 1)
		{
			stopCatching();
		}
	}

	public void startTimer() {

		timer_catching = new Timer(true);
		timer_catching.schedule(new CatchTaskCatching(this), time_catch*1000);
	}
	
	public void timerCompleteCatching() {
		
		
		timer_catching = null;
		
		stopCaught();
		
		actualizeGroup();
		CatchLang.broadMess(catchme.bukkit_server, actual_group+" "+CatchLang.get("now-controls")+" "+region_name);
			
		CatchLang.sysMess(CatchLang.sys_group+" "+actual_group+"' "+CatchLang.sys_control+" '"+region_name+"'");
		
		actual_duration = 0;
		timer_caugth = new Timer(true);
		timer_caugth.schedule(new CatchTaskCaught(this), period*1000, period*1000);
	}

	public void actualizeGroup() {

		for(String key : playerslist.keySet()) {
			ArrayList<Player> players = playerslist.get(key);
			
			if(players.size() > 0) {
				actual_group = key;
			}
		}	
	}

	public void addMoneyToPlayers() {
	
		CatchLang.sysMess(CatchLang.sys_addMoneyToGroup+" "+actual_group);
		Player pl;
		
		for(String p : CatchUnifiedGroups.getPlayersGroup(actual_group)) {
			
			pl = catchme.bukkit_server.getPlayer(p);
			
			if(pl != null) {
				CatchLang.playerMess(pl, CatchLang.get("receive-money")+" "+money_amount
						+" "+iConomy.getBank().getCurrency()+" "+CatchLang.get("from")+" "+region_name);
			}
			
			iConomy.getBank().getAccount(p).add(money_amount);
		}
	}

	public void timerCompleteCaught() {
		actual_duration++;
		
		if(duration == 0 || actual_duration <= duration) {
			addMoneyToPlayers();
		}
		else
		{
			stopCaught();
		}
	}

	public void stopCatching() {

		if(timer_catching != null)
			timer_catching.cancel();
		timer_catching = null;		
	}
	
	public void stopCaught() {

		if(timer_caugth != null)
			timer_caugth.cancel();
		timer_caugth = null;
		actual_group = null;
	}

	public boolean removeGroup(String group_name) {
		
		if(!playerslist.containsKey(group_name))
			return false;
		
		if(actual_group != null && actual_group.equals(group_name))
			stopCaught();
		
		playerslist.remove(group_name);
		
		return true;
	}

	@SuppressWarnings("unchecked")
	public <T> Map<String, Object> getFlagsMap() {

		Map<String, Object> ret = new HashMap<String, Object>();
		
		for(Flag<T> f : flags_map.keySet()) {
			
			Object s = f.marshal((T) flags_map.get(f));
			ret.put(f.getName(), s);
		}
		
		return ret;
	}

	public List<String> getGroupsList() {

		List<String> ret = new ArrayList<String>();
		
		ret.addAll(playerslist.keySet());
		
		return ret;
	}

	public String[] getInfo() {
		
		String[] ls = new String[2];
		
		String s = "";
		s += region_name + " ( ";
		if(region_active)
			s += "ALLOW";
		else
			s += "DENY";
		s += " | duration : " + duration;
		s += " | money-amount : " + money_amount;
		s += " | period : " + period;
		s += " | time-catch : " + time_catch;
		s += " ) ";
		
		ls[0] = s;
		
		s = "groups : ";
		
		for(String g : playerslist.keySet()) {
			s += g + " ";
		}
		
		ls[1] = s;
		
		return ls;
	}
}
