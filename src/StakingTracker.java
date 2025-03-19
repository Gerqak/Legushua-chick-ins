import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StakingTracker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите название стейкинга: ");
        String stakingName = scanner.nextLine();

        System.out.print("Введите дату начала (yyyy-MM-dd HH:mm): ");
        String startDateTimeStr = scanner.nextLine();

        System.out.print("Выберите тип фаз (1 - 24/12/6, 2 - 12/6/3): ");
        int phaseType = scanner.nextInt();

        LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        List<String> checkIns = generateCheckIns(startDateTime, stakingName, phaseType);

        saveToICSFile(stakingName, checkIns);
        System.out.println("Чек-ины сохранены в .ics файл!");
    }

    private static List<String> generateCheckIns(LocalDateTime startDateTime, String stakingName, int phaseType) {
        List<String> checkIns = new ArrayList<>();
        LocalDateTime currentCheckIn = startDateTime.plusHours(phaseType == 1 ? 24 : 12);
        int phase = 1;

        int[] phaseDurations = phaseType == 1 ? new int[]{24, 12, 6} : new int[]{12, 6, 3};
        int[] checkInCounts = phaseType == 1 ? new int[]{6, 7, 11} : new int[]{13, 14, 20};

        for (int p = 0; p < 3; p++) {
            for (int i = 0; i < checkInCounts[p]; i++) {
                checkIns.add(createEvent(currentCheckIn, phase, stakingName));
                currentCheckIn = currentCheckIn.plusHours(phaseDurations[p]).plusMinutes(30);
            }
            if (p < 2) {
                checkIns.add(createBreakEvent(currentCheckIn, "Перерыв перед Фазой " + (phase + 1)));
                currentCheckIn = currentCheckIn.plusHours(2);
                currentCheckIn = currentCheckIn.plusHours(phaseDurations[p + 1]);
            }
            phase++;
        }
        return checkIns;
    }

    private static String createEvent(LocalDateTime checkIn, int phase, String stakingName) {
        LocalDateTime endCheckIn = checkIn.plusMinutes(30);
        DateTimeFormatter icsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

        return "BEGIN:VEVENT\n" +
                "DTSTART:" + checkIn.format(icsFormatter) + "\n" +
                "DTEND:" + endCheckIn.format(icsFormatter) + "\n" +
                "SUMMARY:" + stakingName + " (Фаза " + phase + ")\n" +
                "BEGIN:VALARM\n" +
                "ACTION:AUDIO\n" +
                "TRIGGER:PT0M\n" +
                "ATTACH;VALUE=URI:Basso\n" +
                "DESCRIPTION:Voice\n" +
                "END:VALARM\n" +
                "END:VEVENT\n";
    }

    private static String createBreakEvent(LocalDateTime breakStart, String summary) {
        LocalDateTime breakEnd = breakStart.plusHours(2);
        DateTimeFormatter icsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

        return "BEGIN:VEVENT\n" +
                "DTSTART:" + breakStart.format(icsFormatter) + "\n" +
                "DTEND:" + breakEnd.format(icsFormatter) + "\n" +
                "SUMMARY:" + summary + "\n" +
                "END:VEVENT\n";
    }

    private static void saveToICSFile(String stakingName, List<String> checkIns) {
        File file = new File("staking_checkins.ics");
        try (FileWriter writer = new FileWriter(file, false)) { // false для очистки файла при каждом запуске
            writer.write("BEGIN:VCALENDAR\n");
            writer.write("PRODID:-//LEGUSHUA w//EN\n");
            writer.write("VERSION:2.0\n");
            writer.write("METHOD:PUBLISH\n");

            for (String event : checkIns) {
                writer.write(event);
            }

            writer.write("END:VCALENDAR\n");
        } catch (IOException e) {
            System.out.println("Ошибка записи в .ics файл: " + e.getMessage());
        }
    }
}
