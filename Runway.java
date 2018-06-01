public class Runway implements Runnable {
	private static int ID_INC = 1;

	private static final String STATUS_TAKEOFF = "Takeoff";
	private static final String STATUS_LANDING = "Landing";
	private static final String STATUS_FREE = "FREE";

	private final int ID;
	private volatile String status;
	private volatile String currentAircraft;
	private volatile boolean run;
	private volatile int departureCount;
	private volatile int arrivalCount;

	private volatile Container<Aircraft> aircraftContainer;

	public Runway(Container<Aircraft> aircraftContainer) {
		this.ID = Runway.ID_INC++;
		this.status = Runway.STATUS_FREE;
		this.currentAircraft = "None";
		this.run = true;

		this.departureCount = 0;
		this.arrivalCount = 0;

		this.aircraftContainer = aircraftContainer;
	}

	public @Override void run() {
		boolean checkForNext = true;
		while (run) {
			synchronized(this) {
				synchronized(aircraftContainer) {
					while (aircraftContainer.isEmpty()) {
						try {
							aircraftContainer.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				Aircraft aircraft = aircraftContainer.getArrayList().get(0);

				synchronized(aircraftContainer) {
					if (aircraft.isRunway() || aircraft.isParked() || !checkForNext) {
						for (Aircraft a : aircraftContainer.getArrayList()) {
							if (!(a.isRunway()) && (aircraft.getID() != a.getID()) && !(a.isParked())) {
								aircraft = a;
								break;
							}
						}
					}
				}

				checkForNext = true;

				// If the plane is flying and ready to land
				synchronized(aircraft) {
					if (aircraft.isFlying()) {
						aircraft.setAtRunway();
						this.arrivalCount++;

						// Landing (Arrivals)
						Util.addLog(String.format("[ID: %d] (2/5) %s is landing at Runway %d in 10seconds", aircraft.getID(), aircraft.getName(), this.getID()), aircraft.getID());

						this.currentAircraft = aircraft.getName();
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
						final int parkTime = (new java.util.Random().nextInt(10) + 5) * 1000;
						Util.addLog(String.format("[ID: %d] (3/5) %s finished landing at Runway %d and is parked in the airport for %dseconds", aircraft.getID(), aircraft.getName(), this.getID(), parkTime/1000), aircraft.getID());
						aircraft.park(System.currentTimeMillis(), parkTime);

						checkForNext = false;
					}
				}

				// If the plane is landed and ready to takeoff
				synchronized(aircraft) {
					if (aircraft.isLanded() && checkForNext) {
						aircraft.setAtRunway();
						this.departureCount++;

						// Takeoff (Departure)
						final int takeOffTime = new java.util.Random().nextInt(5) + 5;
						Util.addLog(String.format("[ID: %d] (4/5) %s is taking off from Runway %d to %s in %dseconds", aircraft.getID(), aircraft.getName(), this.getID(), aircraft.getDestination(), takeOffTime), aircraft.getID());
						this.currentAircraft = aircraft.getName();
						this.status = Runway.STATUS_TAKEOFF;
						try {
							aircraft.wait(takeOffTime*1000);
							aircraft.notifyAll();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						this.status = Runway.STATUS_FREE;
						this.currentAircraft = "None";

						// removing the aircraft from aircraftContainer
						for (int i = 0; i < aircraftContainer.getArrayList().size(); i++) {
							if (aircraftContainer.getArrayList().get(i).getID() == aircraft.getID()) {
								aircraftContainer.remove(i);
								Util.addLog(String.format("[ID: %d] (5/5) %s has flown off from Runway %d to %s", aircraft.getID(), aircraft.getName(), this.getID(), aircraft.getDestination()), aircraft.getID());
								break;
							}
						}
					}
				}

				synchronized(aircraftContainer) {
					for (Aircraft a : aircraftContainer.getArrayList()) {
						a.checkParking();
					}
				}
				
				currentAircraft = "None";

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
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