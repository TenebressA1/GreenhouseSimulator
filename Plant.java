package greenhousesimulator;

import java.util.Random;

public class Plant {
    private String type;
    private int growthStage; // 0-100
    private int health; // 0-100
    private int waterLevel; // 0-100
    private int fertilizerLevel; // 0-100
    private boolean isAlive = true;
    private Random random = new Random();
    
    public Plant(String type) {
        this.type = type;
        this.growthStage = 0;
        this.health = 100;
        this.waterLevel = 50;
        this.fertilizerLevel = 50;
    }
    
    public void grow() {
        if (!isAlive) return;
        int growthAmount = 3;
        
        if (waterLevel < 30) growthAmount -= 1;
        if (waterLevel > 80) growthAmount -= 1;
        if (fertilizerLevel > 70) growthAmount += 2;
        if (fertilizerLevel < 20) growthAmount -= 2;
        
        if (health < 50) growthAmount -= 1;
        if (health > 80) growthAmount += 1;
        
        growthStage = Math.min(100, growthStage + growthAmount);
    }
    
    public void degrade() {
        if (!isAlive) return;
        int waterLoss = random.nextInt(3);
        int fertilizerLoss = random.nextInt(2);

        waterLevel = Math.max(0, waterLevel - waterLoss);
        fertilizerLevel = Math.max(0, fertilizerLevel - fertilizerLoss);
    }
    
    public void water(int amount) {
        waterLevel = Math.min(100, waterLevel + amount);
        if (amount > 0) {
            health = Math.min(100, health + 2);
        }
    }
    
    public void fertilize(int amount) {
        fertilizerLevel = Math.min(100, fertilizerLevel + amount);
        if (amount > 0) {
            health = Math.min(100, health + 5);
        }
    }
    
    public void reduceHealth(int amount) {
        if (!isAlive) return;
        
        health = Math.max(0, health - amount);
        System.out.println("[ВНИМАНИЕ] " + type + " теряет здоровье: -" + amount + " (осталось: " + health + ")");
        
        if (health <= 0) {
            isAlive = false;
            System.out.println("[ГИБЕЛЬ] " + type + " погибло!");
        }
    }
    
    public void restoreHealth(int amount) {
        if (!isAlive) return;
        
        int oldHealth = health;
        health = Math.min(100, health + amount);
        int restored = health - oldHealth;
        
        if (restored > 0) {
            System.out.println("[ВОССТАНОВЛЕНИЕ] " + type + " восстанавливает здоровье: +" + restored + " (теперь: " + health + ")");
        }
    }
    
    public int harvest() {
        if (isReadyToHarvest() && isAlive()) {
            isAlive = false;
            int value = (growthStage * 2) + (health / 2);

            switch (type) {
                case "Помидор": return value * 3;
                case "Огурец": return value * 2;
                case "Перец": return value * 4;
                case "Салат": return value * 2;
                default: return value;
            }
        }
        return 0;
    }
    
    public String getType() { return type; }
    public int getGrowthStage() { return growthStage; }
    public int getHealth() { return health; }
    public int getWaterLevel() { return waterLevel; }
    public void setWaterLevel(int level) { this.waterLevel = Math.max(0, Math.min(100, level)); }
    public int getFertilizerLevel() { return fertilizerLevel; }
    public void setFertilizerLevel(int level) { this.fertilizerLevel = Math.max(0, Math.min(100, level)); }
    public boolean isAlive() { return isAlive; }
    public boolean isReadyToHarvest() { return growthStage >= 90 && isAlive; }
    public boolean needsWater() { return waterLevel < 40 && isAlive; }
    public boolean needsFertilizer() { return fertilizerLevel < 50 && isAlive; }
    public boolean isDying() { return health < 30 && isAlive; }
    
    @Override
    public String toString() {
        String status = "";
        if (!isAlive) {
            status = "Погибло";
        } else if (isReadyToHarvest()) {
            status = "Готов к сбору";
        } else if (isDying()) {
            status = "Требует ухода";
        } else {
            status = "Растет";
        }
        
        return String.format("%s: рост %d%%, здоровье %d, вода %d, удобрения %d [%s]",
            type, growthStage, health, waterLevel, fertilizerLevel, status);
    }
}