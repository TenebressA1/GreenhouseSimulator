package greenhousesimulator;

import java.util.*;

public class GameEngine {
    private Greenhouse greenhouse;
    private Player player;
    private Scanner scanner;
    private PlantGrowthThread growthThread;
    private WeatherThread weatherThread;
    private boolean gameRunning;
    
    public void startNewGame() {
        scanner = new Scanner(System.in);
        
        System.out.print("Введите ваше имя: ");
        String playerName = scanner.nextLine().trim();
        if (playerName.isEmpty()) playerName = "Фермер";
        
        greenhouse = new Greenhouse();
        player = new Player(playerName);
        gameRunning = true;
        
        System.out.println("\nДобро пожаловать в теплицу, " + playerName + "!");
        System.out.println("Ваша задача - выращивать растения и зарабатывать деньги.");
        
        System.out.println("\n=== ВАШ СТАРТОВЫЙ ИНВЕНТАРЬ ===");
        player.showInventory();
        
        System.out.println("\n=== ВАЖНАЯ ИНФОРМАЦИЯ ===");
        System.out.println("1 игровой день = 2 минуты реального времени");
        System.out.println("Здоровье проверяется каждые 15 секунд");
        System.out.println("При нулевой воде здоровье падает на 10 каждые 15 сек");
        System.out.println("При 100% удобрений здоровье растет на 10 каждые 15 сек");
        System.out.println("Каждый день вода уменьшается на 10 единиц");
        System.out.println("Нажмите Enter чтобы продолжить...");
        scanner.nextLine();
        
        growthThread = new PlantGrowthThread(greenhouse);
        weatherThread = new WeatherThread(greenhouse);
        growthThread.start();
        weatherThread.start();
        
        gameLoop();
    }
    
    public void loadGame() {
        System.out.println("Функция загрузки в разработке...");
        System.out.println("Начните новую игру!");
    }
    
    private void gameLoop() {
        while (gameRunning && greenhouse.getMoney() > 0) {
            greenhouse.showStatus();
            showMenu();
            
            System.out.print("\nВыбор: ");
            String input = scanner.nextLine().trim();
            
            processCommand(input);
            
            if (greenhouse.getMoney() <= 0 && greenhouse.getPlants().isEmpty()) {
                System.out.println("\n[ПОРАЖЕНИЕ] ВЫ ПРОИГРАЛИ!");
                System.out.println("Закончились деньги и растения...");
                gameRunning = false;
                endGame();
            }
        }
    }
    
    private void showMenu() {
        System.out.println("\n=== МЕНЮ ===");
        System.out.println("1. Посадить растение");
        System.out.println("2. Полить все растения");
        System.out.println("3. Удобрить все растения");
        System.out.println("4. Собрать урожай");
        System.out.println("5. Магазин");
        System.out.println("6. Инвентарь");
        System.out.println("7. Показать детали растений");
        System.out.println("8. Следующий день (ускорить)");
        System.out.println("9. Сохранить и выйти");
        System.out.println("0. Сдаться (закончить игру)");
    }
    
    private void processCommand(String input) {
        switch (input) {
            case "1": plantMenu(); break;
            case "2": greenhouse.waterAllPlants(); break;
            case "3": player.useFertilizer(greenhouse); break;
            case "4": harvestCrops(); break;
            case "5": shopMenu(); break;
            case "6": player.showInventory(); break;
            case "7": showPlantDetails(); break;
            case "8": forceNextDay(); break;
            case "9": saveAndExit(); break;
            case "0": gameRunning = false; endGame(); break;
            default: System.out.println("Неизвестная команда");
        }
    }
    
    private void plantMenu() {
        System.out.println("\nЧто посадить?");
        System.out.println("1. Помидор (50 руб.)");
        System.out.println("2. Огурец (40 руб.)");
        System.out.println("3. Перец (70 руб.)");
        System.out.println("4. Салат (30 руб.)");
        System.out.println("0. Назад");
        
        System.out.print("Выбор: ");
        String choice = scanner.nextLine();
        
        String plantType;
        int cost = 0;
        
        switch (choice) {
            case "1": plantType = "Помидор"; cost = 50; break;
            case "2": plantType = "Огурец"; cost = 40; break;
            case "3": plantType = "Перец"; cost = 70; break;
            case "4": plantType = "Салат"; cost = 30; break;
            case "0": return;
            default: System.out.println("Неверный выбор"); return;
        }
        
        boolean hasSeedsInInventory = player.hasSeeds(plantType);
        
        if (!hasSeedsInInventory) {
            System.out.println("[ОШИБКА] У вас нет семян " + plantType.toLowerCase() + "а!");
            System.out.print("Купить семена за " + cost + " руб.? (Y/N): ");
            String buyChoice = scanner.nextLine().trim().toUpperCase();
            
            if (buyChoice.equals("Y")) {
                player.buyItem("Семена_" + plantType, cost, greenhouse);
                player.plantSeed(plantType, greenhouse);
            }
        } else {
            System.out.print("Использовать семена из инвентаря? (Y/N): ");
            String useChoice = scanner.nextLine().trim().toUpperCase();
            
            if (useChoice.equals("Y")) {
                player.plantSeed(plantType, greenhouse);
            } else {
                System.out.print("Купить новые семена за " + cost + " руб.? (Y/N): ");
                String buyChoice = scanner.nextLine().trim().toUpperCase();
                
                if (buyChoice.equals("Y")) {
                    player.buyItem("Семена_" + plantType, cost, greenhouse);
                    player.plantSeed(plantType, greenhouse);
                }
            }
        }
    }
    
    private void harvestCrops() {
        List<Plant> readyPlants = greenhouse.getReadyToHarvest();
        
        if (readyPlants.isEmpty()) {
            System.out.println("Нет растений готовых к сбору");
            return;
        }
        
        System.out.println("Готовы к сбору " + readyPlants.size() + " растений:");
        readyPlants.forEach(System.out::println);
        
        System.out.print("Собрать все? (Y/N): ");
        String answer = scanner.nextLine().trim().toUpperCase();
        
        if (answer.equals("Y")) {
            int income = greenhouse.harvestAll();
            greenhouse.addMoney(income);
            player.addExperience(readyPlants.size() * 5);
        }
    }
    
    private void shopMenu() {
        System.out.println("\n=== МАГАЗИН ===");
        System.out.println("У вас: " + greenhouse.getMoney() + " руб.");
        System.out.println("1. Семена помидора - 50 руб.");
        System.out.println("2. Семена огурца - 40 руб.");
        System.out.println("3. Семена перца - 70 руб.");
        System.out.println("4. Семена салата - 30 руб.");
        System.out.println("5. Удобрение - 30 руб.");
        System.out.println("6. Система полива (ускоряет рост) - 200 руб.");
        System.out.println("0. Назад");
        
        System.out.print("Выбор: ");
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1": player.buyItem("Семена_Помидор", 50, greenhouse); break;
            case "2": player.buyItem("Семена_Огурец", 40, greenhouse); break;
            case "3": player.buyItem("Семена_Перец", 70, greenhouse); break;
            case "4": player.buyItem("Семена_Салат", 30, greenhouse); break;
            case "5": player.buyItem("Удобрение", 30, greenhouse); break;
            case "6": 
                if (greenhouse.getMoney() >= 200) {
                    greenhouse.spendMoney(200);
                    System.out.println("[ПОКУПКА] Установлена система полива! Растения растут быстрее.");
                } else {
                    System.out.println("[ОШИБКА] Недостаточно денег!");
                }
                break;
            case "0": return;
            default: System.out.println("Неверный выбор");
        }
    }
    
    private void showPlantDetails() {
        if (greenhouse.getPlants().isEmpty()) {
            System.out.println("Нет растений для отображения");
            return;
        }
        
        System.out.println("\n=== ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О РАСТЕНИЯХ ===");
        System.out.println("Внимание: если вода = 0, здоровье уменьшается на 10 каждые 15 сек!");
        System.out.println("Если удобрено на 100%, здоровье восстанавливается на 10 каждые 15 сек!");
        System.out.println();
        
        greenhouse.getPlants().stream()
            .sorted((p1, p2) -> {
                if (p1.isDying() && !p2.isDying()) return -1;
                if (!p1.isDying() && p2.isDying()) return 1;
                return Integer.compare(p2.getHealth(), p1.getHealth());
            })
            .forEach(System.out::println);
    }
    
    private void forceNextDay() {
        System.out.println("Ускоряем время...");

        greenhouse.getPlants().forEach(plant -> {
            if (plant.isAlive()) {
                int currentWater = plant.getWaterLevel();
                int newWater = Math.max(0, currentWater - 10);
                plant.setWaterLevel(newWater);

                if (newWater == 0) {
                    System.out.println("[ВНИМАНИЕ] " + plant.getType() + ": вода закончилась!");
                    plant.reduceHealth(10);
                }
            }
        });

        greenhouse.getPlants().forEach(plant -> {
            if (plant.isAlive()) {
                plant.degrade();
            }
        });

        greenhouse.updatePlants();
        greenhouse.nextDay();

        System.out.println("Наступил день " + greenhouse.getDay());
        System.out.println("Вода у всех растений уменьшена на 10 единиц");
    }
    
    private void saveAndExit() {
        System.out.println("Сохранение игры...");
        GreenhouseDatabase.saveGame(player.getName(), greenhouse, player);
        
        gameRunning = false;
        growthThread.stopThread();
        weatherThread.stopThread();
        
        try {
            growthThread.join();
            weatherThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        endGame();
    }
    
    private void endGame() {
        System.out.println("\n=== ИГРА ОКОНЧЕНА ===");
        System.out.println("Игрок: " + player.getName());
        System.out.println("Дней прожито: " + greenhouse.getDay());
        System.out.println("Финальный капитал: " + greenhouse.getMoney() + " руб.");
        System.out.println("Опыт: " + player.getExperience());
        
        int score = player.calculateScore() + greenhouse.getDay() * 10 + greenhouse.getMoney();
        System.out.println("Итоговый счет: " + score);
        
        GreenhouseDatabase.saveGame(player.getName(), greenhouse, player);
        GreenhouseDatabase.close();
        
        System.out.println("\nСпасибо за игру в Тепличный симулятор!");
    }
}