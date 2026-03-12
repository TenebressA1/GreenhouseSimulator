package greenhousesimulator;

import java.sql.*;
import java.util.*;

public class GreenhouseDatabase {
    private static final String URL = "jdbc:sqlite:greenhouse.db";
    private static Connection conn = null;
    
    static {
        initializeDatabase();
    }
    
    private static void initializeDatabase() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement();
                
                // Таблица для рекордов
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS scores (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        player_name TEXT,
                        score INTEGER,
                        day INTEGER,
                        plants_harvested INTEGER,
                        money INTEGER,
                        save_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
                
                // СОЗДАЕМ ТАБЛИЦЫ ТОЛЬКО ЕСЛИ ИХ НЕТ
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS saves (
                        save_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        player_name TEXT,
                        save_name TEXT,
                        day INTEGER,
                        money INTEGER,
                        temperature INTEGER,
                        humidity INTEGER,
                        save_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
                
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS saved_plants (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        save_id INTEGER,
                        plant_type TEXT,
                        growth_stage INTEGER,
                        health INTEGER,
                        water_level INTEGER,
                        fertilizer_level INTEGER,
                        is_alive INTEGER,
                        FOREIGN KEY (save_id) REFERENCES saves(save_id) ON DELETE CASCADE
                    )
                    """);
                
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS saved_inventory (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        save_id INTEGER,
                        item_name TEXT,
                        FOREIGN KEY (save_id) REFERENCES saves(save_id) ON DELETE CASCADE
                    )
                    """);
            }
        } catch (SQLException e) {
            System.err.println("[ОШИБКА БД] Не удалось инициализировать: " + e.getMessage());
        }
    }
    
    private static void checkConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL);
        }
    }
    
    public static boolean saveGame(String playerName, String saveName, Greenhouse greenhouse, Player player) {
        try {
            checkConnection();
            conn.setAutoCommit(false);
            
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO saves (player_name, save_name, day, money, temperature, humidity) VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            
            pstmt.setString(1, playerName);
            pstmt.setString(2, saveName);
            pstmt.setInt(3, greenhouse.getDay());
            pstmt.setInt(4, greenhouse.getMoney());
            pstmt.setInt(5, greenhouse.getTemperature());
            pstmt.setInt(6, greenhouse.getHumidity());
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            int saveId = -1;
            if (rs.next()) {
                saveId = rs.getInt(1);
            }
            
            if (saveId == -1) {
                conn.rollback();
                return false;
            }
            
            PreparedStatement plantStmt = conn.prepareStatement(
                "INSERT INTO saved_plants (save_id, plant_type, growth_stage, health, water_level, fertilizer_level, is_alive) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            
            for (Plant plant : greenhouse.getPlants()) {
                plantStmt.setInt(1, saveId);
                plantStmt.setString(2, plant.getType());
                plantStmt.setInt(3, plant.getGrowthStage());
                plantStmt.setInt(4, plant.getHealth());
                plantStmt.setInt(5, plant.getWaterLevel());
                plantStmt.setInt(6, plant.getFertilizerLevel());
                plantStmt.setInt(7, plant.isAlive() ? 1 : 0);
                plantStmt.addBatch();
            }
            plantStmt.executeBatch();
            
            PreparedStatement invStmt = conn.prepareStatement(
                "INSERT INTO saved_inventory (save_id, item_name) VALUES (?, ?)"
            );
            
            for (String item : player.getInventory()) {
                invStmt.setInt(1, saveId);
                invStmt.setString(2, item);
                invStmt.addBatch();
            }
            invStmt.executeBatch();
            
            conn.commit();
            conn.setAutoCommit(true);
            
            System.out.println("[СИСТЕМА] Игра сохранена как '" + saveName + "'");
            return true;
            
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {}
            System.err.println("[ОШИБКА] Не удалось сохранить: " + e.getMessage());
            return false;
        }
    }
    
    public static List<SaveInfo> getSavesList() {
        List<SaveInfo> saves = new ArrayList<>();
        
        try {
            checkConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("""
                SELECT save_id, player_name, save_name, day, money, save_date 
                FROM saves 
                ORDER BY save_date DESC
                """);
            
            while (rs.next()) {
                saves.add(new SaveInfo(
                    rs.getInt("save_id"),
                    rs.getString("player_name"),
                    rs.getString("save_name"),
                    rs.getInt("day"),
                    rs.getInt("money"),
                    rs.getString("save_date")
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("[ОШИБКА] Не удалось загрузить список сохранений: " + e.getMessage());
        }
        
        return saves;
    }
    
    public static SaveData loadGame(int saveId) {
        try {
            checkConnection();
            
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM saves WHERE save_id = ?"
            );
            pstmt.setInt(1, saveId);
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                return null;
            }
            
            String playerName = rs.getString("player_name");
            int day = rs.getInt("day");
            int money = rs.getInt("money");
            int temperature = rs.getInt("temperature");
            int humidity = rs.getInt("humidity");
            
            List<Plant> plants = new ArrayList<>();
            PreparedStatement plantStmt = conn.prepareStatement(
                "SELECT * FROM saved_plants WHERE save_id = ?"
            );
            plantStmt.setInt(1, saveId);
            ResultSet plantRs = plantStmt.executeQuery();
            
            while (plantRs.next()) {
                Plant plant = new Plant(plantRs.getString("plant_type"));
                plant.setGrowthStage(plantRs.getInt("growth_stage"));
                plant.setHealth(plantRs.getInt("health"));
                plant.setWaterLevel(plantRs.getInt("water_level"));
                plant.setFertilizerLevel(plantRs.getInt("fertilizer_level"));
                plant.setAlive(plantRs.getInt("is_alive") == 1);
                plants.add(plant);
            }
            
            List<String> inventory = new ArrayList<>();
            PreparedStatement invStmt = conn.prepareStatement(
                "SELECT * FROM saved_inventory WHERE save_id = ?"
            );
            invStmt.setInt(1, saveId);
            ResultSet invRs = invStmt.executeQuery();
            
            while (invRs.next()) {
                inventory.add(invRs.getString("item_name"));
            }
            
            return new SaveData(playerName, day, money, temperature, humidity, plants, inventory);
            
        } catch (SQLException e) {
            System.err.println("[ОШИБКА] Не удалось загрузить игру: " + e.getMessage());
            return null;
        }
    }
    
    public static boolean deleteSave(int saveId) {
        try {
            checkConnection();
            
            PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM saves WHERE save_id = ?"
            );
            pstmt.setInt(1, saveId);
            pstmt.executeUpdate();
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("[ОШИБКА] Не удалось удалить сохранение: " + e.getMessage());
            return false;
        }
    }
    
    public static void saveScore(String playerName, Greenhouse greenhouse, Player player) {
        try {
            checkConnection();
            
            int plantsHarvested = 0;
            for (Plant plant : greenhouse.getPlants()) {
                if (plant.isReadyToHarvest()) {
                    plantsHarvested++;
                }
            }
            
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO scores (player_name, score, day, plants_harvested, money) VALUES (?, ?, ?, ?, ?)");
            
            pstmt.setString(1, playerName);
            pstmt.setInt(2, player.calculateScore());
            pstmt.setInt(3, greenhouse.getDay());
            pstmt.setInt(4, plantsHarvested);
            pstmt.setInt(5, greenhouse.getMoney());
            
            pstmt.executeUpdate();
            
            System.out.println("[РЕКОРД] Результат сохранен в таблице рекордов");
            
        } catch (SQLException e) {
            System.err.println("[ОШИБКА] Не удалось сохранить рекорд: " + e.getMessage());
        }
    }
    
    public static void showTopFarmers() {
        try {
            checkConnection();
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("""
                SELECT player_name, score, day, plants_harvested, money 
                FROM scores 
                ORDER BY score DESC 
                LIMIT 10
                """);
            
            System.out.println("\n=== ТОП-10 ФЕРМЕРОВ ===");
            System.out.println("№  Имя           Счёт   День  Урожай  Деньги");
            System.out.println("--------------------------------------------");
            
            int rank = 1;
            boolean hasResults = false;
            
            while (rs.next()) {
                hasResults = true;
                System.out.printf("%-2d %-12s %-6d %-5d %-7d %-8d\n",
                    rank,
                    rs.getString("player_name"),
                    rs.getInt("score"),
                    rs.getInt("day"),
                    rs.getInt("plants_harvested"),
                    rs.getInt("money"));
                rank++;
            }
            
            if (!hasResults) {
                System.out.println("Пока нет результатов. Станьте первым!");
            }
            
        } catch (SQLException e) {
            System.err.println("[ОШИБКА] Не удалось загрузить таблицу: " + e.getMessage());
        }
    }
    
    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("[СИСТЕМА] Соединение с БД закрыто");
            }
        } catch (SQLException e) {
            System.err.println("[ОШИБКА] Не удалось закрыть соединение: " + e.getMessage());
        }
    }
    
    public static class SaveInfo {
        public int id;
        public String playerName;
        public String saveName;
        public int day;
        public int money;
        public String date;
        
        public SaveInfo(int id, String playerName, String saveName, int day, int money, String date) {
            this.id = id;
            this.playerName = playerName;
            this.saveName = saveName;
            this.day = day;
            this.money = money;
            this.date = date;
        }
        
        @Override
        public String toString() {
            String shortDate = date.length() > 16 ? date.substring(0, 16) : date;
            return String.format("%s - День %d, %d руб. (%s)", saveName, day, money, shortDate);
        }
    }
    
    public static class SaveData {
        public String playerName;
        public int day;
        public int money;
        public int temperature;
        public int humidity;
        public List<Plant> plants;
        public List<String> inventory;
        
        public SaveData(String playerName, int day, int money, int temperature, int humidity, 
                       List<Plant> plants, List<String> inventory) {
            this.playerName = playerName;
            this.day = day;
            this.money = money;
            this.temperature = temperature;
            this.humidity = humidity;
            this.plants = plants;
            this.inventory = inventory;
        }
    }
}