package org.catchservices.catchme;

import java.util.TimerTask;

public class CatchTaskCatching extends TimerTask{

	private CatchArea caller;
	
	public CatchTaskCatching(CatchArea caller) {
		
		this.caller = caller;
	}
	
	@Override
	public void run() {
		
		caller.timerCompleteCatching();	
	}

}
