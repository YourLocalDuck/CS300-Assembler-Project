import java.util.List;

public class Main {
    
    public static void main(String[] args) {
        String fileName = "basic";
        FileInput fileParser = new FileInput(fileName+".txt");
        List<Instruction> basicInstructions = fileParser.getParsedInstructions();
        basicInstructions.forEach((Instruction inst) -> printInstruction(inst));

        ObjectProgram output = new ObjectProgram();
        output.Head = "testHead";
        output.Text = "testText";
        output.End = "testEnd";
        FileOutput printer = new FileOutput(fileName+".o");
        printer.writeFile(output);
    }

    static void printInstruction(Instruction inst) {
        System.out.println(inst.comment);
        System.out.println(inst.mnemonic);
        System.out.println(inst.operands);
        System.out.println(inst.comment);
    }
}
