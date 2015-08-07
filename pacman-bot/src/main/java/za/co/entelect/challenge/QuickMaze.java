package za.co.entelect.challenge;

import java.util.ArrayList;
import java.util.List;

public class QuickMaze implements Comparable<QuickMaze> {
    private static final byte WIDTH = 19;
    private static final byte HEIGHT = 22;
    private static final byte PORTAL1_X = 10;
    private static final byte PORTAL1_Y = 18;
    private static final byte PORTAL2_X = 10;
    private static final byte PORTAL2_Y = 0;
    
    private static final byte CENTER_X = 10;
    private static final byte CENTER_Y = 9;
    
    private static final byte EMPTY = 0;            // ' '
    private static final byte WALL = 1;             //  #
    private static final byte PILL = 2;             //  .
    private static final byte PILL_LARGE = 3;       //  *
    private static final byte PILL_POISON = 4;      //  !
    private static final byte PLAYER_A = 5;         //  A
    private static final byte PLAYER_B = 6;         //  B
    
    public int Ax;
    public int Ay;
    private int Bx;
    private int By;
    
    private int pointvalue;
    
    private byte[][] maze;
    
    // maybe have an extra row on byte array to store ax,ay,bx,by,pointval
    // test speeds & complexity
    
    public QuickMaze(byte[][] basemaze, int pointval, int ax, int ay, int bx, int by) {
        maze = basemaze;
        pointvalue = pointval;
        Ax=ax;
        Ay=ay;
        Bx=bx;
        By=by;
    }
    
    public byte[][] getBaseMaze() {
        return maze;
    }
    
    public int getPointValue() {
        return pointvalue;
    }
    
    public List<QuickMaze> getChildNodes(boolean playerA) {
        List<QuickMaze> mazelist = new ArrayList<QuickMaze>();
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
    
    private void addMazeAfterMove(boolean playerA, int currentx, int currenty, int movex, int movey, List<QuickMaze> mazelist) {
        if(isValidMove(currentx,currenty,movex,movey)) {
            if(playerA) {
                int movepointvalue = pointvalue+value(maze[movex][movey]);
                byte[][] movedmaze = copyMaze(maze);
                applyMove(currentx,currenty,movex,movey,movedmaze);
                mazelist.add(new QuickMaze(movedmaze, movepointvalue, movex, movey, Bx, By));
            }
            else {
                int movepointvalue = pointvalue-value(maze[movex][movey]);
                byte[][] movedmaze = copyMaze(maze);
                applyMove(currentx,currenty,movex,movey,movedmaze);
                mazelist.add(new QuickMaze(movedmaze, movepointvalue, Ax, Ay, movex, movey));
            }
        }
    }
    
    public static void applyMove(int currentx, int currenty, int movex, int movey, byte[][] editmaze) {
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
    
    public static int value(byte point) {
        switch(point) {
            case PILL:
                return 1;
            case PILL_LARGE:
                return 15;
            case EMPTY:
                return 0;
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
    
    public static byte[][] copyMaze(byte[][] currentmaze) {
        byte[][] newmaze = new byte[currentmaze.length][];
        for(int i = 0; i < currentmaze.length; i++) {
            newmaze[i] = new byte[currentmaze[i].length];
            System.arraycopy(currentmaze[i], 0, newmaze[i], 0, currentmaze[i].length);
        }
        return newmaze;
    }
    
    public static byte[][] createByteMaze(char[][] charmaze) {
        byte[][] bytemaze = new byte[charmaze.length][];
        for(int i=0; i< charmaze.length; i++) {
            bytemaze[i] = new byte[charmaze[i].length];
            for(int j=0; j< charmaze[i].length; j++) {
                switch(charmaze[i][j]) {
                    case '#':
                        bytemaze[i][j]=1;
                        break;
                    case '.':
                        bytemaze[i][j]=2;
                        break;
                    case '*':
                        bytemaze[i][j]=3;
                        break;
                    case '!':
                        bytemaze[i][j]=4;
                        break;
                    case 'A':
                        bytemaze[i][j]=5;
                        break;
                    case 'B':
                        bytemaze[i][j]=6;
                        break;
                    default:
                        bytemaze[i][j]=0;
                }
            }
        }
        return bytemaze;
    }
    
    public static char[][] createCharMaze(byte[][] bymaze) {
        char[][] chmaze = new char[bymaze.length][];
        for(int i=0; i< bymaze.length; i++) {
            chmaze[i] = new char[bymaze[i].length];
            for(int j=0; j< bymaze[i].length; j++) {
                switch(bymaze[i][j]) {
                    case 1:
                        chmaze[i][j]='#';
                        break;
                    case 2:
                        chmaze[i][j]='.';
                        break;
                    case 3:
                        chmaze[i][j]='*';
                        break;
                    case 4:
                        chmaze[i][j]='!';
                        break;
                    case 5:
                        chmaze[i][j]='A';
                        break;
                    case 6:
                        chmaze[i][j]='B';
                        break;
                    default:
                        chmaze[i][j]=0;
                }
            }
        }
        return chmaze;
    }

    public boolean isTerminal() {
        for(int i=1;i<HEIGHT;i++) {
            for(int j=1;j<WIDTH;j++) {
                if(maze[i][j]==PILL || maze[i][j]==PILL_LARGE) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public int compareTo(QuickMaze o) {
        return (getPointValue()>o.getPointValue() ? -1 : (getPointValue()==o.getPointValue() ? 0 : 1));
    }
}
