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
    
    public static void saveGame(String playerName, Greenhouse greenhouse, Player player) {
        try {
            checkConnection();
            
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO scores (player_name, score, day, plants_harvested, money) VALUES (?, ?, ?, ?, ?)");
            
            int plantsHarvested = greenhouse.getPlants().stream()
                .filter(Plant::isReadyToHarvest)
                .mapToInt(p -> 1)
                .sum();
            
            pstmt.setString(1, playerName);
            pstmt.setInt(2, player.calculateScore());
            pstmt.setInt(3, greenhouse.getDay());
            pstmt.setInt(4, plantsHarvested);
            pstmt.setInt(5, greenhouse.getMoney());
            
            pstmt.executeUpdate();
            
            System.out.println("[СИСТЕМА] Игра сохранена в таблице рекордов!");
            
        } catch (SQLException e) {
            System.err.println("[ОШИБКА] Не удалось сохранить: " + e.getMessage());
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
            
            showStatistics();
            
        } catch (SQLException e) {
            System.err.println("[ОШИБКА] Не удалось загрузить таблицу: " + e.getMessage());
            System.out.println("Таблица рекордов временно недоступна.");
        }
    }
    
    private static void showStatistics() {
        try {
            checkConnection();
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT score, day, money FROM scores");
            
            List<Integer> scores = new ArrayList<>();
            List<Integer> days = new ArrayList<>();
            List<Integer> moneyList = new ArrayList<>();
            
            while (rs.next()) {
                scores.add(rs.getInt("score"));
                days.add(rs.getInt("day"));
                moneyList.add(rs.getInt("money"));
            }
            
            if (!scores.isEmpty()) {
                System.out.println("\n=== СТАТИСТИКА ===");
                
                double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
                double avgDay = days.stream().mapToInt(Integer::intValue).average().orElse(0);
                double avgMoney = moneyList.stream().mapToInt(Integer::intValue).average().orElse(0);
                
                System.out.printf("Средний счет: %.1f\n", avgScore);
                System.out.printf("Средняя продолжительность: %.1f дней\n", avgDay);
                System.out.printf("Средний капитал: %.1f руб.\n", avgMoney);
                
                int bestScore = scores.stream().max(Integer::compare).orElse(0);
                int longestGame = days.stream().max(Integer::compare).orElse(0);
                
                System.out.println("Лучший счет: " + bestScore);
                System.out.println("Самая долгая игра: " + longestGame + " дней");
            }
            
        } catch (SQLException e) { }
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
}