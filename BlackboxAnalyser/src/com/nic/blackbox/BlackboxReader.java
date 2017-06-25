package com.nic.blackbox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import com.nic.streamprocessor.StreamSource;
import com.nic.streamprocessor.streamsources.FileStreamSource;

public class BlackboxReader extends FileStreamSource
{
	protected int FRAME_TYPE_H	= 0;
	protected int FRAME_TYPE_I	= 1;
	protected int FRAME_TYPE_P	= 2;
	protected int FRAME_TYPE_S	= 3;
	protected int FRAME_TYPE_E	= 4;
	protected int FRAME_TYPE_BAD = 10;
	
	protected int FIELD_ENCODING_SIGNED_VB = 0; // Signed variable-byte
   	protected int FIELD_ENCODING_UNSIGNED_VB = 1; // Unsigned variable-byte
   	protected int FIELD_ENCODING_TAG2_3S32  = 7;
   	protected int FIELD_ENCODING_TAG8_4S16  = 8;
   	protected int FIELD_ENCODING_NULL = 9;// Nothing is written to the file, take value to be zero
    
	protected byte peekedByte;
	protected boolean hasPeeked;
	protected long streamCounter;
	protected long timedelta;
	protected int frameInterval;
	protected int minThrottle;
	protected boolean logFinished;
	protected FrameDefinition[] frameDefinitions;
	protected HashMap<String, String> headerFields;
	
	public BlackboxReader(String fileName) throws FileNotFoundException
	{
		super(fileName);
		streamCounter = 0;
		hasPeeked = false;
		timedelta = 0;
		frameInterval = 1;
		minThrottle = 1000;
		logFinished = true;
		frameDefinitions = new FrameDefinition[5];
		for(int i = 0 ; i < 5; i++)
			frameDefinitions[i] = new FrameDefinition();
		headerFields = new HashMap<String, String>();
	}
	
	public void nextLog()
	{
		try
		{
			if(nextHeaderSet())
			{
				logFinished = false;
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean nextStep() 
	{
		int frameType = FRAME_TYPE_BAD;
		try
		{
			while(frameType != FRAME_TYPE_I  &&  frameType != FRAME_TYPE_P  &&  !logFinished)	
				frameType = nextFrame();
			if(!logFinished)
			{
				pushOutputs();
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(IOException e)
		{
			logFinished = true;
			return false;
		}
	}
	
	protected boolean nextHeaderSet() throws IOException
	{
		byte b = peekNextByte();
		boolean isValidHeader = false;
		if(b == 'H')
		{
			while(peekNextByte() == 'H')
				nextHFrame();
			frameDefinitions[FRAME_TYPE_I].setNames(headerFields.get("Field I name").split(","));
			frameDefinitions[FRAME_TYPE_P].setNames(headerFields.get("Field I name").split(","));
			frameDefinitions[FRAME_TYPE_S].setNames( headerFields.get("Field S name").split(","));
			frameDefinitions[FRAME_TYPE_I].setEncodings(convertStringArrayToIntArray( headerFields.get("Field I encoding").split(",")));
			frameDefinitions[FRAME_TYPE_P].setEncodings(convertStringArrayToIntArray( headerFields.get("Field P encoding").split(",")));
			frameDefinitions[FRAME_TYPE_S].setEncodings(convertStringArrayToIntArray( headerFields.get("Field S encoding").split(",")));
			if( headerFields.get("P interval").equals("1/2"))
					frameInterval = 2;
			minThrottle = Integer.parseInt( headerFields.get("minthrottle"));	
			int[] rates = convertStringArrayToIntArray( headerFields.get("rates").split(","));
			for(int i = 0; i < rates.length; i++)
				setOutput("rate[" + i + "]", (float)rates[i]);
			isValidHeader = true;
		}
		return isValidHeader;
	}
	
	protected int nextFrame() throws IOException
	{
		int frameType = FRAME_TYPE_BAD;
		byte b = peekNextByte();
		
		if(b == 'H')
		{
			if(nextHFrame())
				frameType = FRAME_TYPE_H;
		}
		else if(b == 'I')
		{
			if(nextIFrame())
				frameType = FRAME_TYPE_I;
		}
		else if(b == 'P')
		{
			if(nextPFrame())
				frameType = FRAME_TYPE_P;
		}
		else if(b == 'S')
		{
			if(nextSFrame())
				frameType = FRAME_TYPE_S;
		}
		else if(b == 'E')
		{
			if(nextEFrame())
				frameType = FRAME_TYPE_E;
		}
		else
		{
			nextByte();
		}
		return frameType;
	}
	
	protected boolean nextHFrame() throws IOException
	{
		boolean validFrame = false;
		if(nextByte() == 'H')
		{
			nextByte();
			String line = nextLine('\n');
			String[] parts = line.split(":", -1);
			if(parts.length == 2)
			{
				headerFields.put(parts[0], parts[1]);
				try
				{
					float val = Float.parseFloat(parts[1]);
					setOutput(parts[0], val);
				}
				catch(Exception e) {}				
			}
			if(isValidStartOfFrame(peekNextByte()))
				validFrame = true;	
		}
		return validFrame;
	}
	
	protected boolean nextIFrame() throws IOException
	{
		timedelta = 0;
		boolean validFrame = false;
		if(nextByte() == 'I')
		{
			FrameDefinition fd = frameDefinitions[FRAME_TYPE_I];
			for(int i = 0; i < fd.getFieldCount(); i++)
			{
				long vals[] = nextEncodedValue(fd.getEncoding(i));	
				for(int j = 0; j < vals.length; j++)
				{
					String fieldName = fd.getName(i + j);
					float val = (float)vals[j];
					if(fieldName.equals("rcCommand[3]"))
					{
						val += minThrottle;
					}
					setOutput(fieldName, val);
				}
			}
			if(isValidStartOfFrame(peekNextByte()))
				validFrame = true;	
		}
		return validFrame;
	}
	
	protected boolean nextPFrame() throws IOException
	{
		boolean validFrame = false;
		if(nextByte() == 'P')
		{
			FrameDefinition fd = frameDefinitions[FRAME_TYPE_P];
			for(int i = 0; i < fd.getFieldCount(); i++)
			{
				long vals[] = nextEncodedValue(fd.getEncoding(i));	
				for(int j = 0; j < vals.length; j++)
				{
					String fieldName = fd.getName(i + j);
					float val = (float)vals[j];
					if(fieldName.equals("loopIteration"))
					{
						val = frameInterval;
					}
					else if(fieldName.equals("time"))
					{
						timedelta += val;
						val = timedelta;
					}
					val += getOutput(fieldName);;
					setOutput(fieldName, val);
				}
				i += vals.length - 1;
			}
			if(isValidStartOfFrame(peekNextByte()))
				validFrame = true;	
		}
		return validFrame;
	}
	
	protected boolean nextSFrame() throws IOException
	{
		boolean validFrame = false;
		if(nextByte() == 'S')
		{
			while(!isValidStartOfFrame(peekNextByte()))
				nextByte();
			validFrame = true;
		}
		return validFrame;		
	}
	
	protected boolean nextEFrame() throws IOException
	{
		boolean validFrame = false;
		if(nextByte() == 'E')
		{
			byte c = nextByte();
			if(c == 255)
			{
				String line = nextLine((char)0);
				if(line.equals("End of log"))
					logFinished = true;
			}
			if(isValidStartOfFrame(peekNextByte()))
				validFrame = true;	
		}
		return validFrame;		
	}
	
	protected byte nextByte() throws IOException
	{
		byte b;
		if(hasPeeked)
		{
			b = peekedByte;
			hasPeeked = false;
		}
		else
		{
			int t = fis.read();
			if(t == -1)
				throw new IOException("Reached end of file");
			b = (byte)t;
			streamCounter++;
		}
		return b;
	}

	protected byte peekNextByte() throws IOException
	{
		byte b;
		if(hasPeeked)
		{
			b = peekedByte;
		}
		else
		{
			b = nextByte();
			peekedByte = b;
			hasPeeked = true;
		}
		return b;
	}
	
	protected String nextLine(char eol) throws IOException
	{
		String line = "";
		char c;
		do
		{
			c = (char)nextByte();
			if(c != eol)
				line += c;
		}
		while(c != eol);
		return line;
	}
	
	protected long[] nextEncodedValue(int encoding) throws IOException
	{
		long[] values = null	;
		if(encoding == FIELD_ENCODING_SIGNED_VB)
		{
			values = new long[1];
			values[0] = nextSignedVB();
		}
		else if(encoding == FIELD_ENCODING_UNSIGNED_VB)
		{
			values = new long[1];
			values[0] = nextUnsignedVB();
		}
		else if(encoding == FIELD_ENCODING_TAG2_3S32)
		{
			values = nextTag2_3S32();
		}
		else if(encoding == FIELD_ENCODING_TAG8_4S16)
		{
			values = nextTag8_4S16();
		}
		else if(encoding == FIELD_ENCODING_NULL)
		{
			values = new long[1];
			values[0] = 0;
		}	
		return values;
	}

	protected long nextSignedVB() throws IOException
	{
		long uval =nextUnsignedVB();
		long val = ((uval >> 1) ^ (-(uval  & 1)));
		return val;
	}

	protected int nextUnsignedVB() throws IOException
	{
		int uval = 0;
		int byteCount = 0;
		boolean hasMoreBytes = true;
		while(hasMoreBytes)
		{
			byte c = nextByte();
			if((c & 0x80) == 0)
				hasMoreBytes = false;
			uval |= ((long)(c & 0x7F)) << (7 * byteCount);
			byteCount++;
		}
		return uval;
	}

	protected long[] nextTag8_4S16() throws IOException
	{
		long[] vals = new long[4];
		int selectors = 0;
		byte c = nextByte();
		int readbit = -1;
		int bitstoread = 0;
		selectors = c;
		for(int i = 0; i < 4; i++)
		{
			vals[i] = 0;
			switch(((selectors >> (2 * i)) & 0x03))
			{
				case 0: bitstoread = 0; break;
				case 1: bitstoread = 4; break;
				case 2: bitstoread = 8; break;
				case 3: bitstoread = 16; break;
			}
			for(int writebit = bitstoread - 1; writebit >= 0; writebit--)
			{
				if(readbit == -1)
				{
					c = nextByte();
					readbit = 7;
				}
				vals[i] |= ((c >> readbit) & 0x01) << writebit;
				readbit--;
			}
			if((vals[i] & (1 << (bitstoread - 1))) > 0)
				vals[i] -= (1 << bitstoread);
		}
		return vals;
	}

	protected long[] nextTag2_3S32() throws IOException
	{
		long[] vals = new long[3];
		int selector = 0;
		int selector2 = 0;
		byte c = nextByte();
		selector = (c >> 6) & 0x03;
		if(selector == 0)
		{
			vals[0] = (c >> 4) & 0x03;
			vals[1] = (c >> 2) & 0x03;
			vals[2] = (c >> 0) & 0x03;
		}
		else if(selector == 1)
		{
			vals[0] = c & 0x0F;
			c = nextByte();
			vals[1] = (c >> 4) & 0x0F;
			vals[2] = c & 0x0F;
		}
		else if(selector == 2)
		{
			vals[0] = c & 0x3F;
			c = nextByte();
			vals[1] = c & 0x3F;
			c = nextByte();
			vals[2] = c & 0x3F;

		}
		else if(selector == 3)
		{
			selector2 = c & 0x03;
			for(int i = 0; i < 3; i++)
			{
				vals[i] = 0;
				for(int j = 0; j <= selector2; j++)
				{
					c = nextByte();
					vals[i] |= (c << (j * 8));
				}
			}
		}
		return vals;
	}
	
	protected boolean isValidStartOfFrame(byte b)
	{
		if(b == 'I' || b == 'P' || b == 'H' || b == 'E' || b == 'S')
			return true;
		else
			return false;
	}
	
	protected int[] convertStringArrayToIntArray(String[] s)
	{
		int[] ret = new int[s.length];
		for(int i = 0; i < s.length; i++)
		{
			ret[i] = Integer.parseInt(s[i]);
		}
		return ret;
	}
	
	public String getHeaderField(String fieldName)
	{
		return headerFields.get(fieldName);
	}
}
