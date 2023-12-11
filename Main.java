import java.util.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    
    public static void main(String[] args) {
	//pass 1
	Table LITTAB = new Table();
	Table BLKTAB = new Table();
	Table SYMTAB = new Table();
	OPTABHashTable OPTAB = new OPTABHashTable();
	int LOCCTR = 0;
	String blockName = "";
	int blockNum = 0;
	String baseVal = "";
        String fileName = "prog_blocks";
        FileInput fileParser = new FileInput(fileName+".txt");
        List<Instruction> basicInstructions = fileParser.getParsedInstructions(1);


	String programName = "";
	Instruction startInstruction = basicInstructions.get(0);
	int startIndex = 0;
	if (startInstruction.getMnemonic().equals("START")) {
		programName = startInstruction.getLabel();
		LOCCTR = Integer.parseInt(startInstruction.getOperands());
		String hexLoc = convertToHex(LOCCTR, 4);
		startInstruction.setLoc(hexLoc);
		startIndex = 1;
		String[] blktabValues = {"0", hexLoc, "0000", hexLoc};
		BLKTAB.addEntry("default", blktabValues);
		blockName = "default";
	}
	else {
		String[] blktabValues = {"0", "0000", "0000", "0000"};
		BLKTAB.addEntry("default", blktabValues);
		blockName = "default";
	}
        for (int i = startIndex; i < basicInstructions.size(); i++) {
		Instruction instruction = basicInstructions.get(i);
		String opcode = instruction.getMnemonic();
		String type = "";

		int locAmount = 3;
		if (opcode.charAt(0) == '+') {
			opcode = opcode.substring(1);
			locAmount = 4;
			type = "R";
		}

		String temp_holder = instruction.getOperands();
		boolean isLiteral = temp_holder.startsWith("=");
		if (isLiteral) {
			String var_name = "";
			String[] var_value = {"", ""};
			String[] parts = temp_holder.split("=");

			var_name = parts[1];

			char varType = 'X';
			for (String element : parts) {
				char[] charArray = element.toCharArray();
				for(int start = 0; start < charArray.length; start++) {	
    	            			if(start == 0) {
    	            				varType = charArray[start];
    	            				break;
    	            			}
    	            		}
			
				String x = element;
				Pattern p = Pattern.compile("'([^' ]+)'");
				Matcher m = p.matcher(x);
				while (m.find()) {
					var_value[0] = m.group(1);
				}
			}
			int litLength = 0;
			if (varType == 'C') {
				litLength = var_value[0].length();
			}
			else litLength = 1;
			var_value[1] = convertToHex(litLength, 4);
			LITTAB.addEntry(var_name, var_value);
		}

		String[] opVal = OPTAB.findOperation(opcode);
		String hexLoc = convertToHex(LOCCTR, 4);
		basicInstructions.get(i).setLoc(hexLoc);
		String label = instruction.getLabel();

		if (!(label.equals(""))) {
			if (type.equals("")) {
				type = "R";
			}
			String[] labelValues = {hexLoc, blockName, type};
			boolean canAdd = SYMTAB.addEntry(label, labelValues);
			if (!canAdd) {
				System.out.println("ERROR: Duplicate symbol '" + label + "'");
				System.exit(0);
			}
		}

		if (opVal != null) {
			String format = opVal[1];
			if (format.equals("2")) {
				locAmount = 2;
			}
			LOCCTR += locAmount;
		}
		else {
			opcode = instruction.getMnemonic();
			if (opcode.equals("WORD")) {
				LOCCTR += locAmount;
			}
			else if (opcode.equals("RESB")) {
				LOCCTR += Integer.parseInt(instruction.getOperands());
			}
			else if (opcode.equals("RESW")) {
				int operand = Integer.parseInt(instruction.getOperands());
				LOCCTR += locAmount * operand;
			}
			else if (opcode.equals("BYTE")) {
				String operand = instruction.getOperands();	
				char constType = operand.charAt(0);
				if (constType == 'C') {
					String constant = operand.substring(2, operand.length() - 1);
					LOCCTR += constant.length();
				}
				else LOCCTR += 1;
			}
			else if (opcode.equals("EQU")) {
				String[] labelVals = SYMTAB.getEntry(label);
				String operands = instruction.getOperands();
				String labelLoc = "";
				if (operands.equals("*")) {
					labelLoc = convertToHex(LOCCTR, 4);
					labelVals[2] = "R";
				}
				else {
					try { 
						int value = Integer.parseInt(operands);
						labelLoc = convertToHex(value, 4);
						labelVals[2] = "A";

					} catch (NumberFormatException e) {
						String operation = "";
						if (operands.contains("-")) {
							operation = "-";
						}
						else if (operands.contains("+")) {
							operation = "+";
						}
						if (!(operation.equals(""))) {
							int[] values = {0, 0};
							int relativeCounter = 0;
							String[] terms = operands.split(operation);
							for (int k = 0; k < terms.length; k++) {
								String[] termData = SYMTAB.getEntry(terms[k]);
								values[k] = Integer.parseInt(termData[0], 16);
								if (termData[2].equals("R")) {
									relativeCounter++;
								}
							}
							if (relativeCounter != 2) {
								System.out.println("ERROR: Calculating expression with odd number of relative terms");
								System.exit(0);
							}
							else {
								int finalVal = 0;
								if (operation.equals("+")) {
									finalVal = values[0] + values[1];
								}
								else {
									finalVal = values[0] - values[1];
								}
								labelLoc = convertToHex(finalVal, 4);
								labelVals[2] = "A";
							}
						}
					}
				}
				labelVals[0] = labelLoc;
				instruction.setLoc(labelLoc);
				SYMTAB.editEntry(label, labelVals);
			}
			else if (opcode.equals("BASE")) {
				baseVal = instruction.getOperands();
			}
			else if (opcode.equals("END")) {
				i = basicInstructions.size();
				Enumeration<String> enumKey = LITTAB.table.keys();
				while(enumKey.hasMoreElements()) {
					String key = enumKey.nextElement();
					String[] litVals = LITTAB.getEntry(key);
	      			 	if (!(litVals.length > 2)) {
						int litLength = Integer.parseInt(litVals[1], 16);
						String[] newLitVals = {litVals[0], litVals[1], convertToHex(LOCCTR, 4)};
						LITTAB.editEntry(key, newLitVals);
						LOCCTR += litLength;		
					}
				}
			}
			else if (opcode.equals("LTORG")) {
				Enumeration<String> enumKey = LITTAB.table.keys();
				while(enumKey.hasMoreElements()) {
					String key = enumKey.nextElement();
					String[] litVals = LITTAB.getEntry(key);
	      			 	if (!(litVals.length > 2)) {
						int litLength = Integer.parseInt(litVals[1], 16);
						String[] newLitVals = {litVals[0], litVals[1], convertToHex(LOCCTR, 4), blockName};
						LITTAB.editEntry(key, newLitVals);
						LOCCTR += litLength;		
					}
				}
		
			}
			else if (opcode.equals("USE")){
				String operand = instruction.getOperands();
				if (operand.equals("")) {
					if (blockName.equals("default")) {
						System.out.println("ERROR: Called USE to exit block when in default block");
						System.exit(0);
					}
					else {
						String[] blkValues = BLKTAB.getEntry(blockName);
						blkValues[3] = convertToHex(LOCCTR, 4);
						BLKTAB.editEntry(blockName, blkValues);
						blkValues = BLKTAB.getEntry("default");
						LOCCTR = Integer.parseInt(blkValues[3], 16);
						instruction.setLoc(convertToHex(LOCCTR, 4));
						blockName = "default";
					}
				}
				else {
					String[] blkValues = BLKTAB.getEntry(blockName);
					blkValues[3] = convertToHex(LOCCTR, 4);
					BLKTAB.editEntry(blockName, blkValues);
					blkValues = BLKTAB.getEntry(operand);
					if (blkValues == null)	 {
						blockNum += 1;
						String[] newBlkValues = {Integer.toString(blockNum), convertToHex(LOCCTR, 4), "0000", "0000"};
						BLKTAB.addEntry(operand, newBlkValues);
						LOCCTR = 0;
					}
					else {
						LOCCTR = Integer.parseInt(blkValues[3], 16);
					}
					instruction.setLoc(convertToHex(LOCCTR, 4));
					blockName = operand;
				}
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

	
	String[] endBlkVal = BLKTAB.getEntry(blockName);
	endBlkVal[3] = convertToHex(LOCCTR, 4);
	BLKTAB.editEntry(blockName, endBlkVal);

	//LITTAB.printTable();
	//SYMTAB.printTable();


	//set lengths for each block, get total program length	
	int endLoc = 0;
	Enumeration<String> enumKey = BLKTAB.table.keys();
	while(enumKey.hasMoreElements()) {
		String key = enumKey.nextElement();
		String[] blkVal = BLKTAB.getEntry(key);
	       	blkVal[2] = blkVal[3];
		blkVal[1] = convertToHex(endLoc, 4);
		BLKTAB.table.replace(key, blkVal);	
		endLoc += Integer.parseInt(blkVal[2], 16);
	}
	LOCCTR = endLoc;

	//BLKTAB.printTable();

	FileOutput writer = new FileOutput(fileName +"Intr.txt");
	writer.writeIntermediateFilePass1(basicInstructions, LITTAB);

	//pass 2

	//for each label, add their relative addresses to their block addresses
	SYMTAB.table.forEach((k, v) -> {
		if (v.length > 1) {
			if (v[2].equals("R")) {
				String[] symVal = v;
				int integerLoc = Integer.parseInt(v[0], 16);
				String[] blkVal = BLKTAB.getEntry(v[1]);
				integerLoc += Integer.parseInt(blkVal[1], 16);
				symVal[0] = convertToHex(integerLoc, 4);
				SYMTAB.table.replace(k, symVal);
			}
		}
	});

	LITTAB.table.forEach((k, v) -> {
		if (v.length > 3) {
			String[] litVals = v;
			int integerLoc = Integer.parseInt(v[2], 16);
			String [] blkVal = BLKTAB.getEntry(v[3]);
			integerLoc += Integer.parseInt(blkVal[1], 16);
			litVals[2] = convertToHex(integerLoc, 4);
			LITTAB.table.replace(k, litVals);
		}
	});

	String hRec = "H";
	String eRec = "E";
	String tRec = "T";
	List<String> tRecords = new LinkedList<String>();
	String mRec = "M";
	List<String> mRecords = new LinkedList<String>();
	fileParser.setFileName(fileName + "Intr.txt");
	basicInstructions = fileParser.getParsedInstructions(2);

	

	int base = 0;
	if (!baseVal.equals("")) {
		String[] values = SYMTAB.getEntry(baseVal);
		if (values != null) {
			String operand = values[0];
			base = Integer.parseInt(operand, 16);
		}
		else {
			System.out.println(baseVal);
			System.out.println("ERROR: could not find symbol");	
			System.exit(0);
		}
	}
	int pc = 0;
	int tRecStartLen = 0;
	int tRecEndLen = 0;
	startInstruction = basicInstructions.get(0);
	String startOpCode = startInstruction.getMnemonic();
	if (startOpCode.equals("START")) {
		int startLoc = Integer.parseInt(startInstruction.getLoc(), 16);
		int progLen = LOCCTR - startLoc;
		hRec += programName;
		hRec += startInstruction.getLoc();
		String progLenHex = convertToHex(progLen, 6);
		hRec += progLenHex;
		tRec += "00" + startInstruction.getLoc();
		tRecStartLen = startLoc;
	}
	else {
		hRec += "000000";
		hRec += convertToHex(LOCCTR, 6);
		tRec += "000000";
		tRecStartLen = 0;
	}
	boolean foundFirstExecLine = false;
	boolean endReached = false;
	boolean isConstant = false;
	int blockStartForRecord = 0;
	int lastLineToGenerateObjCode = 0;
	String firstExecLine = "";
	for (int i = 0; i < basicInstructions.size(); i++) {
		Instruction instruction = basicInstructions.get(i);
		int format = 3;
		boolean pcFound = false;
		boolean outOfBounds = false;
		boolean useStatement = false;
		int j = 1;
		String nextLoc = "";
		if (instruction.getMnemonic().equals("USE")) {
			pcFound = true;
			useStatement = true;
			outOfBounds = true;
		}
		//calculate pc
		while (!pcFound) {
			if (i + j < basicInstructions.size()) {
				if (basicInstructions.get(i+j).getMnemonic().equals("USE")) {
					if (!(basicInstructions.get(i+j).getOperands().equals(""))) {
						String prevBlock = blockName;
						blockName = basicInstructions.get(i+j).getOperands();
						boolean nextUseFound = false;
						while (!nextUseFound) {
							if (basicInstructions.get(i+j).getMnemonic().equals("USE")) {
								if (basicInstructions.get(i+j).getOperands().equals("")) {
									nextLoc = basicInstructions.get(i+j).getLoc();
									pcFound = true;
									nextUseFound = true;
								}
								else {
									j++;
								}
							}
							else if (basicInstructions.get(i+j).getMnemonic().equals("END")) {
								pcFound = true;
								nextUseFound = true;
								String[] blkVals = BLKTAB.getEntry(prevBlock);
								nextLoc = blkVals[2];
								blockStartForRecord = Integer.parseInt(blkVals[1], 16);
							}
							else {
								j++;
							}
						}
					}
					else {
						boolean nextUseFound = false;
						while (!nextUseFound) {
							if (basicInstructions.get(i+j).getMnemonic().equals("USE")) {
								if (basicInstructions.get(i+j).getOperands().equals(blockName)) {
									nextLoc = basicInstructions.get(i+j).getLoc();
									String[] blkVals = BLKTAB.getEntry(blockName);
									blockStartForRecord = Integer.parseInt(blkVals[1], 16);
									pcFound = true;
									nextUseFound = true;
								}
								else {
									j++;
								}
							}
							else if (basicInstructions.get(i+j).getMnemonic().equals("END")) {
								pcFound = true;
								nextUseFound = true;
								String[] blkVals = BLKTAB.getEntry(blockName);
								nextLoc = blkVals[2];
							}
							else {
								j++;
							}
						}
						blockName = "default";
					}
				}
				else {
					nextLoc = basicInstructions.get(i + j).getLoc();
					if (nextLoc.equals("")) {
						j++;
					}
					else {
						pcFound = true;
					}
	
				}
			}
			else {
				pcFound = true;
				outOfBounds = true;
			}
			
		}
		if (!outOfBounds) {
			pc = Integer.parseInt(nextLoc, 16);
		}
		else if (!useStatement){
		       	pc = LOCCTR;
		}
		String opcode = instruction.getMnemonic();
		String objCode = "";
		if (opcode.equals("END")) {
			if (tRec.length() > 7) {
				tRecEndLen = pc + blockStartForRecord;	
				String tRecLen = convertToHex(tRecEndLen - tRecStartLen, 2);
				String finalTRec = "";
				finalTRec += tRec.substring(0, 7);
				finalTRec += tRecLen;
				finalTRec += tRec.substring(7);	
				tRecords.add(finalTRec);
			}
			eRec += firstExecLine;
			endReached = true;
		}
		else if (opcode.equals("BYTE") || opcode.equals("WORD")) {
			objCode = toMachineCodeConst(instruction.getOperands());
		}
		else if (opcode.equals("*")) {
			String operand = instruction.getOperands().substring(1);
			objCode = toMachineCodeConst(operand);

		}
		else {
			//check for format 4, remove '+' if exists
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
				if (operands[0].equals("")) {
					operandAddr[0] = 0;
					isConstant = true;
				}
				else {
					for (j = 0; j < operands.length; j++){
						if ((!(operands[j].equals("")))) {
							String[] values = SYMTAB.getEntry(operands[j]);
							if (values != null) {
								if (values.length > 2) {
									if (values[2].equals("A")) {
										isConstant = true;
									}
									else isConstant = false;
								}
								else {
									isConstant = false;
								}
								String operandAddrStr = values[0];
								operandAddr[j] = Integer.parseInt(operandAddrStr, 16);
							}
							else {
								try {
									String operandAddrStr = operands[j];
									operandAddr[j] = Integer.parseInt(operandAddrStr);
									isConstant = true;
								} catch (NumberFormatException e) {
									if (operands[j].charAt(0) == '=') {
										String[] litVals = LITTAB.getEntry(operands[j].substring(1));
										operandAddr[j] = Integer.parseInt(litVals[2], 16);
									}
									else {
										System.out.println("ERROR: Undefined Symbol '" + operands[j] + "'");
										System.exit(0);
									}
								}
							}
						}
					}
				}
				//check for indexed addressing
				boolean isIndexed = false;
				if (operands[1].equals("X")) {
					isIndexed = true;
				}
				objCode = toMachineCode(opcode, operandAddr[0], operandAddr[1], format, mode, isIndexed, isConstant, pc, base);	

			}
		}
		//add to instruction, tRecord
		if (!objCode.equals("")) {

			instruction.setObjCode(objCode);

			if (!foundFirstExecLine) {
				int firstExecLineLoc = Integer.parseInt(instruction.getLoc(), 16);
				firstExecLine = convertToHex(firstExecLineLoc, 6);
				foundFirstExecLine = true;
			}
			if (Integer.parseInt(instruction.getLoc(), 16) - tRecStartLen >= 1000) {
				tRecEndLen = Integer.parseInt(basicInstructions.get(lastLineToGenerateObjCode + 1).getLoc(), 16);
				String tRecLen = convertToHex(tRecEndLen - tRecStartLen, 2);
				String finalTRec = "";
				finalTRec += tRec.substring(0, 7);
				finalTRec += tRecLen;
				finalTRec += tRec.substring(7);	
				tRecords.add(finalTRec);
				tRec = "T" + "00" + instruction.getLoc() + objCode;
				tRecStartLen = Integer.parseInt(instruction.getLoc(), 16);

			}
			else if (tRec.length() + objCode.length() > 67){
				tRecEndLen = Integer.parseInt(instruction.getLoc(), 16);
				String tRecLen = convertToHex(tRecEndLen - tRecStartLen, 2);
				String finalTRec = "";
				finalTRec += tRec.substring(0, 7);
				finalTRec += tRecLen;
				finalTRec += tRec.substring(7);	
				tRecords.add(finalTRec);
				tRec = "T" + "00" + instruction.getLoc() + objCode;
				tRecStartLen = Integer.parseInt(instruction.getLoc(), 16);

			}
			else tRec += objCode;
			if (format == 4 && !isConstant) {
				int startLoc = Integer.parseInt(instruction.getLoc(), 16) + 1;
				mRec += convertToHex(startLoc, 6);
				mRec += "05";
				mRecords.add(mRec);
				mRec = "M";
			}
			lastLineToGenerateObjCode = i;
		}
		else if (instruction.getMnemonic().equals("USE")) {
			if (instruction.getOperands().equals("")) {
				if (tRec.length() > 7) {
					tRecEndLen = pc + blockStartForRecord;	
					String tRecLen = convertToHex(tRecEndLen - tRecStartLen, 2);
					String finalTRec = "";
					finalTRec += tRec.substring(0, 7);
					finalTRec += tRecLen;
					finalTRec += tRec.substring(7);	
					tRecords.add(finalTRec);
				}
				String tRecStart = instruction.getLoc();
				tRec = "T" + "00" + instruction.getLoc();
				tRecStartLen = Integer.parseInt(instruction.getLoc(), 16);
			}
			else {
				if (tRec.length() > 7) {
					tRecEndLen = pc + blockStartForRecord;
					String tRecLen = convertToHex(tRecEndLen - tRecStartLen, 2);
					String finalTRec = "";
					finalTRec += tRec.substring(0, 7);
					finalTRec += tRecLen;
					finalTRec += tRec.substring(7);	
					tRecords.add(finalTRec);
				}
				String[] blkVals = BLKTAB.getEntry(instruction.getOperands());
				int startOfBlock = Integer.parseInt(blkVals[1], 16);
				String tRecStart = convertToHex(startOfBlock + Integer.parseInt(instruction.getLoc(), 16), 4);
				tRec = "T" + "00" + tRecStart;
				tRecStartLen = Integer.parseInt(tRecStart, 16);
			}
		}
	}
	if (!endReached) {
		tRecords.add(tRec);
		eRec += firstExecLine;
	}
	writer.writeIntermediateFilePass2(basicInstructions);

        ObjectProgram output = new ObjectProgram();
        output.Head = hRec;
        output.Text = tRecords;
        output.End = eRec;
	output.Mod = mRecords;
        FileOutput printer = new FileOutput(fileName+"Obj.txt");
        printer.writeObjectFile(output);
    }

	//Function that generates object code for a line of assembly.
	//Specify the OPCODE as a string, the operand addresses (IN HEX!!!!),
	//the format type (1-4), the addressing mode ("#" for immediate, "@" for indirect, anything else for simple),
	//whether or not index register is used (true for yes, false for no), if the value
        //is a constant or not(determines if relative addressing is used or not), 
        //the PC counter value (ALSO HEX), and the base register value (ALSO HEX). 
 
        //Will return a hex string with the object code.
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
				int disp = 0;
				disp = operandAddr1 - PC;
				//if disp is greater than 12 bits, use base relative
				if (disp >= 2048 || disp <= -2048) {
					disp = operandAddr1 - BASE;
					//if disp still greater than 12 bits, throw error, quit assembly
					if (disp >= 2048 || disp <= -2048) {
						System.out.println("Error: Target Address outside 12 bit limit.");
						System.out.println(OPCODE + " " + operandAddr1 + " " + PC + " " + BASE);
						System.exit(0);
						return machineCode;
					}
					else {
						//if using index reg, set x and b bits to 1, else just b to 1
						if (isIndexed) {
							machineCode += "1100";
							//disp += operandAddr2;
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
						//disp += operandAddr2;
					}
					else machineCode += "0010";
					//if negative number, do twos comp
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
		}
	}//end toMachineCode

	public static String toMachineCodeConst(String operand) {
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
