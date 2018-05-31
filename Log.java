public class Log {
	private final int aircraftID;
	private final String message;
	private final Long timeCreated;

	public Log(String msg, int ID) {
		aircraftID = ID;
		message = msg;
		timeCreated = System.currentTimeMillis();
	}

	public @Override String toString() {
		String timePattern = "MM/dd HH:mm:ss";
		
		java.time.Instant instant = java.time.Instant.ofEpochMilli(timeCreated);
		java.time.ZonedDateTime zdt = java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
		java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern(timePattern);
		
		String output = fmt.format(zdt);

		return String.format("%s - %s", output, this.message);
	}

	public int getID() { return aircraftID; }
}