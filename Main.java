import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		Util.clearScreen();
		System.out.println("Welcome to Airport Traffic Control v1.0");
		System.out.print("Enter the number of aircrafts to spawn: ");
		final int aircraftsToSpawn = (new java.util.Scanner(System.in).nextInt());
		int aircraftsRemaining = aircraftsToSpawn;

		Util.initLogFile();
		System.out.print("Enter the number of logs to display: ");
		final int logsCount = (new java.util.Scanner(System.in).nextInt());

		final Container<Aircraft> aircraftContainer = new Container<Aircraft>(Airport.CAPACITY);
		final Runway[] runways = new Runway[] {
			new Runway(aircraftContainer),
			new Runway(aircraftContainer),
			new Runway(aircraftContainer)
		};

		final ExecutorService runwayExecutor = Executors.newFixedThreadPool(runways.length);

		for (Runway runway : runways) {
			runwayExecutor.execute(runway);
		}

		final long startTime = System.currentTimeMillis();
		int spawnTime = 0;
		boolean spawning = false;
		boolean ended = false;
		
		// Main Thread: Displaying & Spawning aircrafts
		do {
			// Display
			Util.clearScreen();
			Util.printCurrentTime(startTime, aircraftsRemaining);
			Util.printAircraftStatuses(aircraftContainer.getArrayList());
			Util.printRunwayStatuses(runways);
			Util.printLogs(logsCount);

			// Spawn Aircraft
			if (!aircraftContainer.isFull() && aircraftsRemaining > 0) {
				if (!spawning) {
					spawnTime = Util.getRandomInt(4, 8);
					Util.addLog(String.format("Creating an Aircraft in %d seconds", spawnTime), 0);
					spawning = true;
				}

				if (spawnTime == 0) {
					aircraftContainer.add(Airport.newAircraft());
					aircraftsRemaining -= 1;
					spawning = false;
				}
			}

			if (aircraftsRemaining <= 0) {
				System.out.println("Waiting for aircrafts to clear\n");
			}
			
			// Ending Condition
			ended = (aircraftContainer.isEmpty()) && (aircraftsRemaining <= 0);

			Thread.sleep(1000);

			if (!aircraftContainer.isFull()) {
				spawnTime -= 1;
			}

		} while (!ended);

		runwayExecutor.shutdown();

		while (!runwayExecutor.isTerminated()) {
			if (ended) {
				Util.writeToLogsSorted();
				Util.printRunwayStats(runways, startTime, aircraftsToSpawn);
				break;
			}
		}

		System.exit(0);
	}
}