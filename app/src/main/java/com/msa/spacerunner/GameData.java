package com.msa.spacerunner;

import android.content.Context;
import android.util.Log;

import com.msa.spacerunner.collision.BoundingBox3D;
import com.msa.spacerunner.collision.Point3D;
import com.msa.spacerunner.engine.Blocks;
import com.msa.spacerunner.engine.GameActor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GameData {

    private static HashMap<String, GameRecord> allRecords;
    private static HashMap<String, GameBoard> allBoards;

    private static String[] lastBlock = null;
    private static GameActor lastPowerUpActor;

    private static GameBoard testLevel(int mapBlocks, GameBoard.GameDifficulty difficulty) {
        ArrayList<String[]> blocks = Blocks.getTestBlocks();
        GameBoard gameBoard = new GameBoard("Test ", difficulty);
        return parseLevel(gameBoard, blocks, mapBlocks);
    }

    private static GameBoard randomLevel(int levelNumber, int mapBlocks, GameBoard.GameDifficulty difficulty) {
        ArrayList<String[]> blocks = Blocks.getBlocks();
        String titleName = levelNumber < 10 ? "0" + levelNumber : "" + levelNumber;
        GameBoard gameBoard = new GameBoard("Level " + titleName, difficulty);
        return parseLevel(gameBoard, blocks, mapBlocks);
    }

    private static GameBoard parseLevel(GameBoard gameBoard, ArrayList<String[]> blocks, int mapBlocks) {
        int coinAmount = 0;
        float zPos = 0f;

        // Start
        for (int k = 0; k < 10; k++) {
            GameActor[] upper = new GameActor[GameBoard.BOARD_WIDTH];

            upper[0] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(0.0f, 0.0f, zPos));
            upper[1] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(1.0f, 0.0f, zPos));
            upper[2] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(2.0f, 0.0f, zPos));
            upper[3] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(3.0f, 0.0f, zPos));
            upper[4] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(4.0f, 0.0f, zPos));

            GameActor[] lower = new GameActor[GameBoard.BOARD_WIDTH];
            lower[0] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(0.0f, 0.0f, zPos));
            lower[1] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(1.0f, 0.0f, zPos));
            lower[2] = new GameActor(GameActor.ActorType.floor, BoundingBox3D.getBB3D(2.0f, 0.0f, zPos));
            lower[3] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(3.0f, 0.0f, zPos));
            lower[4] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(4.0f, 0.0f, zPos));

            gameBoard.addActorGroup(upper, lower);
            zPos++;
        }

        // Random
        for (int i = 0; i < mapBlocks; i++) {

            String[] lines = getRandomBlock(blocks);

            for (String s : lines) {
                GameActor[] upper = new GameActor[GameBoard.BOARD_WIDTH];
                GameActor[] lower = new GameActor[GameBoard.BOARD_WIDTH];

                char[] line = s.toCharArray();
                int cpt = 0;
                float xPos = -2.0f;

                for (char c : line) {
                    if (Character.toString(c).equals("#")) {
                        upper[cpt] = new GameActor(GameActor.ActorType.barrier, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                        lower[cpt] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));

                    } else if (Character.toString(c).equals("o")) {
                        upper[cpt] = new GameActor(GameActor.ActorType.coin, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                        lower[cpt] = new GameActor(GameActor.ActorType.floor, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                        coinAmount++;
                    } else if (Character.toString(c).equals("p")) {
                        upper[cpt] = getRandomPowerUpActor(BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                        lower[cpt] = new GameActor(GameActor.ActorType.floor, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                    } else if (Character.toString(c).equals("x")) {
                        upper[cpt] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                        lower[cpt] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                    } else if (Character.toString(c).equals(">")) { // right

                        upper[cpt] = new GameActor(GameActor.ActorType.move_right, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                        lower[cpt] = new GameActor(GameActor.ActorType.floor, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));

                    } else if (Character.toString(c).equals("<")) { // left

                        upper[cpt] = new GameActor(GameActor.ActorType.move_left, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                        lower[cpt] = new GameActor(GameActor.ActorType.floor, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));

                    } else if (Character.toString(c).equals("i")) { // up

                        upper[cpt] = new GameActor(GameActor.ActorType.move_up, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                        lower[cpt] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));

                    } else if (Character.toString(c).equals("v")) { // down

                        upper[cpt] = new GameActor(GameActor.ActorType.move_down, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                        lower[cpt] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));

                    } else {
                        upper[cpt] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                        lower[cpt] = new GameActor(GameActor.ActorType.floor, BoundingBox3D.getBB3D(xPos, 0.0f, zPos));
                    }
                    cpt++;
                    xPos++;
                }
                gameBoard.addActorGroup(upper, lower);
                zPos++;
            }

            // Jump
            if(addRandomJump()) {
                GameActor[] upper = new GameActor[GameBoard.BOARD_WIDTH];
                GameActor[] lower = new GameActor[GameBoard.BOARD_WIDTH];

                upper[0] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(0.0f, 0.0f, zPos));
                upper[1] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(1.0f, 0.0f, zPos));
                upper[2] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(2.0f, 0.0f, zPos));
                upper[3] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(3.0f, 0.0f, zPos));
                upper[4] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(4.0f, 0.0f, zPos));

                lower[0] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(0.0f, 0.0f, zPos));
                lower[1] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(1.0f, 0.0f, zPos));
                lower[2] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(2.0f, 0.0f, zPos));
                lower[3] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(3.0f, 0.0f, zPos));
                lower[4] = new GameActor(GameActor.ActorType.empty, BoundingBox3D.getBB3D(4.0f, 0.0f, zPos));

                gameBoard.addActorGroup(upper, lower);
                gameBoard.addActorGroup(upper, lower);
            }
        }

        gameBoard.setNumOfCoins(coinAmount);

        return gameBoard;
    }




    // 30% chance true
    private static boolean addRandomJump() {
        return (int) (Math.random() * 100) >= 70;
    }

    private static String[] getRandomBlock(ArrayList<String[]> blocks) {
        Collections.shuffle(blocks);
        if (lastBlock == null || lastBlock != blocks.get(0)) {
            lastBlock = blocks.get(0);
        } else {
            getRandomBlock(blocks);
        }

        return lastBlock;
    }

    public static void generateAllLevels() {
        allBoards = new HashMap<String, GameBoard>();
        int levelNumber = 1;
        int levelPerDifficulty = 3;

        for (int i = 0; i < levelPerDifficulty; i++) {
            GameBoard gameBoard = GameData.randomLevel(levelNumber,10, GameBoard.GameDifficulty.EASY);
            allBoards.put(gameBoard.boardName, gameBoard);
            levelNumber++;
        }

        for (int i = 0; i < levelPerDifficulty; i++) {
            GameBoard gameBoard = GameData.randomLevel(levelNumber,30, GameBoard.GameDifficulty.MODERATE);
            allBoards.put(gameBoard.boardName, gameBoard);
            levelNumber++;
        }

        for (int i = 0; i < levelPerDifficulty; i++) {
            GameBoard gameBoard = GameData.randomLevel(levelNumber,80, GameBoard.GameDifficulty.HARD);
            allBoards.put(gameBoard.boardName, gameBoard);
            levelNumber++;
        }

        for (int i = 0; i < levelPerDifficulty; i++) {
            GameBoard gameBoard = GameData.randomLevel(levelNumber,120, GameBoard.GameDifficulty.EXTREME);
            allBoards.put(gameBoard.boardName, gameBoard);
            levelNumber++;
        }

        //testLevel
        GameBoard gameBoard = GameData.testLevel(10, GameBoard.GameDifficulty.EXTREME);
        allBoards.put(gameBoard.boardName, gameBoard);


    }

    /**
     * Loads all levels into memory.
     *
     * @param context - Context object
     */
    public static void initialize(Context context) {
        allBoards = new HashMap<String, GameBoard>();
        /*String[] fileList = null;
        try {
            fileList = context.getAssets().list("levels");
        } catch (IOException e) {
            Log.i("getAllGames", "Loading levels failed: " + e.getMessage());
            e.printStackTrace();
            return;
        }*/

        // Load from assets predefined levels
        /*for (String fileName : fileList) {
            if (fileName.startsWith("level_")) { //Level files start with this prefix.
                GameBoard gameBoard = new GameBoard(context, fileName);
                allBoards.put(gameBoard.boardName, gameBoard);
            }
        }*/

        /*GameBoard gameBoard = GameData.buildGameManu(context);
        allBoards.put(gameBoard.boardName, gameBoard);*/

        /*int levelNumber = 1;
        int levelPerDifficulty = 3;

        for (int i = 0; i < levelPerDifficulty; i++) {
            GameBoard gameBoard = GameData.randomLevel(levelNumber,10, GameBoard.GameDifficulty.EASY);
            allBoards.put(gameBoard.boardName, gameBoard);
            levelNumber++;
        }

        for (int i = 0; i < levelPerDifficulty; i++) {
            GameBoard gameBoard = GameData.randomLevel(levelNumber,30, GameBoard.GameDifficulty.MODERATE);
            allBoards.put(gameBoard.boardName, gameBoard);
            levelNumber++;
        }

        for (int i = 0; i < levelPerDifficulty; i++) {
            GameBoard gameBoard = GameData.randomLevel(levelNumber,80, GameBoard.GameDifficulty.HARD);
            allBoards.put(gameBoard.boardName, gameBoard);
            levelNumber++;
        }

        for (int i = 0; i < levelPerDifficulty; i++) {
            GameBoard gameBoard = GameData.randomLevel(levelNumber,120, GameBoard.GameDifficulty.EXTREME);
            allBoards.put(gameBoard.boardName, gameBoard);
            levelNumber++;
        }*/




        //Load records for each level from file.
        /*allRecords = new HashMap<String, GameRecord>();
        try {
            File file = new File(context.getFilesDir(), "game_records.txt");
            FileReader textReader = new FileReader(file);
            BufferedReader bufferedTextReader = new BufferedReader(textReader);

            Gson gson = new Gson();
            String readLine = bufferedTextReader.readLine();
            //Reads data until we hit "end"
            while (!readLine.startsWith("end")) {
                GameRecord gameRecord = gson.fromJson(readLine, GameRecord.class);
                allRecords.put(gameRecord.getGameName(), gameRecord);
                readLine = bufferedTextReader.readLine();
            }

            bufferedTextReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //If records do not exist in file for a level, create a record with initial dummy values.
        /*for (String gameName : allBoards.keySet()) {
            if (allRecords.get(gameName) == null)
                allRecords.put(gameName, new GameRecord(gameName));
        }*/
    }

    /**
     * Writes all records to file
     *
     * @param context - Context object
     */
    /*public static void saveGameRecords(Context context) {
        Gson gson = new Gson();

        String jsonDataModel = "";

        for (String gameName : allRecords.keySet()) {
            jsonDataModel += gson.toJson(allRecords.get(gameName), GameRecord.class) + "\n";
        }
        jsonDataModel += "end\n";

        try {
            File file = new File(context.getFilesDir(), "game_records.txt");
            FileWriter textWriter = new FileWriter(file, false); //false flag overwrites old data
            BufferedWriter bufferedTextWriter = new BufferedWriter(textWriter);
            bufferedTextWriter.write(jsonDataModel);
            bufferedTextWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * Get the name of each game loaded from file
     *
     * @return - List of names
     */
    public static ArrayList<String> getGameNames() {
        if (allBoards == null) {
            Log.i("getGameBoard", "Attempt to access games before initialized");
            return null;
        }

        ArrayList<String> ret = new ArrayList<>(allBoards.keySet());
        Collections.sort(ret, String.CASE_INSENSITIVE_ORDER);
        return ret;
    }

    /**
     * Get a specific GameBoard by its name.
     *
     * @param gameName - name of the GameBoard
     * @return - GameBoard object
     */
    public static GameBoard getGameBoard(String gameName) {
        if (allBoards == null) {
            Log.i("getGameBoard", "Attempt to access games before initialized");
            return null;
        }

        return allBoards.get(gameName);
    }

    /**
     * Gets the records for a specific game, using its name
     *
     * @param gameName - name of the GameBoard
     * @return - GameRecord object.
     */
    public static GameRecord getGameRecord(String gameName) {
        if (allRecords == null) {
            Log.i("getGameRecord", "Attempt to access records before initialized");
            return null;
        }

        return allRecords.get(gameName);
    }


    private static GameActor getRandomPowerUpActor(BoundingBox3D bb3d) {
        int i = (int) (Math.random() * 100);
        GameActor actor;

        if(i>75) {
            actor = new GameActor(GameActor.ActorType.speed, bb3d);
        } else if (i>50){
            actor =  new GameActor(GameActor.ActorType.slow, bb3d);
        } else {
            actor =  new GameActor(GameActor.ActorType.empty, bb3d);
        }

        if(lastPowerUpActor == null || lastPowerUpActor != actor) {
            lastPowerUpActor = actor;
        } else {
            getRandomPowerUpActor(bb3d);
        }

        return actor;
    }
}
