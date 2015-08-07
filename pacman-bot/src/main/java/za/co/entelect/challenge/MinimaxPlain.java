package za.co.entelect.challenge;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MinimaxPlain {
    private static final int WIDTH = 19;
    private static final int HEIGHT = 22;
    private static final int PORTAL1_X = 10;
    private static final int PORTAL1_Y = 18;
    private static final int PORTAL2_X = 10;
    private static final int PORTAL2_Y = 0;
    private static final char WALL = '#';
    private static final char PLAYER_SYMBOL = 'A';
    private static final char ENEMY_SYMBOL = 'B';
    private static final String OUTPUT_FILE_NAME = "game.state";
    
    private static final String pre_file = "gameA.state";
    private static final String post_file = "gameB.state";
    
    private static final int CENTER_X = 10;
    private static final int CENTER_Y = 9;
    
    private static final int default_maxdepth = 6;
    
    public static void main(String[] args) {
        char[][] maze = ReadMaze(args[0]);
        Point aPoint = getCurrentPosition(maze);
        Point bPoint = getCurrentBPosition(maze);
        if(aPoint == null) {
            aPoint = new Point(CENTER_X, CENTER_Y);
        }
        if(bPoint == null) {
            bPoint = new Point(CENTER_X, CENTER_Y);
        }
        
        writeMaze(maze, pre_file);

        List<Point> possibleMoveList = determineMoves(aPoint, maze);

        int moveindex = 0;
        int maxscore = Integer.MIN_VALUE;
        
        String summary = "Values: \n";
        
        for(int i=0; i<possibleMoveList.size(); i++) {
            Point movep = possibleMoveList.get(i);
            int movepointvalue = Maze.value(maze[movep.x][movep.y]);
            char[][] movedmaze = Maze.copyMaze(maze);
            Maze.applyMove(aPoint.x, aPoint.y, movep.x, movep.y, movedmaze);
            Maze node = new Maze(movedmaze, movepointvalue, movep.x, movep.y, bPoint.x, bPoint.y);
            
            movepointvalue += minimax(node, 0, false);
            summary += "("+aPoint.x+", "+aPoint.y+") -> ("+movep.x+", "+movep.y+"):" + movepointvalue+"\n";
            
            if(movepointvalue>maxscore) {
                moveindex=i;
                maxscore=movepointvalue;
            }
        }
        
        try {
            PrintWriter writer = new PrintWriter("summaries\\"+System.currentTimeMillis()+".move.txt");
            writer.print(summary);
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        
        maze = makeMove(aPoint, possibleMoveList.get(moveindex), maze);
        writeMaze(maze, post_file);

        writeMaze(maze, OUTPUT_FILE_NAME);
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
    
    private static boolean isEnteringSpawn(Point p, Point from) {
        if(from.x!=CENTER_X || from.y!=CENTER_Y) {
            if(p.x<12 && p.x>8 && p.y>6 && p.y<12) {
                return true;
            }
        }
        return false;
    }
    
    private static int minimax(Maze node, int depth, boolean maximizingPlayer) {
        if (depth == default_maxdepth && maximizingPlayer) {
            //return mazeValue(currentmaze);
            return node.getPointValue();
        }
        if (maximizingPlayer) {
            int bestValue = Integer.MIN_VALUE;
            for (Maze child: node.getChildNodes(true)) {
                bestValue = Math.max(bestValue, minimax(child, depth + 1, false));
            }
            return bestValue;
        }
        else {
            int bestValue = Integer.MAX_VALUE;
            for (Maze child: node.getChildNodes(false)) {
                bestValue = Math.min(bestValue, minimax(child, depth, true));
            }
            return bestValue;
        }
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
    
    private static Point getCurrentBPosition(char[][] maze) {
        Point coordinate = new Point();
        for (int x = 0; x < HEIGHT; x++) {
            for (int y = 0; y < WIDTH; y++) {
                if (maze[x][y] == ENEMY_SYMBOL) {
                    coordinate.setLocation(x, y);
                    return coordinate;
                }
            }
        }
        return null;
    }
    
    private static char[][] makeMove(Point currentPoint, Point movePoint, char[][] maze) {
        
        // in switch/case update points scored plx
        
        if(currentPoint.x==10 && currentPoint.y==9) {
            if(maze[currentPoint.x][currentPoint.y]!='B') {
                maze[currentPoint.x][currentPoint.y] = ' ';
            }
        }
        else {
            maze[currentPoint.x][currentPoint.y] = ' ';
        }
        
        maze[movePoint.x][movePoint.y] = PLAYER_SYMBOL;
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
