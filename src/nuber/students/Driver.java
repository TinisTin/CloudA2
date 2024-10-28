package nuber.students;

public class Driver extends Person {

	private Passenger currentPassenger;
	
	public Driver(String driverName, int maxSleep)
	{
		super(driverName, maxSleep);
		currentPassenger = null;
	}
	
	/**
	 * Stores the provided passenger as the driver's current passenger and then
	 * sleeps the thread for between 0-maxDelay milliseconds.
	 * 
	 * @param newPassenger Passenger to collect
	 * @throws InterruptedException
	 */
	 public void pickUpPassenger(Passenger newPassenger) {
	        currentPassenger = newPassenger; // Store the current passenger
	        try {
	            Thread.sleep((int) (Math.random() * maxSleep)); // Sleep for a random time
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt(); // Restore interrupted status
	        }
	        System.out.println(name + " picked up " + newPassenger.name); // Print statement for pickup action 
	    }

	/**
	 * Sleeps the thread for the amount of time returned by the current 
	 * passenger's getTravelTime() function
	 * 
	 * @throws InterruptedException
	 */
	 public void driveToDestination() {
		    if (currentPassenger != null) { // Checks if there is a current passenger
		        int travelTime = currentPassenger.getTravelTime(); // 
		        try {
		            Thread.sleep(travelTime); 
		        } catch (InterruptedException e) {
		            Thread.currentThread().interrupt(); // Restore interrupted status
		        }
		        System.out.println(name + " drove " + currentPassenger.name + " to destination."); // Print Statement for Destination action
		        currentPassenger = null; 
		    }
		}
	
}
