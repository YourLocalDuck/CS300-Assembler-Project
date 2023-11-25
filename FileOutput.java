import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * FileOutput
 */
public class FileOutput {
    private String fileName;

    FileOutput() {
    }

    FileOutput(String fileName) {
        this.fileName = fileName;
    }

    //writes to the intermediate file. Specify pass 1 or pass 2 of assembly as second argument.
    void writeIntermediateFile(List<Instruction> instructionList, int pass) {
	try(BufferedWriter writer = new BufferedWriter(new FileWriter("Intr" + this.fileName))) {
		if (pass == 1) {
			for (int i = 0; i < instructionList.size(); i++) {
				Instruction instruction = instructionList.get(i);
				writer.write(instruction.getLoc() + " ");
				writer.write(instruction.getMnemonic() + " ");
				writer.write(instruction.getOperands() + " ");
				writer.newLine();
			}
		}
		else if (pass == 2) {
			for (int i = 0; i < instructionList.size(); i++) {
				Instruction instruction = instructionList.get(i);
				writer.write(instruction.getLoc() + " ");
				writer.write(instruction.getMnemonic() + " ");
				writer.write(instruction.getOperands() + " ");
				if (instruction.getObjCode() != null){
					writer.write(instruction.getObjCode());
				}
				writer.newLine();
			}
		}
		else {
		    System.out.println("Specified invalid pass. Check call to getParsedInstructions and ensure pass is 1 or 2.");
		    System.exit(0);
		}	
	} catch (IOException e) {
		System.out.println("could not write intermediate file");
	}
    }


    void writeObjectFile(ObjectProgram output) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName))) {
            writer.write(output.Head);
            writer.newLine();
            writer.write(output.Text);
            writer.newLine();
            writer.write(output.End);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
