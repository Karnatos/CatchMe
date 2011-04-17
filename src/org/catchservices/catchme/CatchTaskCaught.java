package org.catchservices.catchme;

import java.util.TimerTask;

public class CatchTaskCaught extends TimerTask{

	private CatchArea caller;
	
	public CatchTaskCaught(CatchArea caller) {
		
		this.caller = caller;
	}
	
	@Override
	public void run() {

		caller.timerCompleteCaught();
	}

}
