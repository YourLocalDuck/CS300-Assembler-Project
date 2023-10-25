import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

public class FileIO {

    private String fileName;
    private List<Instruction> parsedInstructions;

    FileIO() {
        parsedInstructions = new LinkedList<Instruction>();
    }

    FileIO(String fileName) {
        this.fileName = fileName;
        parsedInstructions = new LinkedList<Instruction>();
    }

    public List<Instruction> getParsedInstructions() {
        if (parsedInstructions.isEmpty())
            parse();
        return parsedInstructions;
    }

    public List<Instruction> parse() {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
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
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return this.parsedInstructions;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        parsedInstructions.clear();
    }
}
