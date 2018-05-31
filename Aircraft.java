public class Aircraft {
	private static int ID_INC = 1;
	private static final String STATUS_FLYING = "Flying";
	private static final String STATUS_LANDED = "Landed";
	private static final String STATUS_PARKED = "Parked";
	private static final String STATUS_RUNWAY = "Runway";

	private final int ID;
	private final String name;
	private final String destination;
	
	private volatile String status; // Flying -> Runway -> Parked -> Landed -> Runway
	private String prevStatus;

	private long releaseTime = -1;

	public Aircraft(String name, String destination) {
		this.ID = Aircraft.ID_INC++;
		this.name = name;
		this.destination = destination;

		status = (new java.util.Random().nextInt(100) > 50) ? STATUS_FLYING : STATUS_LANDED;
		prevStatus = null;
		
		if (isLanded()) {
			Util.addLog(String.format("[ID: %d] (1/5) %s created and is waiting for depature to %s", ID, name, destination), ID);
		} else if (isFlying()) {
			Util.addLog(String.format("[ID: %d] (1/5) %s created and is arriving soon from %s", ID, name, destination), ID);
		}
	}

	public Aircraft copy() { return this; }

	public synchronized void switchStatus() {
		if (status.equals(STATUS_FLYING)) {
			status = STATUS_LANDED;
		} else {
			status = STATUS_FLYING;
		}
	}

	public synchronized void setAtRunway() {
		prevStatus = status;
		status = STATUS_RUNWAY;
	}

	public synchronized void unsetAtRunway() {
		if (prevStatus == null) {
			prevStatus = status;
		} else {
			status = prevStatus;
		}
	}

	public synchronized void park(long parkedTime, long wait) {
		setParked();
		releaseTime = parkedTime + wait;
	}

	public synchronized void checkParking() {
		if (releaseTime != -1) {
			if (System.currentTimeMillis() >= releaseTime) {
				unsetParked();
				releaseTime = -1;
			}
		} 
	}

	public synchronized void setParked() {
		prevStatus = status;
		status = STATUS_PARKED;
	}

	public synchronized void unsetParked() {
		if (prevStatus == null) {
			prevStatus = status;
		} else {
			status = prevStatus;
		}
	}

	public synchronized boolean isFlying() { return status.equals(STATUS_FLYING); }
	public synchronized boolean isLanded() { return status.equals(STATUS_LANDED); }
	public synchronized boolean isRunway() { return status.equals(STATUS_RUNWAY); }
	public synchronized boolean isParked() { return status.equals(STATUS_PARKED); }

	public synchronized int getID() { return ID; }
	public synchronized String getName() { return name; }
	public synchronized String getStatus() { return status; }
	public synchronized String getDestination() { return destination; }
}