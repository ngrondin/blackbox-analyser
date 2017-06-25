import com.nic.blackbox.BlackboxReader;
import com.nic.blackbox.ExpoRateNode;
import com.nic.streamprocessor.Pipe;
import com.nic.streamprocessor.StreamTarget;
import com.nic.streamprocessor.nodes.Absolute;
import com.nic.streamprocessor.nodes.Arithmetic;
import com.nic.streamprocessor.streamtargets.FlatFileStreamTarget;
import com.nic.streamprocessor.streamtargets.MemoryStreamTarget;
import com.nic.streamprocessor.ui.SeriesVisualiserFrame;


public class BBAnalyserTest
{
	public static void main(String[] args)
	{
		try
		{
			BlackboxReader bbr = new BlackboxReader("D:\\Documents\\Nicolas\\Electronic\\Dronelogs\\LOG00068.TXT");
			bbr.nextLog();
			//FlatFileStreamTarget target = new FlatFileStreamTarget("D:\\Scratch\\LOG00068.csv", new String[]{ "gyroADC[0]", "rcCommandRate[0]"});
			MemoryStreamTarget target = new MemoryStreamTarget( new String[]{ "gyroADC[0]", "rcCommandRate[0]", "error"});
			ExpoRateNode expo = new ExpoRateNode();
			Arithmetic subs1 = new Arithmetic("substract");
			Absolute abs1 = new Absolute();
			
			new Pipe(bbr, "rcCommand[0]", expo, "rc");
			new Pipe(bbr, "rc_rate", expo, "rc_rate");
			new Pipe(bbr, "rc_expo", expo, "rc_expo");
			new Pipe(bbr, "rate[0]", expo, "rate");
			
			new Pipe(expo, "result", subs1, "term1");
			new Pipe(bbr, "gyroADC[0]", subs1, "term2");

			new Pipe(subs1, "result", abs1, "input");

			new Pipe(bbr, "gyroADC[0]", target, "gyroADC[0]");
			new Pipe(expo, "result", target, "rcCommandRate[0]");
			new Pipe(abs1, "output", target, "error");
			
			//for(int i = 0; i < 3200; i++)
			//	bbr.nextStep();
			
			while(bbr.nextStep());
			
			SeriesVisualiserFrame vis = new SeriesVisualiserFrame(target);
			vis.setVisible(true);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
