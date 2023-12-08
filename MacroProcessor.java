import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MacroProcessor { // TODO: Deal with arguments in macro calls. Currently, the macro call is replaced with the macro definition, but the arguments are not replaced with the arguments in the macro call.
    private DEFTAB defTab;

    public MacroProcessor() {
        this.defTab = new DEFTAB();
    }

    public MacroProcessor(List<Instruction> instructionList) {
        this.defTab = new DEFTAB();
        processMacro(instructionList);
    }

    public void processMacro(List<Instruction> instructionList) {
        loadMacros(instructionList);
    }

    private void loadMacros(List<Instruction> instructionList) {
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
                    String macroName = instruction.getLabel();
                    List<Instruction> macroInstructions = new LinkedList<Instruction>();
                    i++;
                    while (!instructionList.get(i).getMnemonic().equals("MEND")) {
                        macroInstructions.add(instructionList.get(i));
                        i++;
                    }
                    this.defTab.addMacro(macroName, macroInstructions);
                }
                // If the instruction is not a macro definition, and the instruction is not a macro call, then we are done defining macros. Next, we will expand macros.
                else if (!instruction.getMnemonic().equals("MACRO")) {
                    defineMacros = false;
                    firstInstruction = i;
                }
            }
        }

        // Second, expand macros. If the instruction is a macro call, replace the macro call with the macro definition taken from DEFTAB. Since the lines leading up to the first instruction have already been processed above, we can start at the first instruction.
        for (int i = firstInstruction; i < instructionList.size(); i++) {
            Instruction instruction = instructionList.get(i);
            if (this.defTab.containsMacro(instruction.getMnemonic())) {
                String macroName = instruction.getMnemonic();
                List<Instruction> macroInstructions = this.defTab.getMacro(macroName);
                instructionList.remove(i);
                instructionList.addAll(i, macroInstructions);
            }
            else {
                System.out.println("Error: Macro " + instruction.getMnemonic() + " not defined.");
                continue;
            }
        }
    }
}

// Implementation of both DEFTAB and NAMTAB, as using a Map, NAMTAB is a subset of DEFTAB. Storing macro definitions, macro names, and indices in one data structure allows for easy lookup of macro names and their definitions.
class DEFTAB {
    public Map<String, List<Instruction>> defTab;

    public DEFTAB() {
        this.defTab = new HashMap<String, List<Instruction>>();
    }

    public void addMacro(String macroName, List<Instruction> macroInstructions) {
        this.defTab.put(macroName, macroInstructions);
    }

    public List<Instruction> getMacro(String macroName) {
        return this.defTab.get(macroName);
    }

    public void removeMacro(String macroName) {
        this.defTab.remove(macroName);
    }

    public void printMacros() {
        for (String macroName : this.defTab.keySet()) {
            System.out.println(macroName);
        }
    }

    public boolean containsMacro(String macroName) {
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
