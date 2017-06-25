package com.nic.blackbox;

import java.util.Random;

import com.nic.streamprocessor.StreamSource;

public class Simulator extends StreamSource
{
	protected Random rnd;
	protected float timeStep;
	protected float bodyMass;
	protected float propMass;
	protected float propHover;
	protected float ratePFactor;
	protected float rateIFactor;
	protected float rateDFactor;
	protected float motorPFactor;
	protected float time;
	protected float command;
	protected float commandTarget;
	protected float commandD;
	protected float rateError;
	protected float rateCumulError;
	protected float rateP;
	protected float rateI;
	protected float rateD;
	protected float motorP;
	protected float pid;
	protected float[] motorInput;
	protected float[] propSpeed;
	protected float[] propThrust;
	protected float actualRate;
	
	public Simulator()
	{
		rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		timeStep = 0.001f;
		bodyMass = 0.01f;
		propMass = 0.01f;
		propHover = 0.1f;
		ratePFactor = 0.4f;
		rateIFactor = 0.0f;
		rateDFactor = 0.001f;
		motorPFactor = 0.4f;
		time = 0f;
		command = 0f;
		commandTarget = 0f;
		commandD = 0f;
		rateError = 0f;
		rateCumulError = 0f;
		rateP = 0;
		rateI = 0;
		rateD = 0;
		motorP = 0;
		pid = 0;
		motorInput = new float[] {0.5f, 0.5f};
		propSpeed = new float[] {0.5f, 0.5f};
		propThrust = new float[] {0.5f, 0.5f};
		actualRate = 0;		
	}

	public boolean nextStep()
	{
		commandStep();
		pidStep();
		physicsStep();
		pushOutputs();
		time += timeStep;
		if(time > 30f)
			return false;
		else
			return true;
	}
	
	protected void commandStep()
	{
		if(probability(0.001f))
		{
			commandTarget = random(-1f, 1f);
		}
		if(commandTarget != 0f  &&  probability(0.005f))
		{
			commandTarget = 0f;
		}
		
		float lastCommand = command;
		command = command + (0.9f * commandD) + (0.1f * ( (0.2f * (commandTarget - command)) - (0.9f * commandD) ) );
		commandD = command - lastCommand;
		setOutput("command", command);
	}
	
	protected void pidStep()
	{
		float lastRateError = rateError;
		rateError = command - actualRate;
		rateCumulError += rateError;
		float rateErrorD = (rateError - lastRateError) / timeStep;
		rateP = ratePFactor * rateError;
		rateI = rateIFactor * rateCumulError;
		rateD = rateDFactor * rateErrorD;
		pid = rateP + rateI - rateD;
		motorInput[0] = 0.5f + pid;
		motorInput[1] = 0.5f - pid;
		
		setOutput("rateP", rateP);
		setOutput("rateI", rateI);
		setOutput("rateD", rateD);
		setOutput("pid", pid);
	}
	
	protected void physicsStep()
	{
		float[] propDrag = new float[2];
		for(int i = 0; i < 2; i++)
		{
			propDrag[i] = power2(propSpeed[i]) / 1000000f;
			propSpeed[i] = propSpeed[i] + ((((motorPFactor * (motorInput[i] - propSpeed[i])) - propDrag[i]) / propMass) * timeStep);
			propThrust[i] = power2(propSpeed[i]);
			setOutput("propSpeed[" + i + "]", propSpeed[i]);
		}
		
		actualRate = actualRate + (((propThrust[0] - propThrust[1]) / bodyMass) * timeStep);
		
		setOutput("actualRate", actualRate);
	}
	
	protected float power2(float v)
	{
		return (float)Math.pow((double)v, 2d);
	}
	
	protected boolean probability(float p)
	{
		float r = rnd.nextFloat();
		if(r < p)
			return true;
		else
			return false;
	}
	
	protected float random(float min, float max)
	{
		float r = rnd.nextFloat();
		r *= (max - min);
		r += min;
		return r;
	}
}
