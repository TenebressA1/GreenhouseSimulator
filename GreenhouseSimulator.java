package greenhousesimulator;

import java.util.Scanner;

public class GreenhouseSimulator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameEngine game = new GameEngine();
        
        while (true) {
            System.out.println("\n=== ТЕПЛИЧНЫЙ СИМУЛЯТОР ===");
            System.out.println("1. Новая игра");
            System.out.println("2. Загрузить теплицу");
            System.out.println("3. Таблица рекордов");
            System.out.println("4. Выход");
            System.out.print("Выбор: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Неверный ввод!");
                scanner.nextLine();
                continue;
            }
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    game.startNewGame();
                    break;
                case 2:
                    game.loadGame();
                    break;
                case 3:
                    GreenhouseDatabase.showTopFarmers();
                    System.out.println("\nНажмите Enter чтобы вернуться в меню...");
                    scanner.nextLine();
                    break;
                case 4:
                    GreenhouseDatabase.close();
                    System.out.println("До свидания!");
                    System.exit(0);
                default:
                    System.out.println("Неверный выбор!");
            }
        }
    }
}