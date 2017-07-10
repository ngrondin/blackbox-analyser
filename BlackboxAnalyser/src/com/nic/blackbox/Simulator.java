package com.nic.blackbox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import com.nic.streamprocessor.StreamSource;

public class Simulator extends StreamSource
{
	protected Random rnd;
	protected float timeStep;
	protected float bodyMass;
	protected float propMass;
	protected float propHover;
	protected float dragFactor;
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
	protected float disturbanceTarget;
	protected float disturbance;
	protected float disturbanceD;
	
	public Simulator()
	{
		rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
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
		
		try
		{
			Properties prop = new Properties();
			prop.load(new FileInputStream("simulator.properties"));
			timeStep = Float.parseFloat(prop.getProperty("timeStep"));
			bodyMass = Float.parseFloat(prop.getProperty("bodyMass"));
			propMass = Float.parseFloat(prop.getProperty("propMass"));
			propHover = Float.parseFloat(prop.getProperty("propHover"));
			dragFactor = Float.parseFloat(prop.getProperty("dragFactor"));
			ratePFactor = Float.parseFloat(prop.getProperty("ratePFactor"));
			rateIFactor = Float.parseFloat(prop.getProperty("rateIFactor"));
			rateDFactor = Float.parseFloat(prop.getProperty("rateDFactor"));
			motorPFactor = Float.parseFloat(prop.getProperty("motorPFactor"));
		} 
		catch 
		(Exception e)
		{
			e.printStackTrace();
		}		
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
		command = command + (0.95f * commandD) + (0.05f * ( (0.2f * (commandTarget - command)) - (0.95f * commandD) ) );
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
		float rateDNoisy = rateDFactor * rateErrorD;
		rateD += 0.1 * (rateDNoisy - rateD); // lpf on rateD
		pid = rateP + rateI + rateD;
		motorInput[0] = constrain(propHover + pid, 0f, 1f);
		motorInput[1] = constrain(propHover - pid, 0f, 1f);
		
		setOutput("rateP", rateP);
		setOutput("rateI", rateI);
		setOutput("rateD", rateD);
		setOutput("pid", pid);
		setOutput("motorInput[0]", motorInput[0]);
		setOutput("motorInput[1]", motorInput[1]);
	}
	
	protected void physicsStep()
	{
		if(probability(0.001f))
			disturbanceTarget = random(-0.2f, 0.2f);
		if(disturbanceTarget != 0f  &&  probability(0.005f))
			disturbanceTarget = 0f;
		
		float lastDisturbance = disturbance;
		disturbance = disturbance + (0.95f * disturbanceD) + (0.05f * ( (0.2f * (disturbanceTarget - disturbance)) - (0.95f * disturbanceD) ) );
		disturbanceD = disturbance - lastDisturbance;

		float[] propDrag = new float[2];
		for(int i = 0; i < 2; i++)
		{
			propDrag[i] = dragFactor * power2(propSpeed[i]);
			propSpeed[i] = propSpeed[i] + ((((motorPFactor * (motorInput[i] - propSpeed[i])) - propDrag[i]) / propMass) * timeStep);
			propThrust[i] = power2(propSpeed[i]);
			setOutput("propSpeed[" + i + "]", propSpeed[i]);
		}
		
		actualRate = actualRate + (((propThrust[0] - propThrust[1] + disturbance) / bodyMass) * timeStep);
		
		actualRate += random(-0.01f, 0.01f); // vibration noise
		
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
	
	protected float constrain(float v, float min, float max)
	{
		float ret = v;
		if(ret < min)
			ret = min;
		if(ret > max)
			ret = max;
		return ret;
	}
}
