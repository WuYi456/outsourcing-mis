package com.tx.outsourcingmis.cli;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Slf4j
@Component
public class OutsourcingMisCli implements CommandLineRunner {

    private final CliCommandExecutor commandExecutor;

    public OutsourcingMisCli(CliCommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void run(String... args) {
        log.info("CLI 命令行已启动，输入 'help' 查看帮助");
        log.info("========================================");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\n>> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();
            String[] params = new String[parts.length - 1];
            System.arraycopy(parts, 1, params, 0, params.length);

            if ("exit".equals(command) || "quit".equals(command)) {
                System.out.println("正在退出...");
                break;
            }

            commandExecutor.execute(command, params);
        }
        scanner.close();
    }
}