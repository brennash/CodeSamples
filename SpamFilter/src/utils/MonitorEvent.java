package classifier.utils;

/**
 * Bespoke event object used to monitor execution time and 
 * memory consumption within the Spam Classifier. 
 */

import java.text.NumberFormat;
import java.util.Date;

public class MonitorEvent {

    private Date startTime, stopTime;
    private long startMem, stopMem;
	private boolean valid;
	
	public MonitorEvent()
	{
		valid = false;
	}

	/**
	 * Start the event monitor, recording the start time and memory usage.
	 */
	public void start()
	{
		valid = false;

		
		startMem = (Runtime.getRuntime().totalMemory() 
				- Runtime.getRuntime().freeMemory());
		
		startTime = new Date();
	}
	
	/**
	 * Stop the event monitor, recording the start time and memory usage.
	 */
	public void stop()
	{
		stopTime = new Date();		

		stopMem = (Runtime.getRuntime().totalMemory() 
				- Runtime.getRuntime().freeMemory());
		valid = true;
	}
	
	/**
	 * A function to check if there has been a start event, followed by a 
	 * stop event.
	 * 
	 * @return True if a valid start/stop event has been recorded, false otherwise.
	 */
	public boolean isValid()
	{
		return valid;
	}
	
	/**
	 * Gets the memory usage for the monitor event object. 
	 * 
	 * @return The memory usage in kB, as a long value.
	 */
	public long getMemoryUsage()
	{
		return ((stopMem-startMem)/1024);
	}
	
	
	/**
	 * Gets the elapsed time for the monitor event object. 
	 * 
	 * @return The elapsed time in milliseconds. 
	 */
	public long getElapsedTime()
	{
		return (stopTime.getTime() - startTime.getTime());
	}
	
	/**
	 * @return A string giving the performance monitor details. 
	 */
	public String toString()
	{
		NumberFormat nf = NumberFormat.getInstance();
		String result = "";
		if(this.valid)
		{
			result = result.concat("Elapsed Time: "+nf.format(getElapsedTime())+"ms\n");
			result = result.concat("Memory Usage: "+nf.format(getMemoryUsage())+"kB\n");
			return result;
		}		
		
		return "No Performance Data Available";
	}
	
}
