import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		Util.clearScreen();
		System.out.print("Enter simulation time (seconds): ");
		final long duration = (new java.util.Scanner(System.in).nextLong()) * 1000;

		Util.initLogFile(duration);
		final int logsCount = 10;
		
		final long startTime = System.currentTimeMillis();
		final long endTime = startTime + duration;

		Container<Aircraft> aircraftContainer = new Container<Aircraft>(Airport.CAPACITY);
		Spawner aircraftSpawner = new Spawner(aircraftContainer);
		Runway[] runways = new Runway[] {
			new Runway(aircraftContainer),
			new Runway(aircraftContainer),
			new Runway(aircraftContainer)
		};

		Thread spawnerThread = new Thread(aircraftSpawner);
		ExecutorService runwayExecutor = Executors.newFixedThreadPool(runways.length);

		spawnerThread.start();

		for (Runway runway : runways) {
			runwayExecutor.execute(runway);
		}

		// Display Thread
		boolean ended = false;

		do {
			Util.clearScreen();
			Util.printCurrentTime(startTime, duration);
			synchronized(aircraftContainer) {
				Util.printAircraftStatuses(aircraftContainer.getArrayList());
			}
			Util.printRunwayStatuses(runways);
			Util.printLogs(logsCount);

			if (!(System.currentTimeMillis() <= endTime)){
				aircraftSpawner.stop();
			}

			ended = !(System.currentTimeMillis() <= endTime) && (aircraftContainer.isEmpty());

			Thread.sleep(1000);

		} while (!ended);

		runwayExecutor.shutdown();

		while (!runwayExecutor.isTerminated()) {
			if (ended) {
				Util.writeToLogsSorted();
				Util.printRunwayStats(runways);
				break;
			}
		}

		System.exit(0);
	}
}