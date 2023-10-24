//Record.java

public abstract class Record {
	public String record;

	public abstract void appendToRecord(String text);

	public void clearRecord(){
		this.record = "";
	}
}
