import java.util.Random;

public class Spawner implements Runnable {
	private volatile Container<Aircraft> aircraftContainer;
	private Random randGenerator;

	public Spawner(Container<Aircraft> container) {
		aircraftContainer = container;
		randGenerator = new java.util.Random();
	}

	public @Override void run() {
		// int spawnCount = 2;
		// for (int i = 0; i < spawnCount; i++) {
		while (true) {
			// int spawnTime = 2;
			int spawnTime = randGenerator.nextInt(5) + 3;
			// Thread.sleep(3000); // Wait time

			Util.addLog(String.format("Creating an Aircraft in %d seconds", spawnTime), 0);
			try {
				Thread.sleep(spawnTime*1000);
				aircraftContainer.add(Airport.newAircraft());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}