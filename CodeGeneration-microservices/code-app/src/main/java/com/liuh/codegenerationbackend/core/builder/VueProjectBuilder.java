package com.liuh.codegenerationbackend.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.lang.Thread;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @Description 用于ai生成vue项目后构建vue项目
 */

@SuppressWarnings("all")
@Slf4j
@Component
public class VueProjectBuilder {


    /**
     * 异步构建vue项目
     */
    public void buildProjectAsync(String projectPath) {
        //创建一个java 的虚拟线程
        Thread.ofVirtual().name("vue-build-" + System.currentTimeMillis())
                //需要执行的任务
                .start(() -> {
                    //构建vue项目
                    try {
                        buildProject(projectPath);
                    } catch (Exception e) {
                        log.error("异步构建vue项目失败: {}", projectPath, e.getMessage(), e);
                    }
                });


    }


    /**
     * 构建vue项目
     *
     * @param projectPath 项目路径
     * @return 是否构建成功
     */
    public boolean buildProject(String projectPath) {
        //判断项目路径是否存在并且是否是文件夹
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目路径不存在或不是文件夹: {}", projectPath);
            return false;
        }

        //检查是否有 package.json 文件
        File packageJsonFile = new File(projectDir, "package.json");
        if (!packageJsonFile.exists()) {
            log.error("项目目录中缺少 package.json 文件: {}", projectPath);
            return false;
        }

        log.info("开始构建vue项目: {}", projectPath);
        //执行 npm install 命令
        if (!executeNpmInstall(projectDir)) {
            log.error("执行 npm install 失败，构建失败");
            return false;
        }

        //执行 npm run build 命令
        if (!executeNpmBuild(projectDir)) {
            log.error("执行 npm run build 失败，构建失败");
            return false;
        }

        //判断 dist 目录是否已经生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("构建完成，但是dist 目录不存在: {}", projectPath);
            return false;
        }

        log.info("构建vue项目成功: {}", projectPath);
        return true;
    }


    /**
     * 执行 npm install 命令
     *
     * @param projectDir 项目目录
     * @return 是否执行成功
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300); // 5分钟超时
    }
    /**
     * 执行 npm run build 命令
     *
     * @param projectDir 项目目录
     * @return 是否执行成功
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }


    /**
     * 判断是否是windows系统
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 根据操作系统执行构建命令
     * 是否添加  .cmd
     *
     * @param command 命令字符串
     * @return 命令字符串
     */
    private String buildCommand(String command) {
        if (isWindows()) {
            return command + ".cmd";
        } else {
            return command;
        }
    }

    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            //执行命令
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }

}
