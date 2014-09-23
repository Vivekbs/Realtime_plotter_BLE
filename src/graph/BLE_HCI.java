package graph;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.time.Millisecond;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import jssc.SerialPort;
import jssc.SerialPortException;

public class BLE_HCI {
	
	
	//define event  2byte code
	static int[] scan_over={0x06,0x01},
				 device_info={0x06,0x0D},
				 event_init={0x06,0x00},
				 event_link={0x06,0x07},
				 event_notification={0x05,0x13},
				 event_data={0x05,0x1b},
				 ter={0x06,0x06};
				
	static int channel=0xf5f5;
	static int fil[]=new int[150];
	static int f,fpointer=0,fsize=1;
	static boolean finit=false,header=false,write=false;
	
	// bt device address
	static int[] device_address=new int[6];
	
	// received packet
	static int[] packet=new int[100];
	static int[] event={0x00,0x00};
	
	//packet size
	static int packet_size;

	// tx dump for discovery ,link and  enable notification
	static int[] tx_init={0x01,0x00, 0xFE, 0x26,0x08,0x05,0x00,0x00, 00 ,00 ,00, 00 ,00 ,00 ,00 ,00, 
		00 ,00 ,00 ,00 ,00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 
		00, 00 ,00 ,00 ,00 ,00 ,01 ,00, 00, 00 
	};
	static int[] tx_discovery={0x01,0x04,0xFE,0x03,0x03,0x01,0x00 };
	static int[] tx_link={0x01,0x09,0xFE,0x09,0x00,0x00,0x00};
	static int[] tx_enable_notification={0x01,0x92,0xFD,0x06 ,0x00,0x00,0x2F,0x00,0x01,0x00 };
	static int[] terminate={01, 0x0A,0xFE,0x03,0x00,0x00,0x13};
	static boolean device_found=false;
	static boolean device_connected=false,ENABLE=true;
	// Port object
	
	SerialPort port;
	PeakTracking track=new PeakTracking();
	
	// method find ble device in the range
	
	public void discover() throws SerialPortException, InterruptedException {
		// TODO Auto-generated method stub

		port.openPort();
		port.setParams(SerialPort.BAUDRATE_115200,SerialPort.DATABITS_8, 
						SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		System.out.println("Scanning....");
		graph_main.text.append("\n Scanning....");
		port.writeIntArray(tx_init);
		process_event(event_init);
		port.writeIntArray(tx_discovery);
		process_event(scan_over);
		// if response return true device found
		if(device_found)
		{
		
			System.out.println("Device Found....");
			graph_main.text.append("\n Device Found....");
			
			port.writeIntArray(tx_link);
			port.writeIntArray(device_address);
			process_event(event_link);
			
			System.out.println("Connection established....");
			graph_main.text.append("\n Connection established....");
			
			port.writeIntArray(tx_enable_notification);
			process_event(event_notification);
			
			System.out.println("Notification enabled.....");
			graph_main.text.append("\n Notification enabled.....");
			device_connected=true;
			while(true)
				process_event(event_data);
		}
		else
		{
			System.out.println("No device found ...");
			System.out.println("Exiting....");
			graph_main.text.append("\n No device found ... \n Exiting....");
			port.closePort();
		}
		
	}

public  void process_event(int[] ev) throws SerialPortException {
		// TODO Auto-generated method stub
	do{
		get_packet();
		if(packet_size>2){
		get_event();
		// if device found copy device address
			event_task();
		}
		}while(!event_equal(ev));
		
	}

	private void event_task() {
		
		
		if(event_equal(device_info))
		{
			for(int i=5;i<11;i++)
				device_address[i-5]=packet[i];
			device_found=true;
		}
		if(event_equal(event_data))
		{
			
			// ############for single plot
			for(int i=8;i<16;i=i+2)
			{
			//System.out.println(Integer.rotateLeft(packet[i+1],8)+packet[i]);
				int temp=Integer.rotateLeft(packet[i+1],8)+packet[i];
				if(temp!=0xffff){
					if(write){
						try {
							write2file(temp,graph_main.filename.getText());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				/*
				header=true;
				
				if(temp==channel){
					header=true;
					continue;
				}
				if(header){
				fil[fpointer]=temp;
				fpointer=(fpointer+1)%fsize;
				if(fpointer==0)
					finit=true;
				
				
					temp=0;
					for(int j=0;j<fsize;j++)
						temp+=fil[j];
						
					
					fil[fpointer]=temp;
					fpointer=(fpointer+1)%fsize;
					temp=0;
					for(int j=0;j<fsize;j++)
						temp+=fil[j];
					*/
				
				track.process_data(temp);
				graph_main.lastValue=temp;//0.07536*50/temp;
		    	graph_main.series.addOrUpdate(new Millisecond(), graph_main.lastValue);
		    	header=false;
				//}//	  
			}
			}
			
			
			/*
			//############# for multi plot
			int start=8;
			int bytes=16;
			int stop=start+bytes;
			for(int i=start;i<stop;i=i+2)
			{
			//System.out.println(Integer.rotateLeft(packet[i+1],8)+packet[i]);
				int temp=Integer.rotateLeft(packet[i+1],8)+packet[i];
				graph_main.lastValue=temp;
				if(temp!=0xffff)
				{
				if(temp>4096 && ENABLE)
				{
		    	graph_main.series.addOrUpdate(new Millisecond(), graph_main.lastValue);
				}
		    	
		    	if(temp<4096 && !ENABLE)
				graph_main.series1.addOrUpdate(new Millisecond(), graph_main.lastValue);
					}
			
			}
			*/
			
		}
		
	
}


private void write2file(int temp, String fname) throws IOException {
		// TODO Auto-generated method stub
		File f=new File("E:/logged_data/"+fname);
		if(!f.exists())
		{
			f.createNewFile();
		}
		BufferedWriter wr=new BufferedWriter(new FileWriter(f,true));
		wr.append(temp+",");
		wr.flush();
		wr.close();
	}

	// compare 2 events
	private boolean event_equal( int[] ev) {
		// TODO Auto-generated method stub
		if(event[0]==ev[0] && event[1]== ev[1])
			return true;
		else
			return false;
	}

// get event code from the packet
	private void get_event() {
		// TODO Auto-generated method stub
		
		//System.out.println("event:"+event[0]+event[1]);
		event[0]=packet[1];
		event[1]=packet[0];
		//System.out.println("event:"+event[0]+event[1]);
	}

// get packet
	private void get_packet() throws SerialPortException {
		
		packet=port.readIntArray(3);
		packet_size=packet[2];
		//System.out.println("Packet_size:"+packet_size);
		packet=port.readIntArray(packet_size);
		
		
	}

// create link with specified parameter
	public void init(String p) throws SerialPortException {
	
		//Define COM PORT
	
		port=new SerialPort(p);		
		final Marker start = new ValueMarker(3400000.0);
        start.setPaint(Color.red);
        start.setLabel("Current Value");
        start.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
        start.setLabelTextAnchor(TextAnchor.TOP_LEFT);
       // plot.addRangeMarker(start);
		graph_main.plot.addRangeMarker(start);
		//open port and configure port
		
		
	}
	
	
	
	
	
}
