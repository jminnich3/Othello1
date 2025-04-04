package othello;

import java.io.FileNotFoundException;
import java.util.Vector;

public class RunTestCases {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        if (args.length != 3) {
            System.err.println("Usage: java othello.RunTestCases "+
                    "<playerClass> \n" +
                    "<testCasesFile> \n" +
                    "<depthLimit> \n");
            System.exit(-1);
        }

        int argIndex = 0;

        String playerClass = args[argIndex++];
        String testCaseFilename = args[argIndex++];
        final int depthLimit = Integer.parseInt(args[argIndex++]);


        AIPlayer player = (AIPlayer) Misc.getPlayerInstance(playerClass);

        boolean passed = player.checkTestCases(testCaseFilename, depthLimit, true);

        if (passed) {
            System.out.println("Passed test cases (" + testCaseFilename + ")");
        }
        else {
            System.out.println("FAILED test cases (" + testCaseFilename + ")");
        }
    }
}
