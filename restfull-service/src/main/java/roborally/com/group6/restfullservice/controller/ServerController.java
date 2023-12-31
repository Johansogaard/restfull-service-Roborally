package roborally.com.group6.restfullservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import roborally.com.group6.restfullservice.model.Game;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

@RestController
public class ServerController {
    ArrayList<Game> games = new ArrayList<>();
    private final String gameSavePath ="src/main/resources/gamesavesonline";
    int currId = 1000;
    @PostMapping("/game/savegame/{gamename}")
    public void saveGame(@PathVariable String gamename,  @RequestBody String gameInstance) {

        saveStringToFile(gameInstance,gamename+".json");

    }

    public ResponseEntity<Object> saveStringToFile(String content, String fileName) {

        try {
            // Create the folder if it doesn't exist
            File folder = new File(gameSavePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Write the game data to a file
            String filePath = gameSavePath + "/" + fileName;
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(content);
            fileWriter.close();

            return ResponseEntity.status(HttpStatus.CREATED).build(); // Return HTTP 201 Created for successful save
        } catch (IOException e) {
            // Handle any error that occurs during file writing
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Return HTTP 500 Internal Server Error for error during save
        }
    }
    @GetMapping("/game/files/{name}")
    public String getFileFromName(@PathVariable String name)
    {
        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(gameSavePath+"/"+name));
            return new String(fileBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Handle any potential IO exceptions here
            e.printStackTrace();
        }
        return null;
    }
    @GetMapping("/game/files")
    public String getFileNames()
    {
        String files="";
        File folder = new File(gameSavePath);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
               files = files+file.getName()+",";
            }
        }
        return files;
    }
    @GetMapping("/game/{id}/statpost/{player}")
    public String getGameStatusPost(@PathVariable final String id,@PathVariable final String player) {
        //der er noget galt her med status
        Game exists = gameExist(id);
        if (exists != null) {
            if (exists.isReadyProgramming(player) == true) {
                return "Ready";
            } else {
                return "not";
            }
        }
        return "game dosent exist";
    }
    @GetMapping("/game/{id}/statact/{player}")
    public String getGameStatusAct(@PathVariable final String id,@PathVariable final String player) {
        //der er noget galt her med status
        Game exists = gameExist(id);
        if (exists != null) {
            if (exists.isReadyActivation(player) == true) {
                return "Ready";
            } else {
                return "not";
            }
        }
        return "game dosent exist";
    }
    @GetMapping("/game/{id}/players")
    public String getGameStatusAct(@PathVariable final String id) {
        //der er noget galt her med status
        Game exists = gameExist(id);
        if (exists != null) {
            String toReturn = exists.players+","+exists.start;
           return toReturn;
        }
        return null;

    }
    @PostMapping("/game/{id}/start")
    //spring outomatecly bindes our parameters aoutomatecly to return value mayby
    public void postStart(@PathVariable final String id,@RequestBody String start) {
        Game exists = gameExist(id);
        if (exists != null&&start.equals("true")) {
            exists.start=true;
        }
        else if (exists != null&&start.equals("false"))
        {
            exists.start =false;
        }
    }
    @PostMapping("/game/{id}")
    //spring outomatecly bindes our parameters aoutomatecly to return value mayby
    public void postGameInstanceStartPhase(@PathVariable final String id,@RequestBody String gameInstance)
    {
       Game exists = gameExist(id);
        if (exists == null) {

        }
        else
        {
            exists.setJsonFile(gameInstance);
        }
    }
    @PostMapping("/game/{id}/step/{player}")
    //spring outomatecly bindes our parameters aoutomatecly to return value mayby
    public void postGameInstanceExcecuteStep(@PathVariable final String id,@PathVariable final  String player, @RequestBody String gameInstance)
    {

        Thread thread = new Thread(() -> {
            // Lengthy operation
            executeStepIfAllHaveRecivedStatus(id,player,gameInstance);
        });
        thread.start();

    }
    public void executeStepIfAllHaveRecivedStatus(String id, String player, String gameInstance)
    {

        Game exists = gameExist(id);
        if (exists!= null)
        {
            while (true)
            {
                if (exists.lastPlayers.size() == exists.maxPlayers||exists.lastPlayers.size()==0)
                {
                    exists.resetLastPlayers();
                    break;
                }
            }
            System.out.println("Recieved board from "+player);

            exists.addLastPlayers(player);
            exists.readyAct=true;
            exists.setJsonFile(gameInstance);
        }
    }
    @PostMapping("/game/{id}/prog/{player}")
    //spring outomatecly bindes our parameters aoutomatecly to return value mayby
    public void postGameInstanceProgrammingFase(@PathVariable final String id,@PathVariable final String player,@RequestBody String gameInstance)
    {
        Game exists = gameExist(id);
        if (exists == null) {

        }
        else
        {
            exists.resetLastPlayers();
            exists.setPlayersPosted(Integer.parseInt(player));
            exists.setJsonFile(gameInstance);
        }
    }
    @GetMapping("/game/{id}")
    public String getGameInstance(@PathVariable final String id)
    {
        Game exists = gameExist(id);

        if (exists != null) {
            return exists.getJsonFile();
        }
        else
        {
            return "Game dosent exist";
        }


    }

    @GetMapping("/game/{id}/join")
    public String getGamePlayerNumb(@PathVariable final String id)
    {
        Game exists = gameExist(id);

        if (exists!=null) {
            int players = exists.getPlayers();
            if (players + 1 > exists.maxPlayers) {
                return "Game is full";
            } else {
                exists.setPlayers(players + 1);
                return Integer.toString(players + 1);
            }

        }
        else
        {
            return "Game dosent exist";
        }



    }
    @GetMapping("/game/id/{maxPlayers}")
    public String createGame(@PathVariable final String maxPlayers){
       int id =currId;
       currId = currId+1;
        System.out.println(id);
        System.out.println(maxPlayers);
        Game newgame = new Game(Integer.toString(id),Integer.parseInt(maxPlayers));
        games.add(newgame);
       return Integer.toString(id);
    }



    public String getGameFromID(String Id)
    {
        for (Game game : games)
        {
            if(Id.equals( game.getId()))
            {
                return game.getJsonFile();
            }
        }
        return null;
    }
    public Game gameExist(String id)
    {
        Game exists = null;
        for (Game game : games)
        {
            if (game.getId().equals(id))
            {
                exists =game;
                break;
            }
        }
        return exists;
    }
}

