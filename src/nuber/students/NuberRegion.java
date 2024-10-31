package nuber.students;

/**
 * A single Nuber region that operates independently of other regions, other than getting 
 * drivers from bookings from the central dispatch.
 * 
 * A region has a maxSimultaneousJobs setting that defines the maximum number of bookings 
 * that can be active with a driver at any time. For passengers booked that exceed that 
 * active count, the booking is accepted, but must wait until a position is available, and 
 * a driver is available.
 * 
 * Bookings do NOT have to be completed in FIFO order.
 * 
 * @author james
 *
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;



public class NuberRegion {
	
	private NuberDispatch dispatch;
	private String regionName;
    private int maxSimultaneousJobs; 
    private final AtomicInteger currentActiveJobs;
	private ConcurrentHashMap<Passenger, Future<BookingResult>> bookings;
	private int bookingsAwaitingDriver;
	
	/**
	 * Creates a new Nuber region
	 * 
	 * @param dispatch The central dispatch to use for obtaining drivers, and logging events
	 * @param regionName The regions name, unique for the dispatch instance
	 * @param maxSimultaneousJobs The maximum number of simultaneous bookings the region is allowed to process
	 */
	public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs)
	{
		 this.dispatch = dispatch;
		 this.regionName = regionName;
	     this.maxSimultaneousJobs = maxSimultaneousJobs;
	     this.currentActiveJobs = new AtomicInteger(0);
	     this.bookings = new ConcurrentHashMap<>();
		

	}
	
	/**
	 * Creates a booking for given passenger, and adds the booking to the 
	 * collection of jobs to process. Once the region has a position available, and a driver is available, 
	 * the booking should commence automatically. 
	 * 
	 * If the region has been told to shutdown, this function should return null, and log a message to the 
	 * console that the booking was rejected.
	 * 
	 * @param waitingPassenger
	 * @return a Future that will provide the final BookingResult object from the completed booking
	 */
	public Future<BookingResult> bookPassenger(Passenger waitingPassenger) {
	    if (currentActiveJobs.get() >= maxSimultaneousJobs) {
	        dispatch.logEvent(null, "Booking rejected for passenger " + waitingPassenger + " in region " + regionName + ": Max bookings reached.");
	        return null; 
	    }

	    CompletableFuture<BookingResult> bookingFuture = new CompletableFuture<>();
	    bookings.put(waitingPassenger, bookingFuture); 

	    int activeJobCount = currentActiveJobs.incrementAndGet();

	    dispatch.logEvent(null, "Passenger " + waitingPassenger + " booked in region " + regionName + ". Active jobs: " + activeJobCount);

	    processBooking(waitingPassenger, bookingFuture);

	    return bookingFuture;
	}
	
	private void processBooking(Passenger passenger, CompletableFuture<BookingResult> bookingFuture) {
	
	}
	
	/**
	 * Called by dispatch to tell the region to complete its existing bookings and stop accepting any new bookings
	 */
	public void shutdown()
	{
	}

	public int getBookingsAwaitingDriver() {
		// TODO Auto-generated method stub
		return 0;
	}
		
}
