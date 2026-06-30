package com.tx.outsourcingmis.cli;

import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.service.JvmMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CliCommandExecutor {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JvmMetricsService jvmMetricsService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void execute(String command, String[] params) {
        switch (command) {
            case "help":
                showHelp();
                break;
            case "stats":
                showStats();
                break;
            case "users":
                listUsers();
                break;
            case "health":
                checkHealth();
                break;
            case "logs":
                showLogs();
                break;
            case "jvm":
                showJvmInfo();
                break;
            case "redis":
                showRedisInfo();
                break;
            case "clear":
                clearScreen();
                break;
            default:
                System.out.println("未知命令: " + command + "，输入 'help' 查看帮助");
                break;
        }
    }

    private void showHelp() {
        System.out.println("\n========================================");
        System.out.println("  外包人员管理系统 - CLI 命令帮助");
        System.out.println("========================================");
        System.out.println("  help      - 显示此帮助信息");
        System.out.println("  stats     - 显示系统统计信息");
        System.out.println("  users     - 列出所有用户");
        System.out.println("  health    - 系统健康检查");
        System.out.println("  logs      - 查看最新日志");
        System.out.println("  jvm       - 查看 JVM 信息");
        System.out.println("  redis     - 查看 Redis 信息");
        System.out.println("  clear     - 清屏");
        System.out.println("  exit      - 退出 CLI");
        System.out.println("========================================");
    }

    private void showStats() {
        System.out.println("\n========================================");
        System.out.println("  系统统计信息");
        System.out.println("========================================");

        long userCount = userMapper.selectCount(null);
        System.out.println("  总用户数: " + userCount);

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        System.out.println("  系统运行时间: " + formatUptime(runtime.getUptime()));
        System.out.println("  当前时间: " + LocalDateTime.now().format(FORMATTER));
        System.out.println("========================================");
    }

    private void listUsers() {
        System.out.println("\n========================================");
        System.out.println("  用户列表");
        System.out.println("========================================");
        System.out.println("  ID  | 用户名 | 角色 | 状态");
        System.out.println("  ----|--------|------|------");

        var users = userMapper.selectList(null);
        users.forEach(u -> {
            System.out.printf("  %-4d| %-6s | %-4s | %s%n",
                    u.getId(),
                    u.getUsername(),
                    u.getRole(),
                    u.getStatus() == 1 ? "启用" : "禁用"
            );
        });
        System.out.println("========================================");
        System.out.println("  共 " + users.size() + " 名用户");
    }

    private void checkHealth() {
        System.out.println("\n========================================");
        System.out.println("  系统健康检查");
        System.out.println("========================================");

        try {
            redisTemplate.opsForValue().set("health_check", "ok");
            String result = (String) redisTemplate.opsForValue().get("health_check");
            System.out.println("  Redis: " + ("ok".equals(result) ? "✅ 正常" : "❌ 异常"));
        } catch (Exception e) {
            System.out.println("  Redis: ❌ 连接失败 - " + e.getMessage());
        }

        try {
            long count = userMapper.selectCount(null);
            System.out.println("  MySQL: ✅ 正常 (用户数: " + count + ")");
        } catch (Exception e) {
            System.out.println("  MySQL: ❌ 连接失败 - " + e.getMessage());
        }

        Map<String, Object> jvmInfo = jvmMetricsService.getJvmMemoryInfo();
        Object heapPercent = jvmInfo.getOrDefault("heap_used_percent", 0.0);
        System.out.println("  JVM 堆内存: " + String.format("%.1f%%", heapPercent));
        System.out.println("========================================");
    }

    private void showLogs() {
        System.out.println("\n========================================");
        System.out.println("  最新日志 (最后20行)");
        System.out.println("========================================");
        try {
            java.nio.file.Path logPath = java.nio.file.Paths.get("logs/outsourcing-mis.log");
            if (java.nio.file.Files.exists(logPath)) {
                java.util.List<String> lines = java.nio.file.Files.readAllLines(logPath);
                int start = Math.max(0, lines.size() - 20);
                for (int i = start; i < lines.size(); i++) {
                    System.out.println(lines.get(i));
                }
            } else {
                System.out.println("日志文件不存在，请检查 logging.file.name 配置");
            }
        } catch (Exception e) {
            System.out.println("读取日志失败: " + e.getMessage());
        }
        System.out.println("========================================");
    }

    private void showJvmInfo() {
        System.out.println("\n========================================");
        System.out.println("  JVM 信息");
        System.out.println("========================================");
        Map<String, Object> info = jvmMetricsService.getJvmMemoryInfo();
        for (Map.Entry<String, Object> entry : info.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.contains("percent") && value instanceof Number) {
                System.out.printf("  %s: %.1f%%%n", key, ((Number) value).doubleValue());
            } else {
                System.out.println("  " + key + ": " + value);
            }
        }
        System.out.println("========================================");
    }

    private void showRedisInfo() {
        System.out.println("\n========================================");
        System.out.println("  Redis 缓存信息");
        System.out.println("========================================");
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                System.out.println("  缓存键数量: " + keys.size());
                keys.stream().limit(10).forEach(k -> System.out.println("    - " + k));
                if (keys.size() > 10) {
                    System.out.println("    ... 还有 " + (keys.size() - 10) + " 个键");
                }
            } else {
                System.out.println("  缓存为空");
            }
        } catch (Exception e) {
            System.out.println("  Redis 连接失败: " + e.getMessage());
        }
        System.out.println("========================================");
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private String formatUptime(long uptimeMillis) {
        long seconds = uptimeMillis / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (days > 0) {
            return String.format("%d天 %d时 %d分 %d秒", days, hours, minutes, secs);
        } else if (hours > 0) {
            return String.format("%d时 %d分 %d秒", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d分 %d秒", minutes, secs);
        } else {
            return String.format("%d秒", secs);
        }
    }
}