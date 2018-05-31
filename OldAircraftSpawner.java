class AircraftSpawner implements Runnable {
	private volatile boolean running = true;
	private Container<Aircraft> aircraftContainer;
	private int delay;
	private boolean randomDelay;

	public AircraftSpawner(Container<Aircraft> container) {
		aircraftContainer = container;
		delay = 2;
		randomDelay = true;
	}

	public AircraftSpawner(Container<Aircraft> container, int spawnDelay) {
		this(container);
		delay = spawnDelay;
		randomDelay = false;
	}


	public @Override void run() {
		while (running) {
			try {
				int spawnTime = delay;

				if (randomDelay) {
					spawnTime = new java.util.Random().nextInt(5) + 7;
				}

				Main.addLog(String.format("Creating an Aircraft in %d seconds", spawnTime));
				Thread.sleep(spawnTime*1000);

				aircraftContainer.add(Airport.newAircraft());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		running = false;
	}
}