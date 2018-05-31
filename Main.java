import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Main {
	public static void main(String[] args) {
		Util.clearScreen();
		System.out.print("Enter simulation time (seconds): ");
		final long duration = (new java.util.Scanner(System.in).nextLong()) * 1000;
		Util.initLogFile(duration);
		
		final long startTime = System.currentTimeMillis();
		final long endTime = startTime + duration;

		Container<Aircraft> aircraftContainer = new Container<Aircraft>(Airport.CAPACITY);
		Spawner aircraftSpawner = new Spawner(aircraftContainer);
		Runway[] runways = new Runway[] {
			new Runway(aircraftContainer),
			new Runway(aircraftContainer),
			new Runway(aircraftContainer)
		};

		ExecutorService runwayExecutor = Executors.newFixedThreadPool(3);
		ExecutorService spawnerExecutor = Executors.newCachedThreadPool();

		for (Runway runway : runways) {
			runwayExecutor.execute(runway);
		}

		spawnerExecutor.execute(aircraftSpawner);

		do {
			Util.clearScreen();

			Util.printCurrentTime(startTime, duration);
			synchronized(aircraftContainer) {
				Util.printAircraftStatuses(aircraftContainer.getArrayList());
			}
			Util.printRunwayStatuses(runways);
			Util.printLogs(20);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while ((System.currentTimeMillis() <= endTime) || (!aircraftContainer.isEmpty()));

		runwayExecutor.shutdown();
		spawnerExecutor.shutdown();

		Util.writeToLogsSorted();
		Util.printRunwayStats(runways);

		System.exit(0);
	}
}