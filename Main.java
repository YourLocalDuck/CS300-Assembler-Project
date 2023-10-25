import java.util.List;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("hello world");
        FileIO fileParser = new FileIO("basic.txt");
        List<Instruction> basicInstructions = fileParser.getParsedInstructions();
        basicInstructions.forEach((Instruction inst) -> printInstruction(inst));
    }

    static void printInstruction(Instruction inst) {
        System.out.println(inst.comment);
        System.out.println(inst.mnemonic);
        System.out.println(inst.operands);
        System.out.println(inst.comment);
    }
}
