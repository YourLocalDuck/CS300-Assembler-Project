import java.util.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner; //Import the scanner class to read input


class OPTABHashTable
{

	//Define Hashtable
	Hashtable<String, Integer> OPTAB = new Hashtable<>();
	
	//Define Scanner
	Scanner myScanner = new Scanner(System.in);
	
	//Add a Mnenomic Operation
	void addOperation()
	{
		//Ask the user for input
		System.out.println("Enter the Mnemonic Operation you wish to add");//Get Replaced?
		String mnemonicOperation = myScanner.nextLine();
		System.out.println("Enter the machine code related to the mnemonic operation");//Get Replaced?
		int mnemonicMachineCode = Integer.parseInt(myScanner.nextLine());
		
		//Insert the value into the hash table
		OPTAB.put(mnemonicOperation, mnemonicMachineCode);
	}
	
	//Find a Mnenomic Operation 
	void findOperation()
	{
		//Get User Input
		System.out.println("Enter the Mnemonic operation you wish to find");//Get Replaced?
		String mnemonicOperation = myScanner.nextLine();
		//Find the input
	
		int machineCode = -1;
		machineCode = OPTAB.get(mnemonicOperation);
		
		if(machineCode != -1)
		{
			System.out.println("Code was found");
			System.out.println("\n" + "Mnemonic Opeartion: " + mnemonicOperation + "\n" +  "Machine Code: " + machineCode);
		}
		else
		{
			System.out.println("No code was found");
		}
	}
	
	void viewData()
	{
		System.out.println(OPTAB.toString());
	}	
}



public class OPTAB {
	public static void main(String[] args)
	{

		OPTABHashTable OPTAB = new OPTABHashTable();
		//Define Scanner
		Scanner myScanner = new Scanner(System.in);
		
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
}
