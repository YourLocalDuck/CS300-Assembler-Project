//Code to assembler object code instructions


public class CodeAssembly {


	public static void main(String[] args){
		CodeAssembly ass = new CodeAssembly();
		String hexCode = "0036";
		int operandAddr = Integer.parseInt(hexCode, 16);
		System.out.println(ass.toMachineCodeConst("X'05'"));
		System.out.println(ass.toMachineCodeConst("C'EOF'"));
		//System.out.println(ass.toMachineCode("54", operandAddr, 3, "", true, 0x1051, 0x0033));
		//System.out.println(ass.toMachineCode("54", 0x0036, 3, "", true, 0x1051, 0x0033));
	}

	//Function that generates object code for a line of assembly.
	//Specify the OPCODE as a string, the operand address (IN HEX!!!!),
	//the format type (1-4), the addressing mode ("#" for immediate, "@" for indirect, anything else for simple),
	//whether or not index register is used (true for yes, false for no), the PC counter value (ALSO HEX), 
	//and the base register value (ALSO HEX). Will return a hex string with the object code.
	public String toMachineCode(String OPCODE, int operandAddr, int format, String mode, boolean isIndexed, int PC, int BASE) {
		//add first four bits of OPCODE to machineCode string
		String machineCode = "";
		char opBitOneChar = OPCODE.charAt(0);
		int opBitOneInt = Integer.parseInt(String.valueOf(opBitOneChar));
		String opBitOneBinary = convertToBinary(opBitOneInt, 4);
		machineCode += opBitOneBinary;
		//if format 1 instruction, just add next 4 bits, and done
	       	if (format == 1) {
			char opBitTwoChar = OPCODE.charAt(1);
			int opBitTwoInt = Integer.parseInt(String.valueOf(opBitTwoChar));
			String opBitTwoBinary = convertToBinary(opBitTwoInt, 4);
			machineCode += opBitTwoBinary;
			int machineCodeInt = Integer.parseInt(machineCode);
			machineCode = Integer.toHexString(machineCodeInt);
			return machineCode;
		}	
		//if format 2, add next 4 bits in OPCODE, add operandAddr
		else if (format == 2) {
			char opBitTwoChar = OPCODE.charAt(1);
			int opBitTwoInt = Integer.parseInt(String.valueOf(opBitTwoChar));
			String opBitTwoBinary = convertToBinary(opBitTwoInt, 4);
			machineCode += opBitTwoBinary;

			String operandAddrBinary = convertToBinary(operandAddr, 8);
			machineCode += operandAddrBinary;

			int machineCodeInt = Integer.parseInt(machineCode);
			machineCode = Integer.toHexString(machineCodeInt);
			return machineCode;
		}
		else {
			//if format 3 or 4, only want first two bits of second half of OPCODE
			char opBitTwoChar = OPCODE.charAt(1);
			int opBitTwoInt = Integer.parseInt(String.valueOf(opBitTwoChar));
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
				String operandAddrBinary = convertToBinary(operandAddr, 20);
				machineCode += operandAddrBinary;
	
				int machineCodeInt = Integer.parseInt(machineCode);
				machineCode = Integer.toHexString(machineCodeInt);
				return machineCode;
			}	
			//if format 3 instruction, use relative addressing
			else {
				//PC relative
				int disp = operandAddr - PC;
				//if negative number, do twos comp
				if (disp < 0){
					disp = twosComp(disp);
				}
				//if disp is greater than 12 bits, use base relative
				if (disp >= 2048) {
					disp = operandAddr - BASE;
					if (disp < 0){
						disp = twosComp(disp);
					}
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
						}
						else machineCode += "0100";
						String dispBinary = convertToBinary(disp, 12);
						machineCode += dispBinary;
	
						int machineCodeInt = Integer.valueOf(machineCode, 2);
						machineCode = Integer.toHexString(machineCodeInt);
						return machineCode;
					}
				}
				else {
					//if using index reg, set x and p bits to 1, else just p to 1
					if (isIndexed){
						machineCode += "1010";
					}
					else machineCode += "0010";
					String dispBinary = convertToBinary(disp, 12);
					machineCode += dispBinary;
	
					int machineCodeInt = Integer.valueOf(machineCode, 2);
					machineCode = Integer.toHexString(machineCodeInt);
					return machineCode;
				}
			}
		}
	}//end toMachineCode

	public String toMachineCodeConst(String operand) {
		char constType = operand.charAt(0);
		if (constType == 'C') {
			String operandVal = operand.substring(2, operand.length() - 1);
			String objCode = "";
			for (int i = 0; i < operandVal.length(); i++) {
				int charVal = (int)operandVal.charAt(i);
				objCode += convertToHex(charVal, 2);
			}
			return objCode;		
		}
		else if (constType == 'X') {
			return operand.substring(2, operand.length() - 1);
		}
		else {
			System.out.println("ERROR: Invalid constant def '" + operand + "'");
			System.exit(0);
			return "";
		}
	}


	//Function to convert a decimal integer to a binary string. Specify a length for how many digits 
	//to include.
	public String convertToBinary(int number, int length){
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

	public int twosComp(int number) {
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

}
