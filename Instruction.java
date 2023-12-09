// Custom datatype for a parsed Instruction.
public class Instruction {
    String loc;
    String label;
    String mnemonic;
    String operands;
    String comment;
    String objCode;


	public Instruction() {
	}
	
	public Instruction(Instruction instruction) {
		this.loc = instruction.getLoc();
		this.label = instruction.getLabel();
		this.mnemonic = instruction.getMnemonic();
		this.operands = instruction.getOperands();
		this.comment = instruction.getComment();
		this.objCode = instruction.getObjCode();
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}
    	public String getLoc() {
		return this.loc;
	}

	public String getLabel() {
		return this.label;
	}

	public String getMnemonic() {
		return this.mnemonic;
	}

	public String getOperands() {
		return this.operands;
	}

	public String getComment() {
		return this.comment;
	}

	public void setObjCode(String objCode) {
		this.objCode = objCode;
	}
	public String getObjCode() {
		return this.objCode;
	}
}
