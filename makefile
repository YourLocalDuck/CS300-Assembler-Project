assembler: Main.java FileInput.java FileOutput.java ObjectProgram.java Table.java OPTAB.java Instruction.java
	javac -g *.java
	
main: Main.java
	javac -g Main.java

input: FileInput.java
	javac -g FileInput.java

output: FileOutput.java
	javac -g FileOutput.java

objectprogram: ObjectProgram.java
	javac -g ObjectProgram.java

table: Table.java
	javac -g Table.java

OPTAB: OPTAB.java
	javac -g OPTAB.java

instruction: Instruction.java
	javac -g Instruction.java

clean: 
	rm *.class

run: Main.java
	java Main
