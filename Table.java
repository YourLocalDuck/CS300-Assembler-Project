//Table.java
import java.util.*;

//Table class that handles all table functions we need. All values are Strings.
public class Table {
	
	public Hashtable<String, String[]> table;

	public Table(){
		this.table = new Hashtable<String, String[]>();
	}

	//function to add entry to the table. returns a boolean if it finds
	//the inputted key in the table. use this to deal with duplicates.	
	public boolean addEntry(String key, String[] values) {
		if (table.containsKey(key)) {
			return false;
		}
		else {
			table.put(key, values);
			return true;
		}
	}

	//returns a string array containing all of the information
	//for a given key. string array can be any size. convert to
	//desired data types when needed.
	public String[] getEntry(String key){
		String[] values = table.get(key);
		return values;
	}

	//debug function to print the table
	public void printTable() {
		table.forEach((k, v) -> {
			System.out.print(k + ": ");
			for (int i = 0; i < v.length; i++) {
				System.out.println(v[i] + ", ");
			}
		});
		System.out.println();
	}
}
