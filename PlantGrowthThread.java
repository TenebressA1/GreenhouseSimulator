package greenhousesimulator;

import greenhousesimulator.ui.GameWindow;

public class PlantGrowthThread extends Thread {
    private Greenhouse greenhouse;
    private GameWindow gameWindow;
    private volatile boolean running = true;
    private int dayDuration = 120000;
    private int healthCheckInterval = 15000;
    
    public PlantGrowthThread(Greenhouse greenhouse, GameWindow gameWindow) {
        this.greenhouse = greenhouse;
        this.gameWindow = gameWindow;
    }
    
    @Override
    public void run() {
        gameWindow.log("[ИНФО] 1 игровой день = 2 минуты");
        gameWindow.log("[ИНФО] Здоровье проверяется каждые 15 секунд");
        gameWindow.log("[ИНФО] Каждый день вода уменьшается на 10 единиц");
        
        long lastDayUpdate = System.currentTimeMillis();
        long lastHealthCheck = System.currentTimeMillis();
        int dayCounter = 0;
        
        try {
            while (running) {
                long currentTime = System.currentTimeMillis();
                
                if (currentTime - lastHealthCheck >= healthCheckInterval) {
                    synchronized (greenhouse) {
                        checkPlantHealth();
                        lastHealthCheck = currentTime;
                    }
                }
                
                if (currentTime - lastDayUpdate >= dayDuration) {
                    synchronized (greenhouse) {
                        updateDay();
                        dayCounter++;
                        lastDayUpdate = currentTime;
                        
                        if (dayCounter % 3 == 0) {
                            greenhouse.updatePlants();
                        }
                    }
                }
                
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) { }
    }
    
    private void checkPlantHealth() {
        boolean healthChanged = false;
        
        for (Plant plant : greenhouse.getPlants()) {
            if (plant.isAlive()) {
                if (plant.getWaterLevel() == 0) {
                    plant.reduceHealth(10);
                    healthChanged = true;
                }
                
                if (plant.getFertilizerLevel() >= 100) {
                    plant.restoreHealth(10);
                    plant.setFertilizerLevel(0);
                    healthChanged = true;
                }
                
                plant.degrade();
            }
        }
        
        if (healthChanged) {
            greenhouse.removeDeadPlants();
        }
    }
    
    private void updateDay() {
        greenhouse.nextDay();
        gameWindow.log("\n[АВТО] Прошел 1 день. День " + greenhouse.getDay());
        
        for (Plant plant : greenhouse.getPlants()) {
            if (plant.isAlive()) {
                int currentWater = plant.getWaterLevel();
                int newWater = Math.max(0, currentWater - 10);
                plant.setWaterLevel(newWater);
                
                if (newWater == 0) {
                    gameWindow.log("[ВНИМАНИЕ] " + plant.getType() + ": вода закончилась!");
                }
            }
        }
    }
    
    public void stopThread() {
        running = false;
        this.interrupt();
    }
}