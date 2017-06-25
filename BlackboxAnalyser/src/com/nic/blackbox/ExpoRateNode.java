package com.nic.blackbox;

import com.nic.streamprocessor.Node;

public class ExpoRateNode extends Node
{
	public ExpoRateNode()
	{
		super();
		setExpectedInputs(new String[] {"rate", "rc_rate", "rc_expo", "rc"});
	}
	
	protected void processStep()
	{
		double rate = getInput("rate") / 100d;
		double rcrate = getInput("rc_rate") / 100d;
		double expo = getInput("rc_expo") / 100d;
		double rc = getInput("rc") / 500d;
		
		
		double output = (rc * Math.pow(Math.abs(rc), 3d) * expo) + (rc * (1d - expo));
		output *= 200d * rcrate;
		
		double rcSuperFactor = 1d / (1d - (Math.abs(rc)  * rate));
		output *= rcSuperFactor;
		
		setOutput("result", (int)output);
		pushOutputs();
	}

	protected boolean processDrain()
	{
		return true;
	}

}
