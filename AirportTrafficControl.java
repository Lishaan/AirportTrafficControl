import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

// AirportTrafficControl is the program class that contains all the logic for starting up the program’s execution.
public class AirportTrafficControl {
	private final Container<Aircraft> aircraftContainer;
	private final Runway[] runways;
	private final ExecutorService runwayExecutor;
	private final int aircraftsToSpawn;
	private final int logsCount;

	// DEFAULT PROGRAM SETUP
	private AirportTrafficControl() {
		// aircraftsToSpawn = 10
		// logsCount = 5
		// airportSize = 5
		// runwayCount = 3
		this(3, 5, 5, 10);
	}

	// CUSTOM PROGRAM SETUP
	private AirportTrafficControl(int runwayCount, int containerSize, int logsCount, int aircraftsToSpawn) {
		aircraftContainer = new Container<Aircraft>(containerSize);

		runways = new Runway[runwayCount];

		for (int i = 0; i < runways.length; i++) {
			runways[i] = new Runway(aircraftContainer);
		}

		runwayExecutor = Executors.newFixedThreadPool(runways.length);

		this.aircraftsToSpawn = aircraftsToSpawn;
		this.logsCount = logsCount;
	}

	// Starts the program
	public int start() throws InterruptedException {
		Util.initLogFile();
		int aircraftsRemaining = aircraftsToSpawn;

		// Submit the runways into the runway executor
		for (Runway runway : runways) {
			runwayExecutor.execute(runway);
		}

		final long startTime = System.currentTimeMillis();
		int spawnTime = -1;
		boolean spawning = false;
		boolean ended = false;
		
		// Main Thread: Displaying & Spawning aircrafts
		do {
			// Determines whether to spawn aircrafts or not
			boolean spawn = (!aircraftContainer.isFull() && aircraftsRemaining > 0);

			// Display
			Util.clearScreen();
			Util.printCurrentTime(startTime, aircraftsRemaining, spawnTime);
			Util.printAircraftStatuses(aircraftContainer.getArrayList());
			Util.printRunwayStatuses(runways);
			Util.printLogs(logsCount);
			System.out.format("%s", (aircraftsRemaining <= 0) ? "Waiting for aircrafts to clear\n" : "");

			// Spawn Aircraft
			if (spawn) {
				if (!spawning) {
					spawnTime = Util.getRandomInt(4, 6);
					Util.addLog(String.format("Creating an Aircraft in %d seconds", spawnTime), 0);
					spawning = true;
				}

				if (spawnTime <= 0) {
					aircraftContainer.add(Airport.newAircraft());
					aircraftsRemaining -= 1;
					spawning = false;
				}
			}
			
			// Ending Condition
			ended = (aircraftContainer.isEmpty()) && (aircraftsRemaining <= 0);

			Thread.sleep(1000);

			if (spawn) {
				spawnTime -= 1;
			}

		} while (!ended);

		runwayExecutor.shutdown();

		while (!runwayExecutor.isTerminated()) {
			if (ended) {
				// Stop all the runways
				for (Runway runway : runways) {
					runway.stop();
				}
				
				// Write the logs to the log.txt file
				Util.writeToLogsSorted();
				Util.printRunwayStats(runways, startTime, aircraftsToSpawn);
				break;
			}
		}

		return 0;
	}

	// Creates a new instance of AirportTrafficControl that starts a menu and asks the user to choose the simulation setup type
	public static AirportTrafficControl create() {
		Util.clearScreen();
		System.out.println("Airport Traffic Control v1.0\n");
		System.out.println("1. Default Setup");
		System.out.println(" - Aircrafts to spawn: 10");
		System.out.println(" - Logs to display: 5");
		System.out.println(" - Airport capacity: 5");
		System.out.println(" - Runways: 3\n");
		System.out.println("2. Custom Setup\n");
		
		Scanner userInput = new Scanner(System.in);
		int choice;
		do {
			System.out.print("Choice: ");
			choice = userInput.nextInt();
		} while (choice > 2 || choice < 1);

		userInput.close();

		if (choice == 2) {
			Util.clearScreen();
			System.out.println("Custom Setup\n");
			final int aircraftsToSpawn = Util.getValidUserInputInt("Aircrafts to spawn (recommended MAX=99): ");
			final int logsCount = Util.getValidUserInputInt("Logs to display (recommended MAX=10): ");
			final int airportSize = Util.getValidUserInputInt("Airport capacity: ");
			final int runwayCount = Util.getValidUserInputInt("Runways: ");
			
			Airport.setCapacity(airportSize);
			return new AirportTrafficControl(runwayCount, airportSize, logsCount, aircraftsToSpawn);
		} else {
			return new AirportTrafficControl();
		}
	}
}