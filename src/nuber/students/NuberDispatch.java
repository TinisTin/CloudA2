package nuber.students;



import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The core Dispatch class that instantiates and manages everything for Nuber
 * 
 * @author james
 *
 */
public class NuberDispatch {

	/**
	 * The maximum number of idle drivers that can be awaiting a booking 
	 */
	private final int MAX_DRIVERS = 999;
	
	private boolean logEvents = false;
	
	private Map<String, NuberRegion> regions;
	
	private Queue<Driver> idleDrivers;
	
	/**
	 * Creates a new dispatch objects and instantiates the required regions and any other objects required.
	 * It should be able to handle a variable number of regions based on the HashMap provided.
	 * 
	 * @param regionInfo Map of region names and the max simultaneous bookings they can handle
	 * @param logEvents Whether logEvent should print out events passed to it
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents)
	{
		this.logEvents = logEvents;
		this.idleDrivers = new ConcurrentLinkedQueue<>();
		this.regions = new HashMap<>();
		
		for (String regionName : regionInfo.keySet()) {
			int maxBookings = regionInfo.get(regionName);
			this.regions.put(regionName, new NuberRegion(this, regionName, maxBookings));
		}
	}
	
	/**
	 * Adds drivers to a queue of idle driver.
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @param The driver to add to the queue.
	 * @return Returns true if driver was added to the queue
	 */
	public boolean addDriver(Driver newDriver) {
		if (idleDrivers.size() < MAX_DRIVERS) {
			return idleDrivers.offer(newDriver);
		}
		return false;
	}
	
	/**
	 * Gets a driver from the front of the queue
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @return A driver that has been removed from the queue
	 */
	public Driver getDriver()
	{
		return idleDrivers.poll();
	}

	/**
	 * Prints out the string
	 * 	    booking + ": " + message
	 * to the standard output only if the logEvents variable passed into the constructor was true
	 * 
	 * @param booking The booking that's responsible for the event occurring
	 * @param message The message to show
	 */
	public void logEvent(Booking booking, String message) {
		
		if (!logEvents) return;
		
		System.out.println(booking + ": " + message);
		
	}

	/**
	 * Books a given passenger into a given Nuber region.
	 * 
	 * Once a passenger is booked, the getBookingsAwaitingDriver() should be returning one higher.
	 * 
	 * If the region has been asked to shutdown, the booking should be rejected, and null returned.
	 * 
	 * @param passenger The passenger to book
	 * @param region The region to book them into
	 * @return returns a Future<BookingResult> object
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
	    // Log the attempt to book
	    logEvent(null, "Attempting to book passenger: " + passenger + " in region: " + region);
	    
	    // Check if the specified region exists
	    NuberRegion selectedRegion = regions.get(region);
	    
	    if (selectedRegion == null) {
	        logEvent(null, "Failed booking - Region '" + region + "' does not exist.");
	        return null; 
	    }
	    
	    // Try booking the passenger in the selected region
	    Future<BookingResult> result = selectedRegion.bookPassenger(passenger);
	    
	    if (result == null) {
	        logEvent(null, "Booking could not proceed in region: " + region + " - region may be shutting down.");
	    } else {
	        logEvent(null, "Booking successfully processed in region: " + region);
	    }
	    return result;
	}



	/**
	 * Gets the number of non-completed bookings that are awaiting a driver from dispatch
	 * 
	 * Once a driver is given to a booking, the value in this counter should be reduced by one
	 * 
	 * @return Number of bookings awaiting driver, across ALL regions
	 */
	public int getBookingsAwaitingDriver() {
	    int totalBookingsAwaitingDriver = 0;

	    for (NuberRegion region : regions.values()) {
	        totalBookingsAwaitingDriver += region.getBookingsAwaitingDriver();
	    }
	    
	    return totalBookingsAwaitingDriver;

	}
	
	/**
	 * Tells all regions to finish existing bookings already allocated, and stop accepting new bookings
	 */
	public void shutdown() {
	    for (NuberRegion region : regions.values()) {
	        region.shutdown();
	    }
	}

	public void releaseDriver(Driver driver) {
		addDriver(driver);		
	}

}
