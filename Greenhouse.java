package greenhousesimulator;

import java.util.*;
import java.util.stream.Collectors;

public class Greenhouse {
    private List<Plant> plants = new ArrayList<>();
    private int temperature = 22;
    private int humidity = 60;
    private boolean lightsOn = true;
    private int day = 1;
    private int money = 1000;
    
    public void addPlant(Plant plant) {
        plants.add(plant);
        System.out.println("[ПОСАДКА] Новое растение: " + plant.getType());
    }
    
    public void removeDeadPlants() {
        List<Plant> trulyDeadPlants = plants.stream()
            .filter(p -> !p.isAlive() && !p.isReadyToHarvest())
            .collect(Collectors.toList());

        if (!trulyDeadPlants.isEmpty()) {
            plants.removeAll(trulyDeadPlants);
            System.out.println("[УБОРКА] Убрано " + trulyDeadPlants.size() + " погибших растений");
        }
    }
    
    public void waterAllPlants() {
        plants.stream()
            .filter(Plant::isAlive)
            .forEach(p -> p.water(30));
        System.out.println("[ПОЛИВ] Все растения политы!");
    }
    
    public void fertilizeAllPlants() {
        plants.stream()
            .filter(Plant::isAlive)
            .forEach(p -> p.fertilize(20));
        System.out.println("[УДОБРЕНИЕ] Все растения удобрены!");
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
            System.out.println("[УРОЖАЙ] Собрано " + readyPlants.size() + " растений на " + totalIncome + " руб.!");
        }

        return totalIncome;
    }
    
    public void showStatus() {
        System.out.println("\n=== День " + day + " ===");
        System.out.println("Температура: " + temperature + "°C");
        System.out.println("Влажность: " + humidity + "%");
        System.out.println("Освещение: " + (lightsOn ? "ВКЛ" : "ВЫКЛ"));
        System.out.println("Деньги: " + money + " руб.");
        System.out.println("Растений: " + plants.size());
        
        if (!plants.isEmpty()) {
            // Stream API для статистики по здоровью
            long healthyPlants = plants.stream()
                .filter(p -> p.isAlive() && p.getHealth() >= 70)
                .count();
            long weakPlants = plants.stream()
                .filter(p -> p.isAlive() && p.getHealth() < 70 && p.getHealth() >= 30)
                .count();
            long dyingPlants = plants.stream()
                .filter(p -> p.isAlive() && p.getHealth() < 30)
                .count();
            
            System.out.println("\n=== СТАТУС РАСТЕНИЙ ===");
            System.out.println("Здоровые: " + healthyPlants);
            System.out.println("Слабые: " + weakPlants);
            System.out.println("Умирающие: " + dyingPlants);
            
            // Stream API для поиска проблем
            long needWater = plants.stream()
                .filter(Plant::needsWater)
                .count();
            long needFertilizer = plants.stream()
                .filter(Plant::needsFertilizer)
                .count();
            
            if (needWater > 0) {
                System.out.println("\n[ВНИМАНИЕ] Требуют полива: " + needWater + " растений");
            }
            if (needFertilizer > 0) {
                System.out.println("[ВНИМАНИЕ] Требуют удобрения: " + needFertilizer + " растений");
            }
            
            List<Plant> criticalPlants = plants.stream()
                .filter(p -> p.isAlive() && (p.getWaterLevel() == 0 || p.isDying()))
                .limit(3)
                .collect(Collectors.toList());
            
            if (!criticalPlants.isEmpty()) {
                System.out.println("\n[КРИТИЧЕСКОЕ] Растения в опасности:");
                criticalPlants.forEach(System.out::println);
            }
        } else {
            System.out.println("\nТеплица пуста. Посадите растения!");
        }
    }
    
    public List<Plant> getPlants() { return plants; }
    public int getPlantCount() { return plants.size(); }
    public int getTemperature() { return temperature; }
    public void setTemperature(int temperature) { this.temperature = temperature; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public boolean isLightsOn() { return lightsOn; }
    public void setLightsOn(boolean lightsOn) { this.lightsOn = lightsOn; }
    public int getDay() { return day; }
    public void nextDay() { day++; }
    public int getMoney() { return money; }
    public void addMoney(int amount) { money += amount; }
    public void spendMoney(int amount) { money -= amount; }
}