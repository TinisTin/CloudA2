package nuber.students;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
 */
public class NuberRegion {
	
	private boolean isShuttingDown = false;
	private NuberDispatch dispatch;
	private String regionName;
    private int maxSimultaneousJobs; 
    private final AtomicInteger currentActiveJobs;
	private ConcurrentHashMap<Passenger, CompletableFuture<BookingResult>> bookings;

	/**
	 * Creates a new Nuber region
	 * 
	 * @param dispatch The central dispatch to use for obtaining drivers, and logging events
	 * @param regionName The regions name, unique for the dispatch instance
	 * @param maxSimultaneousJobs The maximum number of simultaneous bookings the region is allowed to process
	 */
	public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs) {
		this.dispatch = dispatch;
		this.regionName = regionName;
		this.maxSimultaneousJobs = maxSimultaneousJobs;
		this.currentActiveJobs = new AtomicInteger(0);
		this.bookings = new ConcurrentHashMap<>();
	}
	
	/**
	 * Creates a booking for the given passenger and adds the booking to the 
	 * collection of jobs to process. Once the region has a position available and a driver is available, 
	 * the booking should commence automatically. 
	 * 
	 * If the region has been told to shut down, this function should return null and log a message to the 
	 * console that the booking was rejected.
	 * 
	 * @param waitingPassenger The passenger who is booking
	 * @return a Future that will provide the final BookingResult object from the completed booking
	 */
	public Future<BookingResult> bookPassenger(Passenger waitingPassenger) {
	    if (isShuttingDown) {
	        dispatch.logEvent(null, "Booking rejected for passenger " + waitingPassenger + " in region " + regionName + ": Region is shutting down.");
	        return null; 
	    }

	    if (currentActiveJobs.get() >= maxSimultaneousJobs) {
	        dispatch.logEvent(null, "Booking accepted for passenger " + waitingPassenger + " in region " + regionName + ": Added to pending booking");
	        return null; 
	    }

	    CompletableFuture<BookingResult> bookingFuture = new CompletableFuture<>();
	    bookings.put(waitingPassenger, bookingFuture); 

	    if (currentActiveJobs.get() < maxSimultaneousJobs) {
	        int activeJobCount = currentActiveJobs.incrementAndGet(); 
	        dispatch.logEvent(null, "Passenger " + waitingPassenger + ": Starting " + regionName);
	        processBooking(waitingPassenger, bookingFuture);
	    }

	    return bookingFuture;
	}
	
	private void processBooking(Passenger passenger, CompletableFuture<BookingResult> bookingFuture) {
	    Driver driver = dispatch.getDriver(); 
	    long tripDuration = calculateTripDuration(); 

	    if (driver != null) {
	        BookingResult result = new BookingResult(-1, passenger, driver, tripDuration); 
	        bookingFuture.complete(result); 
	        dispatch.logEvent(null, "Booking completed for passenger " + passenger + " with driver " + driver + " in region " + regionName);
	    } else {
	        dispatch.logEvent(null, "No driver available for passenger " + passenger + " in region " + regionName);
	        bookingFuture.complete(new BookingResult(-1, passenger, null, 0)); 
	    }

	    int remainingJobs = currentActiveJobs.decrementAndGet(); 
	    dispatch.logEvent(null, "Active bookings: " + remainingJobs + ", pending: " + getBookingsAwaitingDriver());

	}
	
	private long calculateTripDuration() {
		return 0; 
	}

	/**
	 * Called by dispatch to tell the region to complete its existing bookings and stop accepting any new bookings
	 */
	public void shutdown() {
	    isShuttingDown = true; 

	    for (Passenger passenger : bookings.keySet()) {
	        CompletableFuture<BookingResult> future = bookings.get(passenger);
	        if (!future.isDone()) {
	            future.complete(new BookingResult(-1, null, null, 0)); 
	        }
	    }
	    bookings.clear();

	}

	/**
	 * Returns the count of bookings awaiting a driver
	 * @return number of bookings waiting for a driver
	 */
	public int getBookingsAwaitingDriver() {
		return (int) bookings.values().stream().filter(future -> !future.isDone()).count();
	}
}
