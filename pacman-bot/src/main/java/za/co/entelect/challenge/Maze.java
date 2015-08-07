package za.co.entelect.challenge;

import java.util.ArrayList;
import java.util.List;

public class Maze {
    private static final int WIDTH = 19;
    private static final int HEIGHT = 22;
    private static final int PORTAL1_X = 10;
    private static final int PORTAL1_Y = 18;
    private static final int PORTAL2_X = 10;
    private static final int PORTAL2_Y = 0;
    
    private static final int CENTER_X = 10;
    private static final int CENTER_Y = 9;
    
    private static final char WALL = '#';
    private static final char PILL = '.';
    private static final char PILL_LARGE = '*';
    private static final char PILL_POISON = '!';
    private static final char PLAYER_A = 'A';
    private static final char PLAYER_B = 'B';
    private static final char EMPTY = ' ';
    
    private int Ax;
    private int Ay;
    private int Bx;
    private int By;
    
    private int pointvalue;
    
    private char[][] maze;
    
    public Maze(char[][] basemaze, int pointval, int ax, int ay, int bx, int by) {
        maze = basemaze;
        pointvalue = pointval;
        Ax=ax;
        Ay=ay;
        Bx=bx;
        By=by;
    }
    
    public char[][] getBaseMaze() {
        return maze;
    }
    
    public int getPointValue() {
        return pointvalue;
    }
    
    public List<Maze> getChildNodes(boolean playerA) {
        List<Maze> mazelist = new ArrayList<Maze>();
        int currentx, currenty;
        if(playerA) {
            currentx=Ax;
            currenty=Ay;
        }
        else {
            currentx=Bx;
            currenty=By;
        }
        
        
        if (currenty + 1 < WIDTH) {
            addMazeAfterMove(playerA, currentx, currenty, currentx, currenty+1, mazelist);
        }

        if (currenty - 1 >= 0) {
            addMazeAfterMove(playerA, currentx, currenty, currentx, currenty-1, mazelist);
        }

        if (currentx + 1 < HEIGHT) {
            addMazeAfterMove(playerA, currentx, currenty, currentx+1, currenty, mazelist);
        }

        if (currentx - 1 >= 0) {
            addMazeAfterMove(playerA, currentx, currenty, currentx-1, currenty, mazelist);
        }

        if (currentx == PORTAL1_X && currenty == PORTAL1_Y) {
            //moveList.add(new Point(PORTAL2_X, PORTAL2_Y));
            addMazeAfterMove(playerA, currentx, currenty, PORTAL2_X, PORTAL2_Y, mazelist);
        }

        if (currentx == PORTAL2_X && currenty == PORTAL2_Y) {
            //moveList.add(new Point(PORTAL1_X, PORTAL1_Y));
            addMazeAfterMove(playerA, currentx, currenty, PORTAL1_X, PORTAL1_Y, mazelist);
        }
        
        return mazelist;
    }
    
    private void addMazeAfterMove(boolean playerA, int currentx, int currenty, int movex, int movey, List<Maze> mazelist) {
        if(isValidMove(currentx,currenty,movex,movey)) {
            if(playerA) {
                int movepointvalue = pointvalue+value(maze[movex][movey]);
                char[][] movedmaze = copyMaze(maze);
                applyMove(currentx,currenty,movex,movey,movedmaze);
                mazelist.add(new Maze(movedmaze, movepointvalue, movex, movey, Bx, By));
            }
            else {
                int movepointvalue = pointvalue-value(maze[movex][movey]);
                char[][] movedmaze = copyMaze(maze);
                applyMove(currentx,currenty,movex,movey,movedmaze);
                mazelist.add(new Maze(movedmaze, movepointvalue, Ax, Ay, movex, movey));
            }
        }
    }
    
    public static void applyMove(int currentx, int currenty, int movex, int movey, char[][] editmaze) {
        if(editmaze[movex][movey]==PLAYER_A) {
            editmaze[CENTER_X][CENTER_Y] = PLAYER_A;
        }
        else if(editmaze[movex][movey]==PLAYER_B) {
            editmaze[CENTER_X][CENTER_Y] = PLAYER_B;
        }
        
        if(editmaze[movex][movey]==PILL_POISON) {
            editmaze[CENTER_X][CENTER_Y] = editmaze[currentx][currenty];
            editmaze[movex][movey] = EMPTY;
        }
        else {
            editmaze[movex][movey] = editmaze[currentx][currenty];
        }
        
        editmaze[currentx][currenty] = EMPTY;
    }
    
    public static int value(char point) {
        switch(point) {
            case PILL:
                return 1;
            case PILL_LARGE:
                return 15;
        }
        return 0;
    }
    
    private boolean isValidMove(int fromx, int fromy, int tox, int toy) {
        if(maze[tox][toy] == WALL) {
            return false;
        }
        if(tox==CENTER_X && toy==CENTER_Y) {
            return false;
        }
        else if(!isEnteringSpawn(tox,toy,fromx,fromy)) {
            return true;
        }
        return false;
    }
    
    private static boolean isEnteringSpawn(int tox, int toy, int fromx, int fromy) {
        if(fromx!=CENTER_X || fromy!=CENTER_Y) {
            if(tox<12 && tox>8 && toy>6 && toy<12) {
                return true;
            }
        }
        return false;
    }
    
    public static char[][] copyMaze(char[][] currentmaze) {
        char[][] newmaze = new char[currentmaze.length][];
        for(int i = 0; i < currentmaze.length; i++) {
            newmaze[i] = new char[currentmaze[i].length];
            System.arraycopy(currentmaze[i], 0, newmaze[i], 0, currentmaze[i].length);
        }
        return newmaze;
    }
}
