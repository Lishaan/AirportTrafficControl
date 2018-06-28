public class Spawner implements Runnable {
	private volatile Container<Aircraft> aircraftContainer;
	private volatile boolean run;
	private long endTime;

	public Spawner(Container<Aircraft> container) {
		aircraftContainer = container;
		run = true;
		endTime = -1;
	}

	public @Override void run() {
		while (run) {
			synchronized (this) {
				try {
					int spawnTime = new java.util.Random().nextInt(5) + 4;

					Util.addLog(String.format("Creating an Aircraft in %d seconds", spawnTime), 0);

					wait(spawnTime*1000);

					aircraftContainer.add(Airport.newAircraft());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized void stop() {
		run = false;
		if (endTime == -1) {
			endTime = System.currentTimeMillis();
		}
	}

	public String getEndTime() {
		return Util.formatTime(endTime);
	}
}