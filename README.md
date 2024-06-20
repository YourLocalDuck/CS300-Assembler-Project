# Project Introduction
This is a SIC/XE assembler written entirely in Java. It performs two passes over the input file, first to generate the symbol table and the second to generate the object code. It generates two files, one for the symbol table, and another for the object code.  This assembler includes all SIC/XE instructions, all instruction formats, and all addressing modes.

SIC-XE stands for Simplified Instructional Computer Extra Equipment, and is a hypothetical computer system. It is commonly used for instructional use. This specific implementation of the SIC/XE assembler is faithful to the logic described in the textbook, System Software: An Introduction to Systems Programming, by Leland Beck, 1985.

# How to Compile
To compile, make sure the JDK is installed on your machine, as well as optionally GNU make.

If both are installed, run `make assembler` to compile.

If GNU make is not installed, run `javac *.java`.

# How to run
Place input files into the base directory. For examples of input files, refer to the txt files in the directory by default, namely `basic.txt`, `control_section.txt`, `functions.txt`, `literals.txt`, `macros.txt`, `opcodes.txt`, `prog_blocks.txt`, `registers.txt`. The instructions in the input files must be in the SIC/XE instruction set.

From here, with make, run `make run` to execute the project and convert the input file into a symbol table, and then object code.

Without make, run `java Main` to do the same.
The program will then prompt for a file name, which must be given without the extension. It will then generate {Filename}Intr.txt as the symbol table, and {filename}Obj.txt as the object code.

# Input/Output
The file "basic.txt" from this repository is given as the input.
```
  basic.txt:
  	LDS	#3	.Initialize Register S to 3
  	LDT	#300	.Initialize Register T to 300
  	LDX	#0	.Initialize Index Register to 0
  ADDLP	LDA	ALPHA,X	.Load Word from ALPHA into Register A
  	ADD	BETA,X	.Add Word From BETA
  	STA	GAMMA,X	.Store the Result in a work in GAMMA
  	ADDR	S,X	.ADD 3 to INDEX value
  	COMPR	X,T	.Compare new INDEX value to 300
  	JLT	ADDLP	.Loop if INDEX value is less than 300
  ALPHA	RESW	100
  BETA	RESW	100
  GAMMA	RESW	100
```

This file will then be converted into a symbol table, and basicIntr.txt will look like so:
```
0000    LDS     #3      6D0003
0003    LDT     #300    75012C
0006    LDX     #0      050000
0009    LDA     ALPHA,X 03A00D
000C    ADD     BETA,X  1BA136
000F    STA     GAMMA,X 0FA25F
0012    ADDR    S,X     9041
0014    COMPR   X,T     A015
0016    JLT     ADDLP   3B2FF0
0019    RESW    100
0145    RESW    100
0271    RESW    100
```

Finally, the symbol table will be onverted into object code, which will look like below.
```
H00000000039D
T0000006D000375012C05000003A00D1BA1360FA25F9041A0153B2FF0
E000000
```

# Team
This project was created for CS300 by Michael Moore, Prince Rajaruban, and Luis Martinez
