package za.co.entelect.challenge;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DepthLimitedAttackSuicideAvoid {
    private static final int WIDTH = 19;
    private static final int HEIGHT = 22;
    private static final int PORTAL1_X = 10;
    private static final int PORTAL1_Y = 18;
    private static final int PORTAL2_X = 10;
    private static final int PORTAL2_Y = 0;
    private static final char WALL = '#';
    private static final char PLAYER_SYMBOL = 'A';
    private static final String OUTPUT_FILE_NAME = "game.state";
    
    private static final String pre_file = "gameA.state";
    private static final String post_file = "gameB.state";
    
    private static final int CENTER_X = 10;
    private static final int CENTER_Y = 9;
    
    private static final int default_maxdepth = 35;
    private static final int default_shortdepth = 5;
    private static final boolean VALUE_PILLS = true;
    private static boolean VALUE_SUICIDES = true;
    private static final boolean VALUE_ATTACKS = true;
    
    
    public static void main(String[] args) {
        char[][] maze = ReadMaze(args[0]);
        
        Point coordinate = getCurrentPosition(maze);
        if(coordinate == null) {
            coordinate = new Point(CENTER_X, CENTER_Y);
        }
        if (coordinate != null) {
            writeMaze(maze, pre_file);
            
            List<Point> possibleMoveList = determineMoves(coordinate, maze);
            
            int moveindex = maxValue(coordinate, possibleMoveList, maze);
            
            maze = makeMove(coordinate, possibleMoveList.get(moveindex), maze);
            writeMaze(maze, post_file);
            
            writeMaze(maze, OUTPUT_FILE_NAME);
        }
    }
    
    private static int getProximityFactor(int playerAx, int playerAy, int playerBx, int playerBy, Point Objective) {
        // neeed a proper distance method
        // use A* search plx
        // possibly cached or hardcoded A* results
        // there are only so many open coordinates
        int adist = Math.abs(playerAx-Objective.x)+Math.abs(playerAy-Objective.y);
        int bdist = Math.abs(playerBx-Objective.x)+Math.abs(playerBy-Objective.y);
        int distdiff = bdist-adist;
        
        /*if(distdiff<1) {
            return -1;
        }
        return 0;*/
        
        if(distdiff>-1) {
            return 1;
        }
        
        return (distdiff-1)/2;
    }
    
    private static boolean isEnteringSpawn(Point p, Point from) {
        if(from.x!=CENTER_X || from.y!=CENTER_Y) {
            if(p.x<12 && p.x>8 && p.y>6 && p.y<12) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isSuicidalPoint(Point p, char[][] maze) {
        if(p.x==PORTAL1_X && p.y==PORTAL1_Y) {
            if(maze[PORTAL2_X][PORTAL2_Y]=='B') {
                return true;
            }
        }
        if(p.x==PORTAL2_X && p.y==PORTAL2_Y) {
            if(maze[PORTAL1_X][PORTAL1_Y]=='B') {
                return true;
            }
        }
        if(p.x>0) {
            if(maze[p.x-1][p.y]=='B') {
                return true;
            }
        }
        if(p.x<(HEIGHT-1)) {
            if(maze[p.x+1][p.y]=='B') {
                return true;
            }
        }
        if(p.y>0) {
            if(maze[p.y][p.y-1]=='B') {
                return true;
            }
        }
        if(p.y<(WIDTH-1)) {
            if(maze[p.y][p.y+1]=='B') {
                return true;
            }
        }
        return false;
    }
    
    private static char[][] copyMaze(char[][] maze) {
        char[][] newmaze = new char[maze.length][];
        for(int i = 0; i < maze.length; i++) {
            newmaze[i] = new char[maze[i].length];
            System.arraycopy(maze[i], 0, newmaze[i], 0, maze[i].length);
        }
        return newmaze;
    }
    
    // return index of move with maximum value;
    private static int maxValue(Point currentPoint, List<Point> movelist, char[][] maze) {
        int maxval = Integer.MIN_VALUE;
        int maxind = 0;
        
        for(int i=0; i<movelist.size(); i++) {
            Point movep = movelist.get(i);
            int moveval = valueOf(movep,currentPoint,0,default_maxdepth,maze);
            if(moveval>maxval) {
                maxval = moveval;
                maxind = i;
            }
            else if(moveval==maxval) {
                // compare on a smaller depth
                int shortmoveval = valueOf(movep,currentPoint,0,default_shortdepth,maze);
                int shortmaxval = valueOf(movelist.get(maxind),currentPoint,0,default_shortdepth,maze);
                if(shortmoveval>shortmaxval) {
                    maxind = i;
                }
                else if(shortmoveval==maxval) {
                    // if they are still equal then compare on immediate depth
                    int shortnew = value(movep, maze,0,0);
                    int shortmax = value(movelist.get(maxind), maze,0,0);
                    if(shortnew>shortmax) {
                        maxind=i;
                    }
                }
            }
        }
        return maxind;
    }
    
    
    public static int valueOf(Point currentPoint, Point oldPoint, int depth, int maxdepth, char[][] oldmaze) {
        char[][] maze = copyMaze(oldmaze);
        if(oldPoint!=null) {
            maze = makeMove(oldPoint, currentPoint, maze);
        }
        
        int currentvalue = value(currentPoint,oldmaze,depth,maxdepth);
        if(currentvalue==Integer.MIN_VALUE) {
            return 0;
        }
        
        if(depth==maxdepth) {
            return currentvalue;
        }
        
        int max = Integer.MIN_VALUE;
        
        for(Point movep: determineMoves(currentPoint, maze)) {
            // pill portal logic
            if(VALUE_PILLS && maze[movep.x][movep.y]=='!') {
            // if movep is a pill then valueOf(CENTER, currentPoint, depth+1, maxdepth, maze);
                int val = valueOf(new Point(10,9), currentPoint, depth+1, maxdepth, maze);
                if(val> max) {
                    max=val;
                }
            }
            // attack logic
            else if(VALUE_ATTACKS && depth==0 && maze[movep.x][movep.y]=='B') {
            //if movep is a player then valueOf(movep, currentPoint, depth+1, maxdepth, MAZE WITH ENEMY PLAYER IN CENTER);
                char[][] mazeeat = copyMaze(maze);
                mazeeat[movep.x][movep.y]=' ';
                mazeeat[CENTER_X][CENTER_Y]='B';
                int val = valueOf(movep, currentPoint, depth+1, maxdepth, mazeeat);
                if(val> max) {
                    max=val;
                }
            }
            else {
                // suicide logic
                if(VALUE_SUICIDES && depth==0 && isSuicidalPoint(movep,maze)) {
                /*if movep is a going to get your ass eaten then 
                    int val = valueOf(CENTER, currentPoint, depth+1, maxdepth, maze);*/
                    char[][] mazesuicide = copyMaze(maze);
                    //mazesuicide[movep.x][movep.y]=' ';
                        // We SHOULD move player B as well but that requires finding his position and setting it to ' '
                        // not sure if that is significant enough to justify the resource-rape
                    mazesuicide[CENTER_X][CENTER_Y]='A';
                    int val = value(movep,maze,depth+1,maxdepth)+valueOf(new Point(10,9), currentPoint, depth+2, maxdepth, mazesuicide);
                    // note: it is important to increase depth by 2 here                           ----------^
                    // so that this path is not given an extra move because we are adding the direct value of the movepoint value(movep,maze,depth+1,maxdepth)
                    if(val> max) {
                        max=val;
                    }
                }
                else {
                    if(!oldPoint.equals(movep)) {
                        int val = valueOf(movep, currentPoint, depth+1, maxdepth, maze);
                        if(val> max) {
                            max=val;
                        }
                    }
                    else {
                        // negative value discourage backtracking
                        // weighting this could be difficult without detecting patterns
                        int val = -10;
                        if(depth==0) {
                            val-=80;
                        }
                        if(val>max) {
                            max=val;
                        }
                    }
                }
            }
        }
        
        // depth weighting is done in the value method
        // depth weighting is to value closer pills more
        //max += (maxdepth-depth+1)*currentvalue;
        max += currentvalue;
        
        return max;
    }
    
    private static int value(Point currentPoint, char[][] maze, int depth, int maxdepth) {
        int x = currentPoint.x;
        int y = currentPoint.y;
        if(x<0 || y<0 || y>= WIDTH || x>= HEIGHT) {
            return Integer.MIN_VALUE;
        }
        int wongfactor;
        int aposX=-1;
        int aposY=-1;
        int bposX=-1;
        int bposY=-1;
        
        for(int ix=0; ix<HEIGHT; ix++) {
            for(int iy=0; iy<WIDTH; iy++) {
                if(maze[ix][iy]=='A') {
                    aposX=ix;
                    aposY=iy;
                }
                else if(maze[ix][iy]=='B') {
                    bposX=ix;
                    bposY=iy;
                }
            }
        }
        
        if(aposX==-1) {
            aposX=CENTER_X;
            aposY=CENTER_Y;
        }
        if(bposX==-1) {
            bposX=CENTER_X;
            bposY=CENTER_Y;
        }
        
        wongfactor = getProximityFactor(aposX, aposY, bposX, bposY, currentPoint);
        
        if(maze[x][y]=='.') {
            //return (10+wongfactor*8)*(maxdepth-depth+1);
            return (10)*wongfactor*(maxdepth-depth+1);
        }
        if(maze[x][y]=='*') {
            //return (150+wongfactor*120)*(maxdepth-depth+1);
            return (150)*wongfactor*(maxdepth-depth+1);
        }
        if(maze[x][y]==' ' ) {
            return 0;
        }
        
        /*if(maze[x][y]=='B' ) {
            //return -1000*(depth+1);           AVOID PLAYER
            //return 1000*(maxdepth-depth+1);   EAT PLAYER
        }*/
        if(maze[x][y]=='B' ) {
            return 0;
        }
        if(maze[x][y]=='!' ) {
            return 0;
        }
        
        return Integer.MIN_VALUE;
    }
    
    private static Point getCurrentPosition(char[][] maze) {
        Point coordinate = new Point();
        for (int x = 0; x < HEIGHT; x++) {
            for (int y = 0; y < WIDTH; y++) {
                if (maze[x][y] == PLAYER_SYMBOL) {
                    coordinate.setLocation(x, y);
                    return coordinate;
                }
            }
        }
        return null;
    }

    private static List<Point> determineMoves(Point currentPoint, char[][] maze) {
        List<Point> moveList = new ArrayList<Point>();
        if (currentPoint.y + 1 < WIDTH) {
            if (maze[currentPoint.x][currentPoint.y + 1] != WALL) {
                Point p = new Point(currentPoint.x, currentPoint.y + 1);
                if(isValidMove(currentPoint, p, maze)) {
                    moveList.add(p);
                }
            }
        }

        if (currentPoint.y - 1 >= 0) {
            if (maze[currentPoint.x][currentPoint.y - 1] != WALL) {
                Point p = new Point(currentPoint.x, currentPoint.y - 1);
                if(isValidMove(currentPoint, p, maze)) {
                    moveList.add(p);
                }
            }
        }

        if (currentPoint.x + 1 < HEIGHT) {
            if (maze[currentPoint.x + 1][currentPoint.y] != WALL) {
                Point p = new Point(currentPoint.x + 1, currentPoint.y);
                if(isValidMove(currentPoint, p, maze)) {
                    moveList.add(p);
                }
            }
        }

        if (currentPoint.x - 1 >= 0) {
            if (maze[currentPoint.x - 1][currentPoint.y] != WALL) {
                Point p = new Point(currentPoint.x - 1, currentPoint.y);
                if(isValidMove(currentPoint, p, maze)) {
                    moveList.add(p);
                }
            }
        }

        if (currentPoint.x == PORTAL1_X && currentPoint.y == PORTAL1_Y) {
            moveList.add(new Point(PORTAL2_X, PORTAL2_Y));
        }

        if (currentPoint.x == PORTAL2_X && currentPoint.y == PORTAL2_Y) {
            moveList.add(new Point(PORTAL1_X, PORTAL1_Y));
        }

        return moveList;
    }
    
    private static boolean isValidMove(Point from, Point to, char[][] maze) {
        if(maze[to.x][to.y] == WALL) {
            return false;
        }
        else if(!isEnteringSpawn(to,from)) {
            return true;
        }
        /*else if(maze[to.x][to.y] == '!') {
            to.x=10;
            to.y=9;
            return true;
        }*/
        return false;
    }

    private static char[][] makeMove(Point currentPoint, Point movePoint, char[][] maze) {
        
        // in switch/case update points scored plx
        
        if(currentPoint.x==10 && currentPoint.y==9) {
            if(maze[currentPoint.x][currentPoint.y]!='B') {
                //maze[currentPoint.x][currentPoint.y] = ' ';
                maze[currentPoint.x][currentPoint.y] = ' ';
            }
        }
        else {
            //maze[currentPoint.x][currentPoint.y] = ' ';
            maze[currentPoint.x][currentPoint.y] = ' ';
        }
        
        maze[movePoint.x][movePoint.y] = PLAYER_SYMBOL;
        //maze[(int) movePoint.getX()][(int) movePoint.getY()] = PLAYER_SYMBOL;
        return maze;
    }

    private static void writeMaze(char[][] maze, String filePath) {
        try {
            String output = "";
            for (int x = 0; x < HEIGHT; x++) {
                for (int y = 0; y < WIDTH; y++) {
                    output += maze[x][y];
                }
                if (x != HEIGHT - 1) {
                    output += ('\n');
                }
            }
            PrintWriter writer = new PrintWriter(filePath);
            writer.print(output);
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static char[][] ReadMaze(String filePath) {
        char[][] map = new char[HEIGHT][];
        try {
            Scanner reader = new Scanner(new File(filePath));
            int rowCount = 0;
            while (reader.hasNext()) {
                String row = reader.nextLine();
                map[rowCount] = row.toCharArray();
                rowCount++;
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return map;
    }
}
