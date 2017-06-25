import com.nic.blackbox.BlackboxReader;
import com.nic.blackbox.ExpoRateNode;
import com.nic.blackbox.Simulator;
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
			MemoryStreamTarget target = new MemoryStreamTarget( new String[]{ "command", "actualRate", "pid", "propSpeed[0]"});
			new Pipe(sim, "command", target, "command");
			new Pipe(sim, "pid", target, "pid");
			new Pipe(sim, "propSpeed[0]", target, "propSpeed[0]");
			new Pipe(sim, "actualRate", target, "actualRate");
			
			//for(int i = 0; i < 3200; i++)
			//	bbr.nextStep();
			
			while(sim.nextStep());
			
			SeriesVisualiserFrame vis = new SeriesVisualiserFrame(target);
			vis.setVisible(true);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
