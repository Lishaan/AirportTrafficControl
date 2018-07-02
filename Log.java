// Log is a simple class that represents a log statement. It is used to print logs into the console screen, and to the log file. 
public class Log {
	private final int aircraftID;
	private final long timeCreated;
	private final String message;

	public Log(String msg, int ID) {
		aircraftID = ID;
		message = msg;
		timeCreated = System.currentTimeMillis();
	}

	public @Override String toString() {
		return String.format("%s - %s", 
			Util.formatTime(timeCreated), this.message);
	}

	public int getID() { return aircraftID; }
}

