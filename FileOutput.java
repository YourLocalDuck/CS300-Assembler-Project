import java.util.*;
import java.io.*;

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
    void writeIntermediateFilePass1(List<Instruction> instructionList, Table LITTAB) {
	try(BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName))) {
		for (int i = 0; i < instructionList.size(); i++) {
			Instruction instruction = instructionList.get(i);
			if ((instruction.getMnemonic().equals("LTORG") || instruction.getMnemonic().equals("END"))) {
				writer.write("" + "\t");
				writer.write(instruction.getMnemonic() + "\t");
				writer.write("");
				int LOCCTR = Integer.parseInt(instruction.getLoc(), 16);
				writer.newLine();

				Enumeration<String> enumKey = LITTAB.table.keys();
				while(enumKey.hasMoreElements()) {
					String key = enumKey.nextElement();
					String[] litVals = LITTAB.getEntry(key);
					if (Integer.parseInt(litVals[2], 16) == LOCCTR) {
						writer.write(instruction.getLoc() + "\t");
						writer.write("*" + "\t");
						writer.write("=" + key);
						LOCCTR += Integer.parseInt(litVals[1], 16);
						writer.newLine();
					}
				}

			}
			else {
				writer.write(instruction.getLoc() + "\t");
				writer.write(instruction.getMnemonic() + "\t");
				writer.write(instruction.getOperands());
				writer.newLine();
			}
		}
		writer.close();
	} catch (IOException e) {
		System.out.println("could not write intermediate file");
	}
    }


    void writeIntermediateFilePass2(List<Instruction> instructionList) {
	try(BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName))) {
		for (int i = 0; i < instructionList.size(); i++) {
			Instruction instruction = instructionList.get(i);
			if (instruction.getMnemonic().equals("LTORG") || instruction.getMnemonic().equals("END")) {
				writer.write("" + "\t");
			}
			else {
				writer.write(instruction.getLoc() + "\t");
			}
			writer.write(instruction.getMnemonic() + "\t");
			writer.write(instruction.getOperands() + "\t");
			if (instruction.getObjCode() != null){
				writer.write(instruction.getObjCode());
			}
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
	    for (int i = 0; i < output.Text.size(); i++){
         	   writer.write(output.Text.get(i));
           	   writer.newLine();
	    }
	    for (int i = 0; i < output.Mod.size(); i++) {
		    writer.write(output.Mod.get(i));
		    writer.newLine();
	    }
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
