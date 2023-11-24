import java.util.List;

public class Main {
    
    public static void main(String[] args) {
	//pass 1
	Table SYMTAB = new Table();
	int LOCCTR = 0;
	OPTABHashTable OPTAB = new OPTABHashTable();
        String fileName = "basic";
        FileInput fileParser = new FileInput(fileName+".txt");
        List<Instruction> basicInstructions = fileParser.getParsedInstructions();
        for (int i = 0; i < basicInstructions.size(); i++) {
		Instruction instruction = basicInstructions.get(i);
		String label = instruction.getLabel();
		if (!(label.equals(""))) {
			String hexLoc = convertToHex(LOCCTR, 4);
			String[] labelValues = {hexLoc};
			boolean canAdd = SYMTAB.addEntry(label, labelValues);
			if (!canAdd) {
				System.out.println("ERROR: Duplicate symbol '" + label + "'");
				System.exit(0);
			}
		}
		String opcode = OPTAB.findOperation(instruction.getMnemonic());
		if (opcode != "") {
			String hexLoc = convertToHex(LOCCTR, 4);
			basicInstructions.get(i).setLoc(hexLoc);
			LOCCTR += 3;
		}
		else {
			opcode = instruction.getMnemonic();
			if (opcode.equals("WORD")) {
				System.out.println("word");
			}
			else if (opcode.equals("RESB")) {
				System.out.println("resb");
			}
			else if (opcode.equals("RESW")) {
				int operand = Integer.parseInt(instruction.getOperands());
				String hexLoc = convertToHex(LOCCTR, 4);
				basicInstructions.get(i).setLoc(hexLoc);
				LOCCTR += 3 * operand;

			}
			else if (opcode.equals("BYTE")) {
				System.out.println("byte");
			}
			else {
				System.out.println(opcode);
				System.out.println("ERROR: invalid operation");
				System.exit(0);
			}
		}

	}
	
	SYMTAB.printTable();

	FileOutput writer = new FileOutput("test.txt");
	writer.writeIntermediateFile(basicInstructions);

        /*ObjectProgram output = new ObjectProgram();
        output.Head = "testHead";
        output.Text = "testText";
        output.End = "testEnd";
        FileOutput printer = new FileOutput(fileName+".o");
        printer.writeFile(output);*/
    }

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
		else return hexNumber;

    }

    static void printInstruction(Instruction inst) {
        System.out.println(inst.comment);
        System.out.println(inst.mnemonic);
        System.out.println(inst.operands);
        System.out.println(inst.comment);
    }
}
