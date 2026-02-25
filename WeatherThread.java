package greenhousesimulator;

import java.util.Random;

public class WeatherThread extends Thread {
    private Greenhouse greenhouse;
    private volatile boolean running = true;
    private Random random = new Random();
    private int updateInterval = 30000; // 30 секунд
    
    public WeatherThread(Greenhouse greenhouse) {
        this.greenhouse = greenhouse;
    }
    
    @Override
    public void run() {
        String[] weatherTypes = {"Солнечно", "Облачно", "Дождь", "Туман"};
        
        try {
            while (running) {
                Thread.sleep(updateInterval);
                
                synchronized (greenhouse) {
                    String weather = weatherTypes[random.nextInt(weatherTypes.length)];
                    int tempChange = 0;
                    int humidityChange = 0;
                    
                    switch (weather) {
                        case "Солнечно":
                            tempChange = 2;
                            humidityChange = -5;
                            break;
                        case "Облачно":
                            tempChange = -1;
                            humidityChange = 5;
                            break;
                        case "Дождь":
                            humidityChange = 15;
                            greenhouse.getPlants().forEach(p -> p.water(15));
                            break;
                        case "Туман":
                            humidityChange = 10;
                            tempChange = -2;
                            break;
                    }
                    
                    greenhouse.setTemperature(
                        Math.max(15, Math.min(30, greenhouse.getTemperature() + tempChange)));
                    greenhouse.setHumidity(
                        Math.max(30, Math.min(90, greenhouse.getHumidity() + humidityChange)));
                    
                    System.out.println("\n[ПОГОДА] Сейчас: " + weather + 
                                     ", t°: " + greenhouse.getTemperature() + 
                                     "°C, влажность: " + greenhouse.getHumidity() + "%");
                    
                    if (random.nextInt(100) < 15) {
                        triggerRandomEvent();
                    }
                }
            }
        } catch (InterruptedException e) { }
    }
    
    private void triggerRandomEvent() {
        int event = random.nextInt(3);
        switch (event) {
            case 0:
                System.out.println("[СОБЫТИЕ] Солнечный день! Все растения получают +10 к здоровью.");
                greenhouse.getPlants().forEach(p -> p.restoreHealth(10));
                break;
            case 1:
                System.out.println("[СОБЫТИЕ] Легкий дождик полил растения.");
                greenhouse.getPlants().forEach(p -> p.water(20));
                break;
            case 2:
                System.out.println("[СОБЫТИЕ] Плодородный день! Удобрения работают эффективнее.");
                greenhouse.getPlants().forEach(p -> p.fertilize(10));
                break;
        }
    }
    
    public void stopThread() {
        running = false;
        this.interrupt();
    }
}