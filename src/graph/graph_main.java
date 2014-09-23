package graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;



public class graph_main extends ApplicationFrame implements ActionListener{
 
    public static TimeSeries series;
    public static TimeSeries series1;
    private static SerialPort serialPort;
    public static double lastValue = 10.0;
    private Timer timer = new Timer(1, this);
    DynamicTimeSeriesCollection dataset;
    final JFreeChart chart;
    public static XYPlot plot;
    public static JTextArea text;
    final JButton button1,button2,button3,button4;
    JButton button5;
    public static JTextField filename;
    public static JTextArea BPM;
    static JComboBox list;
    static BLE_HCI ble=new BLE_HCI();
    boolean connection=false;
    static int range=0;
    ValueAxis xaxis ;
    
    public graph_main(final String title) {
        super(title);
        this.series = new TimeSeries("Sensor Value 1", Millisecond.class);
        this.series1= new TimeSeries("Sensor Value 2", Millisecond.class);
      
       // final TimeSeriesCollection dataset = new TimeSeriesCollection(this.series);
       // chart= createChart(dataset);
        
        
        /////////////
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.setDomainIsPointsInTime(true);
        
       
       
        dataset.addSeries(series);
        dataset.addSeries(series1);
        chart= createChart(dataset);
        ////////////
        
        final ChartPanel chartPanel = new ChartPanel(chart);
        
        button1 = new JButton("Ports");
        button1.setActionCommand("port");
        button1.addActionListener(this);
        
        button2= new JButton("Scan");
        button2.setActionCommand("scan");
        button2.addActionListener(this);
        
        button3 = new JButton("Zoom in");
        button3.setActionCommand("zoom_in");
        button3.addActionListener(this);
        
        button4 = new JButton("Zoom out");
        button4.setActionCommand("zoom_out");
        button4.addActionListener(this);
        
        button5 =new JButton("save");
        button5.setActionCommand("save");
        button5.addActionListener(this);;
        
        filename=new JTextField("file name");
        
        Font font1 = new Font("SansSerif", Font.BOLD, 20);
        BPM=new JTextArea("BPM:");
        BPM.setForeground(Color.RED);
        BPM.setFont(font1);
   
        JPanel p3=new JPanel();
        list=new JComboBox();
        p3.add(list);
        p3.add(button1);
        p3.add(BPM);
        
        text=new JTextArea("Ports available:");
        text.setSize(400, 400);
  
         JPanel p1 = new JPanel();
         p1.add(button2);
         p1.add(button3);
         p1.add(button4);
         p1.add(filename);
         p1.add(button5);
        
         JPanel p2=new JPanel(new BorderLayout());
         p2.add(text,BorderLayout.PAGE_END);
         p2.add(p1,BorderLayout.LINE_END);
         p2.add(p3,BorderLayout.LINE_START);
        
        
        final JPanel content = new JPanel(new BorderLayout());
     
        content.add(chartPanel);
        content.add(p2,BorderLayout.PAGE_END);
     
        
        
        timer.setInitialDelay(0);
        chart.setBackgroundPaint(Color.LIGHT_GRAY); 
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        setContentPane(content);
        timer.setActionCommand("timer");
        timer.start();
        
      
    }

    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            "Real time plotter",
            "x",
            "y",
            dataset,
            true,
            true,
            false
        );

        final ChartPanel chartPanel = new ChartPanel(result);
        final JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(content);
        
        
         plot = result.getXYPlot();

        plot.setBackgroundPaint(new Color(0xffffff));
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.lightGray);
        
       
       
        
        
        xaxis = plot.getDomainAxis();
        xaxis.setAutoRange(true);

        xaxis.setFixedAutoRange(3000.0+range);
        xaxis.setVerticalTickLabels(true);

        ValueAxis yaxis = plot.getRangeAxis();
        yaxis.setAutoRange(true);//setRange(0.0,64450);
        return result;
    }

    public void actionPerformed(final ActionEvent e) {
       
    	
    	if(e.getActionCommand().equals("port"))
    	{
    		SerialPortList plist=new SerialPortList();
    		String[] ports=plist.getPortNames();
    		//text.setText("Ports available:");
    		list.removeAllItems();
    		for(int i=0;i<ports.length;i++)
    		{
    			list.addItem(ports[i]);
    			//text.append(ports[i]+" ");
    		}
    	}
    	
    	if(e.getActionCommand().equals("scan"))
    	{
    		System.out.println(list.getSelectedItem());
    		
			
    		
    			new Thread(new Runnable() {
    			    @Override public void run() {
    			        // do stuff in this thread
    			    	try {
    			    		ble.init(list.getSelectedItem().toString());
    						ble.discover();
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				
    			    }
    			}).start();
				
				
				
			
    	}
    	
    	if(e.getActionCommand().equals("zoom_in"))
    	{
    		range=range-500;
    		if((3000-range)>100)
    		 xaxis.setFixedAutoRange(3000.0+range);
    		
    	}
    	if(e.getActionCommand().equals("zoom_out"))
    	{
    		range=range+500;
   		 xaxis.setFixedAutoRange(3000.0+range);
    	}
    	
    	if(e.getActionCommand().equals("save"))
    	{
    		if(ble.write)
    		{
    			ble.write=false;
    			button5.setText("save");
    		}
    		else
    		{
    			ble.write=true;
    			button5.setText("stop");
    		}
    	}
    }

  

	

	public static void main(final String[] args) throws SerialPortException, InterruptedException {
		
        final graph_main demo = new graph_main("Plotter");
        demo.pack();

        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
       
    }

	

	

 
    
    
}