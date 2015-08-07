package za.co.entelect.challenge;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class MTDFhybridPlain {
    private static final String OUTPUT_FILE_NAME = "game.state";
    
    private static final String pre_file = "gameA.state";
    private static final String post_file = "gameB.state";
    
    private static final int default_maxdepth = 18;
    
    public static void main(String[] args) {
        char[][] maze = Util.ReadMaze(args[0]);
        byte[][] bytemaze = QuickMaze.createByteMaze(maze);
        Point aPoint = Util.getCurrentPosition(maze);
        Point bPoint = Util.getCurrentBPosition(maze);
        
        Util.writeMaze(maze, pre_file);
        
        List<Point> possibleMoveList = Util.determineMoves(aPoint, maze);
        
        int moveindex = 0;
        int maxscore = Integer.MIN_VALUE;
        
        for(int i=0; i<possibleMoveList.size(); i++) {
            Point movep = possibleMoveList.get(i);
            int movepointvalue = QuickMaze.value(bytemaze[movep.x][movep.y]);
            byte[][] movedmaze = QuickMaze.copyMaze(bytemaze);
            QuickMaze.applyMove(aPoint.x, aPoint.y, movep.x, movep.y, movedmaze);
            QuickMaze node = new QuickMaze(movedmaze, movepointvalue, movep.x, movep.y, bPoint.x, bPoint.y);
            
            //movepointvalue += alphabeta(node, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            
            movepointvalue += 35*mtfd(node,0);
            movepointvalue += DepthLimitedAttackSuicideAvoid.valueOf(movep,aPoint,0,35,maze);
            
            if(movepointvalue>maxscore) {
                moveindex=i;
                maxscore=movepointvalue;
            }
        }
        
        maze = Util.makeMove(aPoint, possibleMoveList.get(moveindex), maze);
        Util.writeMaze(maze, post_file);
        
        Util.writeMaze(maze, OUTPUT_FILE_NAME);
    }
    
    private static int mtfd(QuickMaze node, int f) {
        int g = f; // f = initial guess
        int upperBound = Integer.MAX_VALUE;
        int lowerBound = Integer.MIN_VALUE;
        int i=0;
        while (lowerBound < upperBound) {
            i++;
            int beta;
            if (g == lowerBound) {
                beta = g+1;
            }
            else {
                beta = g;
            }
            g = alphabeta(node, 0, beta-1, beta, false);
            if (g < beta) {
                upperBound = g;
            }
            else {
                lowerBound = g;
            }
        }
        return g;
    }
    
    private static int alphabeta(QuickMaze node, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth >= default_maxdepth || node.isTerminal()) {
            return node.getPointValue();
        }
        
        if (maximizingPlayer) {
            List<QuickMaze> children = node.getChildNodes(true);
            Collections.sort(children);
            for (QuickMaze child: children) {
                alpha = Math.max(alpha, alphabeta(child, depth + 1, alpha, beta,  false));
                if(beta <= alpha) {
                    break;
                }
            }
            return alpha;
        }
        else {
            List<QuickMaze> children = node.getChildNodes(false);
            Collections.sort(children,Collections.reverseOrder());
            for (QuickMaze child: children) {
                beta = Math.min(beta, alphabeta(child, depth + 1, alpha, beta, true));
                if(beta <= alpha) {
                    break;
                }
            }
            return beta;
        }
    }
}
