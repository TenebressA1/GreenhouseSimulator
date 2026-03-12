package greenhousesimulator;

import greenhousesimulator.ui.GameWindow;
import java.util.*;
import java.util.stream.Collectors;

public class Greenhouse {
    private List<Plant> plants = new ArrayList<>();
    private int temperature = 22;
    private int humidity = 60;
    private int day = 1;
    private int money = 1000;
    private GameWindow gameWindow;
    
    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }
    
    private void log(String message) {
        if (gameWindow != null) {
            gameWindow.log(message);
        }
    }
    
    public void addPlant(Plant plant) {
        plants.add(plant);
        log("[ДОБАВЛЕНО] Новое растение: " + plant.getType());
    }
    
    public void removeDeadPlants() {
        List<Plant> trulyDeadPlants = plants.stream()
            .filter(p -> !p.isAlive() && !p.isReadyToHarvest())
            .collect(Collectors.toList());

        if (!trulyDeadPlants.isEmpty()) {
            plants.removeAll(trulyDeadPlants);
            log("[САНИТАРИЯ] Убрано " + trulyDeadPlants.size() + " погибших растений");
        }
    }
    
    public void waterAllPlants() {
        plants.stream()
            .filter(Plant::isAlive)
            .forEach(p -> p.water(30));
        log("[ПОЛИВ] Все растения политы!");
    }
    
    public void fertilizeAllPlants() {
        plants.stream()
            .filter(Plant::isAlive)
            .forEach(p -> p.fertilize(20));
        log("[УДОБРЕНИЕ] Все растения удобрены!");
    }
    
    public void updatePlants() {
        plants.forEach(Plant::grow);
        removeDeadPlants();
    }
    
    public List<Plant> getReadyToHarvest() {
        return plants.stream()
            .filter(Plant::isReadyToHarvest)
            .collect(Collectors.toList());
    }
    
    public int harvestAll() {
        List<Plant> readyPlants = new ArrayList<>();
        int totalIncome = 0;

        for (Plant plant : plants) {
            if (plant.isReadyToHarvest() && plant.isAlive()) {
                readyPlants.add(plant);
                totalIncome += plant.harvest();
            }
        }

        plants.removeAll(readyPlants);

        if (!readyPlants.isEmpty()) {
            log("[УРОЖАЙ] Собрано " + readyPlants.size() + " растений на " + totalIncome + " руб.!");
        }

        return totalIncome;
    }
    
    public void showStatus() {
        log("\n=== ДЕНЬ " + day + " ===");
        log("Температура: " + temperature + "°C");
        log("Влажность: " + humidity + "%");
        log("Деньги: " + money + " руб.");
        log("Растений: " + plants.size());
    }
    
    public List<Plant> getPlants() { return plants; }
    public int getPlantCount() { return plants.size(); }
    public int getTemperature() { return temperature; }
    public void setTemperature(int temperature) { this.temperature = temperature; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
    public void nextDay() { day++; }
    public int getMoney() { return money; }
    public void addMoney(int amount) { money += amount; }
    public void spendMoney(int amount) { money -= amount; }
}