//Table.java
import java.util.*;

public class Table {
	
	public Hashtable<String, String[]> table;

	public Table(){
		this.table = new Hashtable<String, String[]>();
	}
	
	public boolean addEntry(String key, String[] values) {
		if (table.containsKey(key)) {
			return false;
		}
		else {
			table.put(key, values);
			return true;
		}
	}

	public String[] getEntry(String key){
		String[] values = table.get(key);
		return values;
	}

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
