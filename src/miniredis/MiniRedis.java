package miniredis;

import miniredis.backup.AOFLoader;
import miniredis.command.CommandParser;
import miniredis.core.Context;
import miniredis.core.ExpireTask;

import java.util.Scanner;

public class MiniRedis {

    public static void main(String[] args) {
        Context context = new Context();
        // 加载历史数据 AOF
        AOFLoader.load(context);
        // 定时检查过期数据
        ExpireTask.start(context);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("> ");
                String input = scanner.nextLine();
                System.out.println(CommandParser.parse(input).execute(context));
            } catch (RuntimeException e) {
                System.out.println("ERROR: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("SYSTEM ERROR");
                e.printStackTrace();
            }
        }
    }
}
