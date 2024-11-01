package nuber.students;

public class Driver extends Person {

    private Passenger passenger;
	
	public Driver(String driverName, int maxSleep)
	{
		super(driverName, maxSleep);
		this.passenger = null;	
		
		
	}
	
	/**
	 * Stores the provided passenger as the driver's current passenger and then
	 * sleeps the thread for between 0-maxDelay milliseconds.
	 * 
	 * @param newPassenger Passenger to collect
	 * @throws InterruptedException
	 */
	public void pickUpPassenger(Passenger passenger) {
        this.passenger = passenger;
        int delay = (int) (Math.random() * maxSleep);
        try {
            Thread.sleep(delay);	
            logEvent(getId() + ":D-" + name + ":" + passenger.name + ": Collected passenger, on way to destination");
        } catch (InterruptedException e) {
            logEvent("Driver interrupted: " + e.getMessage());
        }
    }

	/**
	 * Sleeps the thread for the amount of time returned by the current 
	 * passenger's getTravelTime() function
	 * 
	 * @throws InterruptedException
	 */
	 public void driveToDestination() {
	        int travelTime = passenger.getTravelTime();
	        try {
	            Thread.sleep(travelTime);
	            logEvent(getId() + ":D-" + name + ":" + passenger.name + ": At destination, driver is now free");
	        } catch (InterruptedException e) {
	            logEvent("Driver interrupted: " + e.getMessage());
	        }
	    }
	 
	 private void logEvent(String message) {
	        System.out.println(message); 
	    }
	 
	 public String toString() {
	        return name; 
	    }
	}

	
