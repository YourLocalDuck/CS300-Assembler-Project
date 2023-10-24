//Text Record data class

public class TextRecord extends Record {
	public String record;
	
	public void appendToRecord(String text){
		if (record.length() + text.length() > 69){
			//call function to write out text record here
			clearRecord();
			record += text;
		}
		else {
			record += text;
		}
	}

	public void clearRecord(){
		super.clearRecord();
		record += "T";
	}

}
