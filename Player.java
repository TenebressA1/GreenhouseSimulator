package greenhousesimulator;

import java.util.*;
import java.util.stream.Collectors;

public class Player {
    private String name;
    private int experience;
    private List<String> inventory;
    private Random random = new Random();
    
    public Player(String name) {
        this.name = name;
        this.experience = 0;
        this.inventory = new ArrayList<>();
        inventory.add("Семена_Помидор");
        inventory.add("Семена_Помидор");
        inventory.add("Семена_Огурец");
        inventory.add("Удобрение");
        inventory.add("Удобрение");
    }
    
    public boolean buyItem(String item, int cost, Greenhouse greenhouse) {
        if (greenhouse.getMoney() >= cost) {
            greenhouse.spendMoney(cost);
            inventory.add(item);
            System.out.println("[ПОКУПКА] Куплено: " + item + " за " + cost + " руб.");
            return true;
        }
        System.out.println("[ОШИБКА] Недостаточно денег!");
        return false;
    }
    
    public boolean plantSeed(String plantType, Greenhouse greenhouse) {
        String seedItem = "Семена_" + plantType;
        
        boolean hasSeeds = inventory.stream()
            .anyMatch(item -> item.equals(seedItem));
        
        if (hasSeeds) {
            inventory.remove(seedItem);
            greenhouse.addPlant(new Plant(plantType));
            addExperience(10);
            System.out.println("[ПОСАДКА] Посажено растение: " + plantType);
            return true;
        }
        
        System.out.println("[ОШИБКА] Нет семян " + plantType.toLowerCase() + "а!");
        return false;
    }
    
    public boolean hasSeeds(String plantType) {
        String seedItem = "Семена_" + plantType;
        return inventory.stream()
            .anyMatch(item -> item.equals(seedItem));
    }
    
    public void useFertilizer(Greenhouse greenhouse) {
        boolean hasFertilizer = inventory.stream()
            .anyMatch(item -> item.equals("Удобрение"));
        
        if (hasFertilizer) {
            inventory.remove("Удобрение");
            greenhouse.fertilizeAllPlants();
            System.out.println("[УДОБРЕНИЕ] Использовано удобрение");
        } else {
            System.out.println("[ОШИБКА] Нет удобрений!");
        }
    }
    
    public void showInventory() {
        System.out.println("\n=== ИНВЕНТАРЬ ===");
        
        if (inventory.isEmpty()) {
            System.out.println("Инвентарь пуст");
            return;
        }
        
        Map<String, Long> itemCounts = inventory.stream()
            .collect(Collectors.groupingBy(item -> item, Collectors.counting()));
        
        itemCounts.entrySet().stream()
            .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
            .forEach(entry -> {
                String item = entry.getKey();
                long count = entry.getValue();
                System.out.printf("%-15s: %d шт.\n", item, count);
            });
            
        System.out.println("Всего предметов: " + inventory.size());
    }
    
    public void addExperience(int amount) {
        experience += amount;
        if (experience >= 100) {
            System.out.println("[УРОВЕНЬ] Уровень повышен!");
            experience -= 100;
        }
    }
    
    public String getName() { return name; }
    public int getExperience() { return experience; }
    public List<String> getInventory() { return inventory; }
    
    public int calculateScore() {
        return experience * 10 + inventory.size() * 5;
    }
}