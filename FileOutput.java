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

    void writeIntermediateFile(List<Instruction> instructionList) {
	try(BufferedWriter writer = new BufferedWriter(new FileWriter("Intr" + this.fileName))) {
		for (int i = 0; i < instructionList.size(); i++) {
			Instruction instruction = instructionList.get(i);
			writer.write(instruction.getLoc() + " ");
			writer.write(instruction.getMnemonic() + " ");
			writer.write(instruction.getOperands() + " ");
			//writer.write(instruction.getObjCode());
			writer.newLine();
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
