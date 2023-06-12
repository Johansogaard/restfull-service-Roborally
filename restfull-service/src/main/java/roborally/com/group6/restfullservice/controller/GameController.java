package roborally.com.group6.restfullservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import roborally.com.group6.restfullservice.model.Game;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class GameController {
    ArrayList<Game> games = new ArrayList<>();
    int currId = 1000;
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
    public String getId(@PathVariable final String maxPlayers){
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

