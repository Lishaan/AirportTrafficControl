public class Runway implements Runnable {
	private static int ID_INC = 1;

	private static final String STATUS_TAKEOFF = "Takeoff";
	private static final String STATUS_LANDING = "Landing";
	private static final String STATUS_FREE = "FREE";

	private volatile boolean run;

	private boolean shouldTakeoff;

	private final int ID;
	private String status;
	private String currentAircraft;
	private int departureCount;
	private int arrivalCount;

	private final Container<Aircraft> aircraftContainer;

	public Runway(Container<Aircraft> aircraftContainer) {
		this.run = true;
		this.shouldTakeoff = true;

		this.ID = Runway.ID_INC++;
		this.status = Runway.STATUS_FREE;
		this.currentAircraft = "None";
		this.departureCount = 0;
		this.arrivalCount = 0;

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

	private void landing(Aircraft aircraft) {

		// If the plane is flying and ready to land
		synchronized(aircraft) {
			if (aircraft.isFlying()) {
				aircraft.setAtRunway();
				this.arrivalCount++;

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

	private void takeoff(Aircraft aircraft) {
		boolean shouldRemove = false;

		// If the plane is landed and ready to takeoff
		synchronized(aircraft) {
			if (aircraft.isLanded() && shouldTakeoff) {
				aircraft.setAtRunway();
				this.departureCount++;

				// Takeoff (Departure)
				aircraft.setStage(4);
				final int takeOffTime = Util.getRandomInt(5, 10);
				Util.addLog(String.format("[ID: %d] (4/5) %s is taking off from Runway %d to %s in %dseconds", aircraft.getID(), aircraft.getName(), this.getID(), aircraft.getDestination(), takeOffTime), aircraft.getID());
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
	public int getDepartureCount() { return departureCount; }
	public int getArrivalCount() { return arrivalCount; }
}