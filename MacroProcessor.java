import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// Class for processing macros. To use, create a new MacroProcessor object, passing in a list of instructionss. If done this way, the macro processor will automatically process the macros. Otherwise, call the processMacro method, passing in a list of instructions. 

// The program will return the processed instructions, with the macro expanded when getProcessed() is called. If no instructions are provided, this method will return null.
public class MacroProcessor {

    // Example use of MacroProcessor
    public static void main(String[] args) {
        FileInput fileParser = new FileInput("macros.txt");
        List<Instruction> instructionList = fileParser.getParsedInstructions(1);

        MacroProcessor macroProcessor = new MacroProcessor(instructionList);

        for (Instruction instruction : macroProcessor.getProcessed()) {
            System.out.println(instruction.getLabel() + "\t" + instruction.getMnemonic() + "\t" + instruction.getOperands() + "\t" + instruction.getComment());
        }

    }

    protected NAMTAB namTab;
    protected DEFTAB defTab;
    List<Instruction> processed;

    public MacroProcessor() {
        this.namTab = new NAMTAB();
        this.defTab = new DEFTAB();
    }

    public MacroProcessor(List<Instruction> instructionList) {
        this.namTab = new NAMTAB();
        this.defTab = new DEFTAB();
        processMacro(instructionList);
    }

    public List<Instruction> processMacro(List<Instruction> instructionList) {
        return this.processed = loadMacros(instructionList);
    }

    public List<Instruction> getProcessed() {
        return this.processed;
    }

    protected List<Instruction> loadMacros(List<Instruction> instructionList) {
        boolean defineMacros = true;
        int firstInstruction = 0;

        // First, define macros. If the instruction is a macro definition, add it to the DEFTAB. Finish defining macros when the instruction is not a macro definition.
        while (defineMacros)
        {
            for (int i = 0; i < instructionList.size(); i++) {
                Instruction instruction = instructionList.get(i);
                // Example Macro: RDBUFF	MACRO	&INDEV,&BUFADR,&RECLTH	
                // Label: RDBUFF, Mnemonic: MACRO, Operands: &INDEV,&BUFADR,&RECLTH, Comment: null
                if (instruction.getMnemonic().equals("MACRO")) {
                    MacroInstruction macroDef = new MacroInstruction(instruction);
                    String macroName = macroDef.getLabel();
                    List<Instruction> macroInstructions = new LinkedList<Instruction>();
                    instructionList.remove(i);
                    while (!instructionList.get(i).getMnemonic().equals("MEND")) {
                        macroInstructions.add(instructionList.get(i));
                        instructionList.remove(i);
                    }
                    instructionList.remove(i);
                    i--;
                    this.namTab.addMacro(macroName, macroDef);
                    this.defTab.addMacro(macroDef, macroInstructions);
                }
                // If the instruction is not a macro definition, and the instruction is not a macro call, then we are done defining macros. Next, we will expand macros.
                else if (!instruction.getMnemonic().equals("MACRO") && !instruction.getMnemonic().equals("START")) {
                    defineMacros = false;
                    firstInstruction = i;
                    break;
                }
            }
        }

        // Second, expand macros. If the instruction is a macro call, replace the macro call with the macro definition taken from DEFTAB. Since the lines leading up to the first instruction have already been processed above, we can start at the first instruction.
        for (int i = firstInstruction; i < instructionList.size(); i++) {
            Instruction instruction = instructionList.get(i);
            if (this.namTab.containsMacro(instruction.getMnemonic())) {
                MacroInstruction macroInvoc = new MacroInstruction(instruction);
                List<Instruction> expandedMacro = expandMacro(macroInvoc);
                instructionList.remove(i);
                instructionList.addAll(i, expandedMacro);
            }
            else {
                //System.out.println("Error: Macro " + instruction.getMnemonic() + " not defined.");
                continue;
            }
        }

        return instructionList;
    }

    protected List<Instruction> expandMacro(MacroInstruction macroInvoc) {
        // Take all arguments from macro call and store in ARGTAB. Key is argument name, value is argument value.
        ARGTAB argTab = new ARGTAB();
        MacroInstruction macroDef = this.namTab.getMacro(macroInvoc.getMnemonic());
        for (int i = 0; i < macroInvoc.getArguments().length; i++) {
            argTab.addArgument(macroDef.getArguments()[i], macroInvoc.getArguments()[i]);
        }

        // Replace macro call with macro definition. Replace macro arguments with macro argument values. Steps: 1. Copy macro definition from DEFTAB. 2. Replace macro arguments with macro argument values. 3. Return macro definition.
        List<Instruction> macroInstructions = this.defTab.getMacro(macroDef);
        List<Instruction> expandedMacroInstructions = new LinkedList<Instruction>();

        // Different first instruction for macro definition than macro call. Macro definition has no label, so we need to add the label from the macro call to the macro definition.
        Instruction firstInstruction = macroInstructions.get(0);
        Instruction expandedFirstInstruction = new Instruction(firstInstruction);

        // Replace macro arguments with macro argument values.
        String ops = firstInstruction.getOperands();
        for (String argumentName : argTab.argTab.keySet()) {
            ops = ops.replace(argumentName, argTab.getArgument(argumentName));
        }
        expandedFirstInstruction.operands = ops;
        expandedFirstInstruction.label = macroInvoc.getLabel();
        expandedMacroInstructions.add(expandedFirstInstruction);

        // Rest of the instructions

        for (int i = 1; i < macroInstructions.size(); i++) {
            Instruction instruction = macroInstructions.get(i);
            Instruction expandedInstruction = new Instruction(instruction);

            // Replace macro arguments with macro argument values.
            String operands = instruction.getOperands();
            for (String argumentName : argTab.argTab.keySet()) {
                operands = operands.replace(argumentName, argTab.getArgument(argumentName));
            }
            expandedInstruction.operands = operands;
            expandedMacroInstructions.add(expandedInstruction);
        }

        return expandedMacroInstructions;
        
    }
}

// Implementation of NAMTAB, which stores macro names and their definitions. Storing macro names and their definitions in one data structure.
class NAMTAB {
    public Map<String, MacroInstruction> namTab;

    public NAMTAB() {
        this.namTab = new HashMap<String, MacroInstruction>();
    }

    public void addMacro(String macroName, MacroInstruction macroDef) {
        this.namTab.put(macroName, macroDef);
    }

    public MacroInstruction getMacro(String macroName) {
        return this.namTab.get(macroName);
    }

    public void removeMacro(String macroName) {
        this.namTab.remove(macroName);
    }

    public void printMacros() {
        for (String macroName : this.namTab.keySet()) {
            System.out.println(macroName);
        }
    }

    public boolean containsMacro(String macroName) {
        return this.namTab.containsKey(macroName);
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof NAMTAB))
            return false;
        NAMTAB otherNAMTAB = (NAMTAB) other;
        return this.namTab.equals(otherNAMTAB.namTab);
    }
}

// Implementation of DEFTAB, which stores macro definitions and their instructions. The DEFTAB is a subset of the NAMTAB, as the macro definitions are a subset of the macro names. Storing macro definitions and their instructions in one data structure.
class DEFTAB {
    public Map<MacroInstruction, List<Instruction>> defTab;

    public DEFTAB() {
        this.defTab = new HashMap<MacroInstruction, List<Instruction>>();
    }

    public void addMacro(MacroInstruction macroName, List<Instruction> macroInstructions) {
        this.defTab.put(macroName, macroInstructions);
    }

    public List<Instruction> getMacro(MacroInstruction macroName) {
        return this.defTab.get(macroName);
    }

    public void removeMacro(MacroInstruction macroName) {
        this.defTab.remove(macroName);
    }

    public void printMacros() {
        for (MacroInstruction macroName : this.defTab.keySet()) {
            System.out.println(macroName);
        }
    }

    public boolean containsMacro(MacroInstruction macroName) {
        return this.defTab.containsKey(macroName);
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof DEFTAB))
            return false;
        DEFTAB otherDEFTAB = (DEFTAB) other;
        return this.defTab.equals(otherDEFTAB.defTab);
    }
}

// Implementation of ARGTAB, which stores macro arguments and their values. The ARGTAB is a subset of the NAMTAB, as the macro arguments are a subset of the macro names. Storing macro arguments and their values in one data structure.
class ARGTAB {
    public Map<String, String> argTab;

    public ARGTAB() {
        this.argTab = new HashMap<String, String>();
    }

    public void addArgument(String argumentName, String argumentValue) {
        this.argTab.put(argumentName, argumentValue);
    }

    public String getArgument(String argumentName) {
        return this.argTab.get(argumentName);
    }

    public void removeArgument(String argumentName) {
        this.argTab.remove(argumentName);
    }

    public void printArguments() {
        for (String argumentName : this.argTab.keySet()) {
            System.out.println(argumentName);
        }
    }
}

// Class inherited from Instruction.java, for macro definitions to have an additional member variable for the macro arguments. Using Instruction.java as is would require a separate data structure to store macro arguments, as currently macro arguments are stored in the operands member variable without any distinction from the operands.
class MacroInstruction extends Instruction {
    protected String[] arguments;

    public MacroInstruction(Instruction instruction) {
        this.loc = instruction.getLoc();
        this.label = instruction.getLabel();
        this.mnemonic = instruction.getMnemonic();
        this.operands = instruction.getOperands();
        this.comment = instruction.getComment();
        this.objCode = instruction.getObjCode();

        // operands is a string of comma separated arguments. Split the string into an array of arguments. Ex: operands = "&INDEV,&BUFADR,&RECLTH" -> arguments = ["&INDEV", "&BUFADR", "&RECLTH"]
        this.arguments = this.operands.split(",");
    }

    public String[] getArguments() {
        return this.arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
}
