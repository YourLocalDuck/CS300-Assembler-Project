import java.util.*;
import java.io.*;

public class Main {
    
    public static void main(String[] args) {
	//pass 1
	Table SYMTAB = new Table();
	int LOCCTR = 0;
	OPTABHashTable OPTAB = new OPTABHashTable();

        String fileName = "basic";
        FileInput fileParser = new FileInput(fileName+".txt");
        List<Instruction> basicInstructions = fileParser.getParsedInstructions(1);

        for (int i = 0; i < basicInstructions.size(); i++) {
		Instruction instruction = basicInstructions.get(i);
		String opcode = instruction.getMnemonic();
		if (opcode.charAt(0) == '+') {
			opcode = opcode.substring(1);
		}

		String[] opVal = OPTAB.findOperation(instruction.getMnemonic());
		String hexLoc = convertToHex(LOCCTR, 4);
		basicInstructions.get(i).setLoc(hexLoc);
		String label = instruction.getLabel();

		if (!(label.equals(""))) {
			String[] labelValues = {hexLoc};
			boolean canAdd = SYMTAB.addEntry(label, labelValues);
			if (!canAdd) {
				System.out.println("ERROR: Duplicate symbol '" + label + "'");
				System.exit(0);
			}
		}

		if (opVal != null) {
			LOCCTR += 3;
		}
		else {
			opcode = instruction.getMnemonic();
			if (opcode.equals("WORD")) {
				LOCCTR += 3;
			}
			else if (opcode.equals("RESB")) {
				LOCCTR += Integer.parseInt(instruction.getOperands());
			}
			else if (opcode.equals("RESW")) {
				int operand = Integer.parseInt(instruction.getOperands());
				LOCCTR += 3 * operand;
			}
			else if (opcode.equals("BYTE")) {
				String operand = instruction.getOperands();
				LOCCTR += operand.length();
			}
			else {
				System.out.println(opcode);
				System.out.println("ERROR: invalid operation");
				System.exit(0);
			}
		}
	}

	//read in register data
	try {
		BufferedReader reader = new BufferedReader(new FileReader("registers.txt"));
		String line;
		while ((line = reader.readLine()) != null){
			String regString[] = line.split(",");
			String regData[] = {regString[1]}; 
			SYMTAB.addEntry(regString[0], regData);
		}
	} catch (IOException e) {
		System.out.println("error reading registers.txt");
	}

	//SYMTAB.printTable();

	FileOutput writer = new FileOutput("test.txt");
	writer.writeIntermediateFile(basicInstructions, 1);

	//pass 2
	basicInstructions = fileParser.getParsedInstructions(2);
	int base = 0;
	int pc = 0;
	for (int i = 0; i < basicInstructions.size(); i++) {
		Instruction instruction = basicInstructions.get(i);
		if (i + 1 < basicInstructions.size()){
			pc = Integer.parseInt(basicInstructions.get(i+1).getLoc(), 16);
		}
		else pc = LOCCTR;
		String opcode = instruction.getMnemonic();

		if (opcode.equals("START")) {
			System.out.println("START");
		}
		else if (opcode.equals("END")) {
			System.out.println("END");
		}

		else if (opcode.equals("BYTE") || opcode.equals("WORD")) {
			System.out.println("BW");
		}
		
		//check for format 4, remove '+' if exists
		int format = 3;
		if (opcode.charAt(0) == '+') {
			opcode = opcode.substring(1);
			format = 4;
		}
		String[] opVal = OPTAB.findOperation(opcode);

		//generate instruction for given opcode
		if (opVal != null) {
			//get format, check for multiple operands
			opcode = opVal[0];
			if (format != 4) {
				format = Integer.parseInt(opVal[1]);
			}
			int[] operandAddr = {0, 0};
			String operandStr = instruction.getOperands();
			String[] operands = {"", ""};
			if (operandStr.contains(",")) {
				operands = operandStr.split(",");
			}
			else {
				operands[0] = operandStr;
			}

			//find addressing mode
			String mode = "";
			if (!(operands[0].equals(""))) {
				mode = Character.toString(operands[0].charAt(0));
			}
			if ((!(mode.equals("#") || mode.equals("@")))) {
				mode = "";
			}
			else {
				operands[0] = operands[0].substring(1);
			}

			//get operand addresses
			boolean isConstant = false;
			for (int j = 0; j < operands.length; j++){
				if ((!(operands[j].equals("")))) {
					String[] values = SYMTAB.getEntry(operands[j]);
					if (values != null) {
						String operandAddrStr = values[0];
						operandAddr[j] = Integer.parseInt(operandAddrStr, 16);
					}
					else {
						try {
							String operandAddrStr = operands[j];
							operandAddr[j] = Integer.parseInt(operandAddrStr, 16);
							isConstant = true;
						} catch (NumberFormatException e) {
							System.out.println("ERROR: Undefined Symbol '" + operands[j] + "'");
							System.exit(0);
						}
					}
				}
			}
			
			//check for indexed addressing
			boolean isIndexed = false;
			if (operands[1].equals("X")) {
				isIndexed = true;
			}
			String objCode = toMachineCode(opcode, operandAddr[0], operandAddr[1], format, mode, isIndexed, isConstant, pc, base);	
			instruction.setObjCode(objCode);
			//append to T Record	
		}
	}
	
	writer.writeIntermediateFile(basicInstructions, 2);
        /*ObjectProgram output = new ObjectProgram();
        output.Head = "testHead";
        output.Text = "testText";
        output.End = "testEnd";
        FileOutput printer = new FileOutput(fileName+".o");
        printer.writeFile(output);*/
    }

	//Function that generates object code for a line of assembly.
	//Specify the OPCODE as a string, the operand address (IN HEX!!!!),
	//the format type (1-4), the addressing mode ("#" for immediate, "@" for indirect, anything else for simple),
	//whether or not index register is used (true for yes, false for no), the PC counter value (ALSO HEX), 
	//and the base register value (ALSO HEX). Will return a hex string with the object code.
	public static String toMachineCode(String OPCODE, int operandAddr1, int operandAddr2, int format, String mode, boolean isIndexed, boolean isConstant, int PC, int BASE) {
		//add first four bits of OPCODE to machineCode string
		String machineCode = "";
		String opBitOneChar = Character.toString(OPCODE.charAt(0));
		int opBitOneInt = Integer.parseInt(opBitOneChar, 16);
		String opBitOneBinary = convertToBinary(opBitOneInt, 4);
		machineCode += opBitOneBinary;
		//if format 1 instruction, just add next 4 bits, and done
	       	if (format == 1) {
			String opBitTwoChar = Character.toString(OPCODE.charAt(1));
			int opBitTwoInt = Integer.parseInt(opBitTwoChar, 16);
			String opBitTwoBinary = convertToBinary(opBitTwoInt, 4);
			machineCode += opBitTwoBinary;
			int machineCodeInt = Integer.parseInt(machineCode, 2);
			machineCode = convertToHex(machineCodeInt, 2);
			return machineCode;
		}	
		//if format 2, add next 4 bits in OPCODE, add operandAddr
		else if (format == 2) {
			String opBitTwoChar = Character.toString(OPCODE.charAt(1));
			int opBitTwoInt = Integer.parseInt(opBitTwoChar, 16);
			String opBitTwoBinary = convertToBinary(opBitTwoInt, 4);
			machineCode += opBitTwoBinary;

			String operandAddrBinary = convertToBinary(operandAddr1, 4);
			machineCode += operandAddrBinary;

			operandAddrBinary = convertToBinary(operandAddr2, 4);
			machineCode += operandAddrBinary;

			int machineCodeInt = Integer.parseInt(machineCode, 2);
			machineCode = convertToHex(machineCodeInt, 4);

			return machineCode;
		}
		else {
			//if format 3 or 4, only want first two bits of second half of OPCODE
			String opBitTwoChar = Character.toString(OPCODE.charAt(1));
			int opBitTwoInt = Integer.parseInt(opBitTwoChar, 16);
			String opBitTwoBinary = convertToBinary(opBitTwoInt, 4);
			opBitTwoBinary = opBitTwoBinary.substring(0, 2);
			machineCode += opBitTwoBinary;
			//check addressing mode, add appropriate n and i bits
			if (mode.equals("#")) {
				machineCode += "01";
			}
			else if (mode.equals("@")) {
				machineCode += "10";
			}
			else machineCode += "11";
			//if format 4, set e bit to 1, add operandAddr
			if (format == 4) {
				//if index is included in operand field, set x bit to 1
				if (isIndexed) {
					machineCode += "1001";
				}
				else machineCode += "0001";
				String operandAddrBinary = convertToBinary(operandAddr1, 20);
				machineCode += operandAddrBinary;
	
				int machineCodeInt = Integer.parseInt(machineCode, 2);
				machineCode = convertToHex(machineCodeInt, 8);
				return machineCode;
			}
			//if 1st operand is a constant, just use disp
			else if (isConstant) {
				if (isIndexed) {
					machineCode += "1000";
				}
				else {
					machineCode += "0000";
				}
				String operandAddrBinary = convertToBinary(operandAddr1, 12);
				machineCode += operandAddrBinary;

				int machineCodeInt = Integer.parseInt(machineCode, 2);
				machineCode = convertToHex(machineCodeInt, 6);
				return machineCode;
			}	
			//if format 3 instruction, use relative addressing
			else {
				//PC relative
				int disp = operandAddr1 - PC;
				//if disp is greater than 12 bits, use base relative
				if (disp >= 2048) {
					disp = operandAddr1 - BASE;
					//if disp still greater than 12 bits, throw error, quit assembly
					if (disp >= 2048) {
						System.out.println("Error: Target Address outside 12 bit limit.");
						System.exit(0);
						return machineCode;
					}
					else {
						//if using index reg, set x and b bits to 1, else just b to 1
						if (isIndexed) {
							machineCode += "1100";
							disp += operandAddr2;
						}
						else machineCode += "0100";


						if (disp < 0){
							disp = twosComp(disp);
							int machineCodeInt = Integer.valueOf(machineCode, 2);
							machineCode = convertToHex(machineCodeInt, 3);
							String hexDisp = convertToHex(disp, 3);
							machineCode += hexDisp;
							return machineCode;
						}
						else {
							String dispBinary = convertToBinary(disp, 12);
							machineCode += dispBinary;
							int machineCodeInt = Integer.valueOf(machineCode, 2);
							machineCode = convertToHex(machineCodeInt, 6);
							return machineCode;
						}
					}
				}
				else {
					//if using index reg, set x and p bits to 1, else just p to 1
					if (isIndexed){
						machineCode += "1010";
						disp += operandAddr2;
					}
					else machineCode += "0010";
					//if negative number, do twos comp
					if (disp < 0){
						disp = twosComp(disp);
						int machineCodeInt = Integer.valueOf(machineCode, 2);
						machineCode = convertToHex(machineCodeInt, 3);
						String hexDisp = convertToHex(disp, 3);
						System.out.println(hexDisp);
						machineCode += hexDisp;
						return machineCode;
					}
					else {
						String dispBinary = convertToBinary(disp, 12);
						machineCode += dispBinary;
						int machineCodeInt = Integer.valueOf(machineCode, 2);
						machineCode = convertToHex(machineCodeInt, 6);
						return machineCode;
					}
				}
			}
		}
	}//end toMachineCode

	//Function to convert a decimal integer to a binary string. Specify a length for how many digits 
	//to include.
	public static String convertToBinary(int number, int length){
		int quotient = number;
		int remainder = 0;
		String reverseBinaryNumber = "";
		//calculates binary digits from right to left. adds them from left to right
		while (quotient != 0) {
			remainder = quotient % 2;
			quotient = quotient / 2;
			reverseBinaryNumber += Integer.toString(remainder);
		}
		String binaryNumber = "";
		//for loop to get binary digits in correct order
		for (int i = reverseBinaryNumber.length() - 1; i >= 0; i--){
			binaryNumber += reverseBinaryNumber.charAt(i);
		}
		//add padding zeroes to left side of number based on specified length
		if (binaryNumber.length() < length) {
			String padding = "";
			int i = binaryNumber.length();
			while (i < length){
				padding += "0";
				i++;
			}
			padding += binaryNumber;
			return padding;
		}
		else return binaryNumber;
	}

	public static int twosComp(int number) {
		String binaryString = Integer.toBinaryString(number);
		binaryString = binaryString.substring(19);
		int twosCompInt = Integer.valueOf(binaryString, 2);
		return twosCompInt;
	}//end twosComp

	static String convertToHex(int convertNum, int length) {
		int quotient = convertNum;
	    	int remainder = convertNum;
	    	String reverseHexNumber = "";
		while (quotient != 0) {
			remainder = quotient % 16;
			quotient = quotient / 16;
			if (remainder >= 10 && remainder <= 15){
				switch (remainder) {
					case 10:
					       reverseHexNumber += "A";
					       break;
					case 11:
					       reverseHexNumber += "B";
					       break;
					case 12:
					       reverseHexNumber += "C";
					       break;
					case 13:
					       reverseHexNumber += "D";
					       break;
					case 14:
					       reverseHexNumber += "E";
					       break;
					case 15:
					       reverseHexNumber += "F";
					       break;
				}
			}
			else reverseHexNumber += Integer.toString(remainder);
		}
		String hexNumber = "";
		for (int i = reverseHexNumber.length() - 1; i >= 0; i--){
			hexNumber += reverseHexNumber.charAt(i);
		}
		if (hexNumber.length() < length) {
			String padding = "";
			int i = hexNumber.length();
			while (i < length){
				padding += "0";
				i++;
			}
			padding += hexNumber;
			return padding;
		}
		else if (hexNumber.length() > length) {
			hexNumber = hexNumber.substring(hexNumber.length() - length);
			return hexNumber;
		}
		else return hexNumber;

    }

    static void printInstruction(Instruction inst) {
        System.out.println(inst.comment);
        System.out.println(inst.mnemonic);
        System.out.println(inst.operands);
        System.out.println(inst.comment);
    }
}
