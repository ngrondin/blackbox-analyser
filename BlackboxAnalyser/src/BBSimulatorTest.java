import com.nic.blackbox.BlackboxReader;
import com.nic.blackbox.ExpoRateNode;
import com.nic.blackbox.Simulator;
import com.nic.blackbox.SimulatorFrame;
import com.nic.streamprocessor.Pipe;
import com.nic.streamprocessor.StreamTarget;
import com.nic.streamprocessor.nodes.Absolute;
import com.nic.streamprocessor.nodes.Arithmetic;
import com.nic.streamprocessor.streamtargets.FlatFileStreamTarget;
import com.nic.streamprocessor.streamtargets.MemoryStreamTarget;
import com.nic.streamprocessor.ui.SeriesVisualiserFrame;


public class BBSimulatorTest
{
	public static void main(String[] args)
	{
		try
		{
			Simulator sim = new Simulator();
			MemoryStreamTarget target = new MemoryStreamTarget( new String[]{ "command", "actualRate", "rateP", "rateI", "rateD", "pid", "motorInput[0]", "motorInput[1]", "propSpeed[0]", "propSpeed[1]"});
			new Pipe(sim, "command", target, "command");
			new Pipe(sim, "rateP", target, "rateP");
			new Pipe(sim, "rateI", target, "rateI");
			new Pipe(sim, "rateD", target, "rateD");
			new Pipe(sim, "pid", target, "pid");
			new Pipe(sim, "motorInput[0]", target, "motorInput[0]");
			new Pipe(sim, "motorInput[1]", target, "motorInput[1]");
			new Pipe(sim, "propSpeed[0]", target, "propSpeed[0]");
			new Pipe(sim, "propSpeed[1]", target, "propSpeed[1]");
			new Pipe(sim, "actualRate", target, "actualRate");
			
			SimulatorFrame f = new SimulatorFrame();
			f.setVisible(true);
			f.setSourceAndTarget(sim, target);
			//for(int i = 0; i < 3200; i++)
			//	bbr.nextStep();
			/*
			while(sim.nextStep());
			
			SeriesVisualiserFrame vis = new SeriesVisualiserFrame(target);
			vis.setActiveChannelMask(3);
			vis.setVisible(true);
			*/
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
