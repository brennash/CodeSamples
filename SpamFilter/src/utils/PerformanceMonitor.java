package classifier.utils;

/**
 * This interface is used to implement a simple performance monitor. 
 * A MonitorEvent records the execution times, and memory usage 
 * between successive calls. 
 * 
 * @author Shane Brennan
 * @date 23rd July 2011
 */

public interface PerformanceMonitor
{
		public void monitorStart(MonitorEvent event);
		public void monitorStop(MonitorEvent event);
}
