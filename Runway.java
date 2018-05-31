public class Runway implements Runnable {
	private static int ID_INC = 1;

	private static final String STATUS_TAKEOFF = "Takeoff";
	private static final String STATUS_LANDING = "Landing";
	private static final String STATUS_FREE = "FREE";

	private final int ID;
	private volatile String status;
	private volatile String currentAircraft;
	private int departureCount;
	private int arrivalCount;
	// BlockingQueue<Aircraft> aircrafts;
	private volatile Container<Aircraft> aircraftContainer;

	public Runway(Container<Aircraft> aircraftContainer) {
		this.aircraftContainer = aircraftContainer;
		this.ID = Runway.ID_INC++;
		this.status = Runway.STATUS_FREE;
		this.currentAircraft = "None";

		this.departureCount = 0;
		this.arrivalCount = 0;
	}

	public @Override void run() {
		boolean checkForNext = true;
		while (true) {
			synchronized(this) {
				// synchronized(this) {
				// synchronized(aircraftContainer) {
				while (aircraftContainer.isEmpty()) {
					try {
						Thread.sleep(1000);
						// aircraftContainer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// }

				// Main.addLog("SLKS" + getName());
				Aircraft aircraft = aircraftContainer.getArrayList().get(0);

				if (aircraft.isRunway() || aircraft.isParked() || !checkForNext) {
					for (Aircraft a : aircraftContainer.getArrayList()) {
						if (!(a.isRunway()) && (aircraft.getID() != a.getID()) && !(a.isParked())) {
							aircraft = a;
							break;
						}
					}
				}

				checkForNext = true;
				currentAircraft = aircraft.getName();

				// Main.addLog(String.format("%d %s", ID, aircraft.getName()));
				synchronized(aircraft) {

					if (aircraft.isFlying()) {
						aircraft.setAtRunway();

						Main.addLog(String.format("[ID: %d] (2/5) %s is landing at Runway %d in 10seconds", aircraft.getID(), aircraft.getName(), this.getID()), aircraft.getID());
						this.status = Runway.STATUS_LANDING;
						try {
							aircraft.wait(10*1000);
							aircraft.notifyAll();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						this.status = Runway.STATUS_FREE;
						this.departureCount++;
						aircraft.unsetAtRunway();
						aircraft.switchStatus();

						final int parkTime = (new java.util.Random().nextInt(5) + 5) * 1000;
						Main.addLog(String.format("[ID: %d] (3/5) %s finished landing at Runway %d and is parked in the airport for %dseconds", aircraft.getID(), aircraft.getName(), this.getID(), parkTime/1000), aircraft.getID());
						aircraft.park(System.currentTimeMillis(), parkTime);

						checkForNext = false;
						// aircraft.notifyAll();
							// }
					}
				// }

				// synchronized(aircraft) {
					if (aircraft.isLanded() && checkForNext) {
							// synchronized(aircraft) {
						aircraft.setAtRunway();

						final int takeOffTime = new java.util.Random().nextInt(5) + 5;
						Main.addLog(String.format("[ID: %d] (4/5) %s is taking off from Runway %d to %s in %dseconds", aircraft.getID(), aircraft.getName(), this.getID(), aircraft.getDestination(), takeOffTime), aircraft.getID());
						this.status = Runway.STATUS_TAKEOFF;
						try {
							aircraft.wait(takeOffTime*1000);
							aircraft.notifyAll();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						this.status = Runway.STATUS_FREE;
							// }

						for (int i = 0; i < aircraftContainer.getArrayList().size(); i++) {
							if (aircraftContainer.getArrayList().get(i).getID() == aircraft.getID()) {
								this.arrivalCount++;
								aircraftContainer.remove(i);
								Main.addLog(String.format("[ID: %d] (5/5) %s has flown off from Runway %d to %s", aircraft.getID(), aircraft.getName(), this.getID(), aircraft.getDestination()), aircraft.getID());
								break;
							}
						}
					}

				// 	aircraft.notifyAll();
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// Main.addLog(String.format("%d %s END", ID, aircraft.getStatus()));

				for (Aircraft a : aircraftContainer.getArrayList()) {
					a.checkParking();
				}

				currentAircraft = "None";
			}
		}
	}

	public int getID() { return ID; }
	public String getName() { return "Runway " + ID; }
	public String getStatus() { return status; }
	public int getDepartureCount() { return departureCount; }
	public int getArrivalCount() { return arrivalCount; }
}