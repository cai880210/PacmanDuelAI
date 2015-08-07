package za.co.entelect.challenge;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Util {
    private static final char PLAYER_SYMBOL = 'A';
    private static final char ENEMY_SYMBOL = 'B';
    private static final int WIDTH = 19;
    private static final int HEIGHT = 22;
    private static final int PORTAL1_X = 10;
    private static final int PORTAL1_Y = 18;
    private static final int PORTAL2_X = 10;
    private static final int PORTAL2_Y = 0;
    private static final char WALL = '#';
    private static final int CENTER_X = 10;
    private static final int CENTER_Y = 9;
    
    public static Point getCurrentPosition(char[][] maze) {
        Point coordinate = new Point();
        for (int x = 0; x < HEIGHT; x++) {
            for (int y = 0; y < WIDTH; y++) {
                if (maze[x][y] == PLAYER_SYMBOL) {
                    coordinate.setLocation(x, y);
                    return coordinate;
                }
            }
        }
        return new Point(CENTER_X, CENTER_Y);
    }
    
    public static Point getCurrentBPosition(char[][] maze) {
        Point coordinate = new Point();
        for (int x = 0; x < HEIGHT; x++) {
            for (int y = 0; y < WIDTH; y++) {
                if (maze[x][y] == ENEMY_SYMBOL) {
                    coordinate.setLocation(x, y);
                    return coordinate;
                }
            }
        }
        return new Point(CENTER_X, CENTER_Y);
    }
    
    public static char[][] makeMove(Point currentPoint, Point movePoint, char[][] maze) {
        if(currentPoint.x==10 && currentPoint.y==9) {
            if(maze[currentPoint.x][currentPoint.y]!=ENEMY_SYMBOL) {
                maze[currentPoint.x][currentPoint.y] = ' ';
            }
        }
        else {
            maze[currentPoint.x][currentPoint.y] = ' ';
        }
        
        maze[movePoint.x][movePoint.y] = PLAYER_SYMBOL;
        return maze;
    }

    public static void writeMaze(char[][] maze, String filePath) {
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

    public static char[][] ReadMaze(String filePath) {
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
    
    public static List<Point> determineMoves(Point currentPoint, char[][] maze) {
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
}
