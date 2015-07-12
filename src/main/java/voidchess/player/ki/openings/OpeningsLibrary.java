package voidchess.player.ki.openings;

import voidchess.helper.Move;
import voidchess.helper.ResourceFinder;
import voidchess.helper.TreeNode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by stephan on 12.07.2015.
 */
public class OpeningsLibrary {
    final private TreeNode<String> openingsRootNode;

    public OpeningsLibrary(String relativePathToOpeningsFile) {
        List<String> openingSequences = loadOpeningSequencesFromFile(relativePathToOpeningsFile);
        openingsRootNode = parseOpenings(openingSequences);
    }

    public List<Move> nextMove(String history) {
        TreeNode<String> currentNode = openingsRootNode;
        if(!history.equals("")) {
            String[] moves = history.split(",");
            for (String move : moves) {
                move = move.trim();
                currentNode = currentNode.getChild(move);
                if (currentNode == null) {
                    return Collections.EMPTY_LIST;
                }
            }
        }
        Stream<String> moveDescriptionsFound = currentNode.getChildData();

//        List<Move> movesFound = new LinkedList<>();
//        List<String> moveDescriptionsFound1 = moveDescriptionsFound.collect(Collectors.toList());
//        for(String moveDesc: moveDescriptionsFound1) {
//            Move move = Move.get(moveDesc);
//            movesFound.add(move);
//        }
        List<Move> movesFound = moveDescriptionsFound.map(
                (String moveDesc)->Move.get(moveDesc)
        ).collect(Collectors.toList());
        return movesFound;
    }

    private List<String> loadOpeningSequencesFromFile(String relativePathToOpeningsFile) {
        List<String> openingSequences = new LinkedList<>();
        try {
            InputStream fileStream = ResourceFinder.getResourceStream(relativePathToOpeningsFile);
            try(BufferedReader br = new BufferedReader(new InputStreamReader(fileStream))) {
                for(String line; (line = br.readLine()) != null; ) {
                    line = line.trim();
                    if(line.length()==0 || line.startsWith("#")) {
                        continue;
                    }
                    openingSequences.add(line);
                }
            }
        }catch (Exception e) {}
        return openingSequences;
    }

    private TreeNode<String> parseOpenings(List<String> openingSequences) {
        TreeNode<String> root = TreeNode.getRoot(String.class);

        outerloop:
        for(String openingSequence: openingSequences) {
            TreeNode<String> currentNode = root;
            String[] moves = openingSequence.split(",");
            for(String move: moves) {
                move = move.trim();
                if(isValidMoveFormat(move)) {
                    currentNode = currentNode.addChild(move);
                }else{
                    log("discarding sequence: "+openingSequence+"; illegal token: "+move);
                    continue outerloop;
                }
            }
        }
        
        return root;
    }

    private boolean isValidMoveFormat(String move) {
        try{
            Move.get(move);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    //TODO proper logging
    private static void log(String errorMsg) {
        System.out.println(errorMsg);
    }
}
