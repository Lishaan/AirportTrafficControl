import java.util.concurrent.atomic.AtomicInteger;

// The Runway class is used to process aircrafts as they get added into the aircraft container. 
public class Runway implements Runnable {
	private static int ID_INC = 1;

	private static final String STATUS_TAKEOFF = "Takeoff";
	private static final String STATUS_LANDING = "Landing";
	private static final String STATUS_FREE = "Free";

	private volatile boolean run;

	// Whether the runway should take off the current aircraft
	private boolean shouldTakeoff;

	private final int ID;
	private String status;
	private String currentAircraft;

	private final AtomicInteger departureCount;
	private final AtomicInteger arrivalCount;

	private final Container<Aircraft> aircraftContainer;

	public Runway(Container<Aircraft> aircraftContainer) {
		this.run = true;
		this.shouldTakeoff = true;

		this.ID = Runway.ID_INC++;
		this.status = Runway.STATUS_FREE;
		this.currentAircraft = "None";
		this.departureCount = new AtomicInteger(0);
		this.arrivalCount = new AtomicInteger(0);

		this.aircraftContainer = aircraftContainer;
	}

	public @Override void run() {
		while (run) {
			this.shouldTakeoff = true;

			final Aircraft aircraft = fetchAircraft();

			landing(aircraft);
			takeoff(aircraft);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// Fetch the next free aircraft from the aircraft container
	private Aircraft fetchAircraft() {
		Aircraft aircraft = new Aircraft();

		synchronized(aircraftContainer) {
			while (aircraftContainer.isEmpty()) {
				try {
					aircraftContainer.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			aircraft = aircraftContainer.getArrayList().get(0);

			if (aircraft.isRunway() || aircraft.isParked() || !shouldTakeoff) {
				for (Aircraft a : aircraftContainer.getArrayList()) {
					if (!(a.isRunway()) && (aircraft.getID() != a.getID()) && !(a.isParked())) {
						aircraft = a;
						break;
					}
				}
			}
		}

		return aircraft;
	}

	// If the aircraft is flying, then the runway should land the aircraft and set it for parking
	private void landing(Aircraft aircraft) {

		// If the plane is flying and ready to land
		synchronized(aircraft) {
			if (aircraft.isFlying()) {
				aircraft.setAtRunway();
				arrivalCount.incrementAndGet();

				// Landing (Arrivals)
				aircraft.setStage(2);
				Util.addLog(String.format("[ID: %d] (2/5) %s is landing at Runway %d in 10seconds", aircraft.getID(), aircraft.getName(), this.getID()), aircraft.getID());

				this.currentAircraft = aircraft.toString();
				this.status = Runway.STATUS_LANDING;
				
				try {
					aircraft.wait(10*1000);
					aircraft.notifyAll();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				this.status = Runway.STATUS_FREE;
				this.currentAircraft = "None";
				
				aircraft.unsetAtRunway();
				aircraft.switchStatus();

				// Parking
				aircraft.setStage(3);
				final int parkTime = Util.getRandomInt(5, 10);
				Util.addLog(String.format("[ID: %d] (3/5) %s finished landing at Runway %d and is parked in the airport for %dseconds", aircraft.getID(), aircraft.getName(), this.getID(), parkTime), aircraft.getID());
				aircraft.park(parkTime*1000);

				this.shouldTakeoff = false;
			}
		}
	}

	// If the aircraft is landed, and if it hasn't landed from this runway before, the runway will process its takeoff and remove it
	private void takeoff(Aircraft aircraft) {
		boolean shouldRemove = false;
		final int extraTakeOffTime;
		final boolean isTakingOff = getTakingOff();

		// if there's no other aircraft that is going to take off, the aircraft can take longer duration to take off.
		if (!isTakingOff) {
			extraTakeOffTime = Util.getRandomInt(2, 5);
		} else {
			extraTakeOffTime = 0;
		}

		synchronized(aircraft) {
			if (aircraft.isLanded() && this.shouldTakeoff) {
				aircraft.setAtRunway();
				this.departureCount.incrementAndGet();

				// Takeoff (Departure)
				aircraft.setStage(4);
				final int takeOffTime = Util.getRandomInt(5, 10) + extraTakeOffTime;
				Util.addLog(
					String.format("[ID: %d] (4/5) %s is taking off from Runway %d to %s in %dseconds %s", 
						aircraft.getID(), aircraft.getName(), this.getID(), aircraft.getDestination(), takeOffTime, 
						(!isTakingOff) ? String.format("(extra %dseconds)", extraTakeOffTime) : ""
					), aircraft.getID()
				);
				this.currentAircraft = aircraft.toString();
				this.status = Runway.STATUS_TAKEOFF;
				try {
					aircraft.wait(takeOffTime*1000);
					aircraft.notifyAll();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.status = Runway.STATUS_FREE;
				this.currentAircraft = "None";

				shouldRemove = true;
			}
		}
		
		// removing the aircraft from aircraftContainer
		if (shouldRemove) {
			for (int i = 0; i < aircraftContainer.getArrayList().size(); i++) {
				if (aircraftContainer.getArrayList().get(i).getID() == aircraft.getID()) {
					aircraftContainer.remove(i);
					Util.addLog(String.format("[ID: %d] (5/5) %s has flown off from Runway %d to %s", aircraft.getID(), aircraft.getName(), this.getID(), aircraft.getDestination()), aircraft.getID());
					break;
				}
			}
		}

		// Checks for parking 
		for (Aircraft a : aircraftContainer.getArrayList()) {
			a.checkParking();
		}
	}

	public void stop() { this.run = false; }

	public int getID() { return ID; }
	public String getName() { return "Runway " + ID; }
	public String getStatus() { return status; }
	public String getCurrentAircraft() { return currentAircraft; }
	public int getDepartureCount() { return departureCount.get(); }
	public int getArrivalCount() { return arrivalCount.get(); }

	private boolean getTakingOff() {
		for (Aircraft aircraft : aircraftContainer.getArrayList()) {
			if (aircraft.isTakingOff()) {
				return true;
			}
		}

		return false;
	}
}