import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.io.*;

/*
 * Class for handling inputting from file and parsing the file's contents into a custom datatype "Instruction"
 */
public class FileInput {

	private String fileName;
	private List<Instruction> parsedInstructions;

	FileInput() {
		parsedInstructions = new LinkedList<Instruction>();
	}

	FileInput(String fileName) {
		this.fileName = fileName;
		parsedInstructions = new LinkedList<Instruction>();
	}

	// If a parsed instruction for the current filename already exists, return that.
	// Otherwise, parse the file and create a parsed instruction set.
	public List<Instruction> getParsedInstructions(int pass) {
		if (parsedInstructions.isEmpty())
			parse(pass);
		return parsedInstructions;
	}

	// Parsing the instruction by breaking it into a label, mnemonic, operands, and
	// comment. Skips lines that are empty or start with '.' The fields label,
	// mnemonic, operands, and comment are assumed to be separated by a ' '(tab).
	// Also, depending on whether each line has 2 or less tab separated fields,
	// operands or comment is left blank.
	public List<Instruction> parse(int pass) {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			if (pass == 1) {
				while ((line = br.readLine()) != null) {
					if (line.isEmpty() || line.startsWith("."))
						continue;
					String[] instructionParts = line.split("\t");
					Instruction instruction = new Instruction();
					instruction.label = instructionParts[0];
					instruction.mnemonic = instructionParts[1];
					instruction.operands = (instructionParts.length >= 3) ? instructionParts[2] : "";
					instruction.comment = (instructionParts.length >= 4) ? instructionParts[3] : "";
					this.parsedInstructions.add(instruction);
				}
			} else if (pass == 2) {
				while ((line = br.readLine()) != null) {
					if (line.isEmpty() || line.startsWith("."))
						continue;
					String[] instructionParts = line.split("\t");
					Instruction instruction = new Instruction();
					instruction.loc = instructionParts[0];
					instruction.mnemonic = instructionParts[1];
					if (instructionParts.length == 3) {
						instruction.operands = instructionParts[2];
					}
					else {
						instruction.operands = "";
					}
					this.parsedInstructions.add(instruction);
				}
			} else {
				System.out.println(
						"Specified invalid pass. Check call to getParsedInstructions and ensure pass is 1 or 2.");
				System.exit(0);
			}
			br.close();
		} catch (Exception e) {
			System.out.println("badddd");
			// TODO: handle exception
			e.printStackTrace();
		}
		return this.parsedInstructions;
	}

	// Setter for filename, and also clears parsedInstructions, so that
	// getParsedInstructions() will reset properly, as the old parsed data, if it
	// exists, will be irrelevant.
	public void setFileName(String fileName) {
		this.fileName = fileName;
		parsedInstructions.clear();
	}
}
