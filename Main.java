import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		Util.clearScreen();
		System.out.print("Enter simulation time (seconds): ");
		final long duration = (new java.util.Scanner(System.in).nextLong()) * 1000;

		Util.initLogFile(duration);
		final int logsCount = 5;
		
		final long startTime = System.currentTimeMillis();
		final long endTime = startTime + duration;

		final Container<Aircraft> aircraftContainer = new Container<Aircraft>(Airport.CAPACITY);
		final Spawner aircraftSpawner = new Spawner(aircraftContainer);
		final Runway[] runways = new Runway[] {
			new Runway(aircraftContainer),
			new Runway(aircraftContainer),
			new Runway(aircraftContainer)
		};

		final Thread spawnerThread = new Thread(aircraftSpawner);
		final ExecutorService runwayExecutor = Executors.newFixedThreadPool(runways.length);


		for (Runway runway : runways) {
			runwayExecutor.execute(runway);
		}
		
		// runwayExecutor.execute(aircraftSpawner);
		spawnerThread.start();

		// Display Thread
		boolean ended = false;

		do {
			Util.clearScreen();
			Util.printCurrentTime(startTime, duration);
			Util.printAircraftStatuses(aircraftContainer.getArrayList());
			Util.printRunwayStatuses(runways);
			Util.printLogs(logsCount);

			if (!(System.currentTimeMillis() <= endTime)) {
				aircraftSpawner.stop();
				System.out.println(String.format("Aircraft Spawner has stopped spawning aircrafts... (at %s)\n", aircraftSpawner.getEndTime()));
			}

			ended = !(System.currentTimeMillis() <= endTime) && (aircraftContainer.isEmpty());

			Thread.sleep(1000);

		} while (!ended);

		runwayExecutor.shutdown();
		spawnerThread.join();

		while (!runwayExecutor.isTerminated()) {
			if (ended) {
				Util.writeToLogsSorted();
				Util.printRunwayStats(runways, endTime);
				break;
			}
		}

		System.exit(0);
	}
}