import java.util.*;

public class UIDNetworkDriver {

    public static void main (String[] args) {
        int n = Integer.valueOf(args[0]);
        String mode = args[1];
        UIDNetworkDriver uidNetworkDriver = new UIDNetworkDriver();
        uidNetworkDriver.generateUniqueIDs(n,mode);
    }

    private void generateUniqueIDs(int n, String mode) {
        List<MockComputer> computers = new ArrayList<>(n);
        // instances for mock computer
        for(int i = 0; i < n; i++) {
            computers.add(new MockComputer());
        }

        int roundNumber = 0;
        boolean idsAreNotUnique = true;
        List<Integer> idList;
        while(idsAreNotUnique) {
            roundNumber++;
            idList = new ArrayList<>();
            for(int i = 0; i < n; i++) {
                MockComputer computer = computers.get(i);
                idList.add(computer.round(roundNumber));
            }
            if(mode.equals("verbose")) {
                printIDs(roundNumber, computers);
            }
            idsAreNotUnique = areIDsDuplicate(idList, n);
        }
        if(mode.equals("silent")) {
            printIDs(roundNumber, computers);
        }
    }

    private boolean areIDsDuplicate(List<Integer> idList, int n) {
        Set<Integer> idSet = new HashSet<>();
        for(Integer idValue : idList) {
            idSet.add(idValue);
        }
        return idSet.size() != n;
    }

    private void printIDs(int roundNumber, List<MockComputer> computers) {
        System.out.print("Round " + roundNumber + ": " );
        int max = 0;
        for (MockComputer computer : computers) {
            System.out.print(computer.getId() + " ");
            if(max < computer.getId()) {
                max = computer.getId();
            }
        }
        int maxBits = 1;
        if(max > 0) {
            maxBits = (int) (Math.log(max)/Math.log(2)) + 1;
        }
        System.out.println("Max Bits " + maxBits);
    }
}