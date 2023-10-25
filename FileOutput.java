import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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

    void writeFile(ObjectProgram output) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName))) {
            writer.write(output.Head);
            writer.newLine();
            writer.write(output.Text);
            writer.newLine();
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