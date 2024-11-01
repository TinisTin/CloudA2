package nuber.students;

public class Driver extends Person {

    private Passenger passenger; // Assigned passenger to driver
	
	public Driver(String driverName, int maxSleep)
	{
		super(driverName, maxSleep); // Super class constructor
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
        this.passenger = passenger; // Assign new passenger
        int delay = (int) (Math.random() * maxSleep);
        try {
            Thread.sleep(delay); // Time simulation to take to pick up passenger
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
	        int travelTime = passenger.getTravelTime(); // Gets travel time 
	        try {
	            Thread.sleep(travelTime); // Simulates driving time to destination
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

	
