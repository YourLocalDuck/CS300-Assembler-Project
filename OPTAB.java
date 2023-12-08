import java.util.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.*;
import java.util.Scanner; //Import the scanner class to read input

/*public class OPTAB {
	public static void main(String[] args)
	{

		OPTABHashTable OPTAB = new OPTABHashTable();
		//Define Scanner
		Scanner myScanner = new Scanner(System.in);
		
		OPTAB.viewData();

		while(true)
		{
			//Display Menu
			System.out.println("Enter '0' to add an mnemonic opeation");
			System.out.println("Enter '1' to find an mnemonic operation");
			System.out.println("Enter '2' to view all mnemonic operations");
		
			//Get User Input
			int option = myScanner.nextInt();
		
			if(option == 0)
			{
				OPTAB.addOperation();
			}
			else if (option == 1)
			{
				OPTAB.findOperation();		
			}
			else if (option == 2)
			{
				OPTAB.viewData();
			}
			else
			{
				System.out.println("Please enter a valid input");
			}
		}//end while
			
	}
}*/

class OPTABHashTable
{

	//Define Hashtable
	Hashtable<String, String[]> OPTAB;
	
	//Define Scanner
	Scanner myScanner = new Scanner(System.in); // What is the purpose of this scanner? It is never used.
	
	public OPTABHashTable(){
		this.OPTAB = new Hashtable<String, String[]>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("opcodes.txt"));
			String line;
			while ((line = reader.readLine()) != null){
				String opString[] = line.split(",");
			       	String data[] = {opString[1], opString[2]};	
				addOperation(opString[0], data);
			}
		} catch (IOException e) {
			System.out.println("error reading opcodes.txt");
		}
	}

	//Add a Mnenomic Operation
	void addOperation(String mnemonicOperation, String[] data)
	{
		/*
		//Ask the user for input
		System.out.println("Enter the Mnemonic Operation you wish to add");//Get Replaced?
		String mnemonicOperation = myScanner.nextLine();
		System.out.println("Enter the machine code related to the mnemonic operation");//Get Replaced?
		int mnemonicMachineCode = Integer.parseInt(myScanner.nextLine());
		*/
		//Insert the value into the hash table
		OPTAB.put(mnemonicOperation, data);
	}
	
	//Find a Mnenomic Operation 
	String[] findOperation(String mnemonicOperation)
	{
		//Get User Input
		//System.out.println("Enter the Mnemonic operation you wish to find");//Get Replaced?
		//String mnemonicOperation = myScanner.nextLine();
		//Find the input
	
		String[] data = OPTAB.get(mnemonicOperation);
		return data;
		
	}
	
	void viewData()
	{
		OPTAB.forEach((k, v) -> {
			System.out.print(k + ": ");
			for (int i = 0; i < v.length; i++) {
				System.out.println(v[i] + ", ");
			}
		});
		System.out.println();
	}	
}

