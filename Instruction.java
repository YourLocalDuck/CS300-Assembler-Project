// Custom datatype for a parsed Instruction.
public class Instruction {
    String loc;
    String label;
    String mnemonic;
    String operands;
    String comment;
    String objCode;


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

	public void setObjCode(String objCode) {
		this.objCode = objCode;
	}
	public String getObjCode() {
		return this.objCode;
	}
}
