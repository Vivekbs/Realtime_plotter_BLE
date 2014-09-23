package graph;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PeakTracking {

	
	static int max=0,time=0,index=0,count=0,min=0,inc=0,m1;
	static boolean start=false,mnt=true,s=false;
	public void process_data(int temp) {
		// TODO Auto-generated method stub
		time=time+1;
		if(mnt){
			if(max<temp)
			{
				max=temp;
				index=time;
				start=true;
			}
			else 
				if(start)
				{
					count=count+1;
				}
			if(count==150)
			{
				//System.out.println("max:"+max+" at pos. "+index);
				graph_main.BPM.setBackground(Color.GREEN);
				count=0;
				min=(int)Math.pow(2, 12);
				start=false;
				mnt=false;
			}
		}
		
		else
		{
			if(min>temp)
			{
				min=temp;
				start=true;
				index=time;
			}
			else if(start)
				count=count+1;
			if(count==30)
			{
				//System.out.println("min:"+min+" at pos. "+index);
				graph_main.BPM.setBackground(Color.YELLOW);
				
				count=0;
				max=0;
				start=false;
				mnt=true;
				if(!s){
					s=true;
					m1=index;
				}
				inc=(inc+1)%10; 
				
				if(inc==0)
				{
					int dx=index-m1;
					double bpm=(double)(400*9)/dx*60;
					System.out.println("BPM:"+bpm);
					graph_main.BPM.setText("BPM:"+(int)bpm);
					try {
						write2file(bpm,"bpm.csv");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					s=false;
					time=0;
				}
			}
		}
		
		
	}
	private void write2file(double temp, String fname) throws IOException {
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
	

}
