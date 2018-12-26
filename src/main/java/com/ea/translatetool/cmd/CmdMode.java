package com.ea.translatetool.cmd;

import com.ea.translatetool.App;
import com.ea.translatetool.addit.Addit;
import com.ea.translatetool.addit.mode.Translate;
import com.ea.translatetool.addit.WorkCallback;
import com.ea.translatetool.addit.mode.WorkStage;
import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.util.ShutdownHandler;
import com.ea.translatetool.util.WindowTool;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HeRui on 2018/12/22.
 */
public class CmdMode {
    private App app;
    private static CmdMode cmdMode;
    private Options opts = new Options();
    private List<Translate> translates;
    private int exitStatus;

    private CmdMode(App app) {
        this.app = app;
        translates = new ArrayList<Translate>();
        exitStatus = 1;
        addShutdownHandler();
    }

    public synchronized static void start(App app, String[] args) {
        if(cmdMode == null) {
            cmdMode = new CmdMode(app);
            cmdMode.definedOptions();
            try {
                WorkConfig config = cmdMode.parseOptions(args, Addit.getDefaultWorkConfig(), 1);
                if(config != null) {
                    cmdMode.doStart(config);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doStart(WorkConfig config) {
        Addit.start(config, 0, new WorkCallback() {

            @Override
            public void onStart(WorkStage stage) {
                System.out.println(com.ea.translatetool.util.WindowTool.getInstance().getCmdHwnd());
                com.ea.translatetool.util.WindowTool.getInstance().enableSystemMenu(com.ea.translatetool.util.WindowTool.SC_CLOSE, false);
                if(stage.getIndex() == 1) {
                    System.out.println("start ..");
                }
                System.out.println(stage.getName());
            }

            @Override
            public void onProgress(long complete, long total) {
                showProgress(complete, total, 50);
            }

            @Override
            public void onDone(WorkStage stage) {
                System.out.println("\n"+stage.getName()+" finished.");
                WindowTool.getInstance().enableSystemMenu(com.ea.translatetool.util.WindowTool.SC_CLOSE, true);
            }

            @Override
            public void onError(Throwable t) {

            }
        });
    }

    private void addShutdownHandler() {
        // 异常终止处
        ShutdownHandler.addShutdownHandler(new ShutdownHandler() {
            @Override
            public void run() {
                if(Addit.isRunning()) {
                    System.out.println(createStringFromString("\b", 60)
                            + createStringFromString(" ", 60) + createStringFromString("\b", 60)
                            +"WARNING:Terminating now will cause the task to fail!\n" +
                            "app will wait for the task to complete!");
                }
                while (Addit.isRunning()) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {}
                }
                if(exitStatus != 0) {
                    System.err.println("\nabnormal termination.");
                }
            }
        });
    }

    void exit(int status) {
        this.exitStatus = status;
        System.exit(status);
    }

    // 定义命令行参数
    private void definedOptions() {
        opts = new Options();
        Option optHelp = new Option("h", "help", false,"Help of translate tool");
        Option optVersion = new Option("v", "version", false,"Look version for translate addi.");
        Option optVertical = new Option("vt", "vertical", false,"Set read vertical in excel");

        Option optKeyColumn = Option.builder("kc").longOpt("key-column")
                .hasArg().argName("number").desc("Set key column in excel, default automatic recognition.").build();

        Option optLocalColumn = Option.builder("lc").longOpt("local-column")
                .hasArg().argName("number").desc("Set local column in excel, default automatic recognition.").build();

        Option optTranslateColumn = Option.builder("tc").longOpt("translate-column")
                .hasArg().argName("number").desc("Set translate column in excel, default automatic recognition.").build();

        Option optInput = Option.builder("i").longOpt("input")
                .hasArgs().argName("path1,path2...").valueSeparator(',').desc("Source file of excel.").build();

        Option optOutput = Option.builder("o").longOpt("out")
                .hasArg().argName("dir").desc("out json or xml").build();

        Option optOutputType = Option.builder("ot").longOpt("outType")
                .hasArg().argName("json or xml").desc("Default value is json").build();

        Option optColumns = Option.builder("cs").longOpt("columns")
                .hasArgs().argName("number1,number1,number1").valueSeparator(',').desc("key,local,translate").build();

        opts.addOption(optHelp);
        opts.addOption(optVersion);
        opts.addOption(optVertical);
        opts.addOption(optInput);
        opts.addOption(optOutput);
        opts.addOption(optOutputType);
        opts.addOption(optKeyColumn);
        opts.addOption(optLocalColumn);
        opts.addOption(optTranslateColumn);
        opts.addOption(optColumns);
    }

    // 解析处理命令行参数
    private WorkConfig parseOptions(String[] args, WorkConfig config, int mode) throws IOException, InterruptedException {
        if(config == null) {
            config = new WorkConfig();
        }
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        // 解析命令行参数
        try {
            line = parser.parse(opts, args);
        } catch (ParseException e) {
            if(mode == 1) {
                System.err.println(e.getMessage());
                exit(1);
            }
        }

        if(mode == 1) {
            if (args == null || args.length == 0 || line.hasOption("h")) {
                HelpFormatter help = new HelpFormatter();
                help.printHelp("translate addi", opts);
                if (line.hasOption("h")) {
                    return null;
                } else {
                    inInputMode(config);
                    return null;
                }
            }

            if (line.hasOption("v")) {
                System.out.println("The version is 1.0 of translate addi.");
                return null;
            }
        }

        if (line.hasOption("i")) {
            String[] paths = line.getOptionValues("i");
            List<File> files = new ArrayList<File>();
            for (String path : paths) {
                File file = new File(path);
                if (!file.exists() || !file.isDirectory()) {
                    throw new IllegalArgumentException("The -i or -input option must is specify dir or path of file.");
                } else {
                    files.add(file);
                }
            }
            config.setInput(files);
        }

        if (line.hasOption("o")) {
            String path = line.getOptionValue("i");
            File file = new File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("The -o or -out option must is specify dir.");
            }
            config.setOutput(file);
        }

        if (line.hasOption("cs")) {
            String[] columns = line.getOptionValues("cs");
            if (columns.length != 3) {
                throw new IllegalArgumentException("The -cs or -columns option must is 3 number and separator sign of ','.");
            }
            config.setKeyColumn(Integer.parseInt(columns[0]));
            config.setLocalColumn(Integer.parseInt(columns[1]));
            config.setTranslateColumn(Integer.parseInt(columns[2]));
        }

        if (line.hasOption("kc")) {
            try {
                config.setKeyColumn(Integer.parseInt(line.getOptionValue("kc")));
            } catch (Exception e) {
                throw new IllegalArgumentException("invalid key-column value on kc");
            }
        }

        if (line.hasOption("lc")) {
            try {
                config.setLocalColumn(Integer.parseInt(line.getOptionValue("lc")));
            } catch (Exception e) {
                throw new IllegalArgumentException("invalid local-column value on kc");
            }
        }

        if (line.hasOption("tc")) {
            try {
                config.setTranslateColumn(Integer.parseInt(line.getOptionValue("tc")));
            } catch (Exception e) {
                throw new IllegalArgumentException("invalid translate-column value on kc");
            }
        }

        if (line.hasOption("ot")) {
            String type = line.getOptionValue("ot");
            boolean findType = false;
            StringBuffer stringBuffer = new StringBuffer("[");
            for (GlobalConstant.OutType outType : GlobalConstant.OutType.values()) {
                if(outType.getValue().endsWith(type)) {
                    config.setOutType(type);
                    findType = true;
                    break;
                }
                stringBuffer.append(outType.getValue()).append(",");
            }
            if(!findType) {
                stringBuffer.setCharAt(stringBuffer.length()-1, ']');
                throw new IllegalArgumentException("invalid value on ot, value should in "+stringBuffer.toString());
            }
        }

        if(line.hasOption("vt")) {
            config.setVertical(true);
        }

        return config;
    }

    private void inInputMode(WorkConfig config) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            byte[] startBytes = "> ".getBytes();
            System.out.write("\nnow entry input mode, please set the parameters and input 'start' cmd to work.\n".getBytes());
            System.out.write("input cmd of 'help' look for help of input mode.\n".getBytes());
            System.out.write(startBytes);
            List<String> notEndCmdLines = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                switch (line) {
                    case "help":
                        printInputModeHelp();
                        break;
                    case "start":
                        doStart(config);
                        break;
                    case "dll":
                        deleteLastLine(notEndCmdLines);
                        break;
                    case "clear":
                        notEndCmdLines.clear();
                        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                        break;
                    case "list":
                        printKeyList();
                        break;
                    case "exit":
                        System.out.println("bye.");
                        exitStatus = 0;
                        return;
                    default:
                        parseWorkConfigCmd(notEndCmdLines, config, line);
                }
                System.out.write(startBytes);
            }
        }catch (Exception e) {
            throw e;
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void printKeyList() {
        if(translates.size() == 0) {
            System.out.println("not translates");
        }
        for (Translate t : translates) {
            System.out.println(t);
        }
    }

    private void deleteLastLine(List<String> notEndCmdLines) {
        if(notEndCmdLines.size() > 0) {
            notEndCmdLines.remove(notEndCmdLines.size()-1);
        }

        if(notEndCmdLines.size() == 0) {
            System.out.println("lines is empty.");
        } else {
            System.out.println("---------------all lines---------------");
            for (String line : notEndCmdLines) {
                System.out.println("> "+line);
            }
        }
    }

    private void parseWorkConfigCmd(List<String> lines, WorkConfig config, String newCmd) {
        if(newCmd.length() > 0) {
            if(newCmd.endsWith(";")) {
                newCmd = new StringBuffer(newCmd).deleteCharAt(newCmd.length()-1).toString();
                if(!newCmd.isEmpty()) {
                    lines.add(newCmd);
                }
                if(lines.size() > 0) {
                    for (String cmd : lines) {
                        if(cmd.startsWith("add")) {

                        } else if(cmd.startsWith("del")) {

                        } else {
                            String[] args = ("-"+cmd).split("\\s");
                            try {
                                parseOptions(args, config, 2);
                            } catch (Exception e) {
                                String message = e.getMessage();
                                if(message == null) {
                                    message = "unsupported command: "+cmd;
                                }
                                System.err.println("Error:"+message);
                            }
                        }
                    }
                }
                lines.clear();
            } else {
                lines.add(newCmd);
            }
        }
    }

    private void printInputModeHelp() {
        System.out.println("add <key,local,translate>; add translate.");
        System.out.println("clear                      clear the screen.");
        System.out.println("del <key>;                 delete key from list.");
        System.out.println("dll                        delete last line of input.");
        System.out.println("exit                       exit tool.");
        System.out.println("help                       look for help of input mode.");
        System.out.println("i <path1,path2...>;        set source.");
        System.out.println("kc <number>;               set key-column.");
        System.out.println("lc <number>;               set local-column.");
        System.out.println("list                       show the list of keys.");
        System.out.println("o <dir>;                   set out dir.");
        System.out.println("ot <[json|xml]>;           set out type,default is json.");
        System.out.println("start                      start do work.");
        System.out.println("tc <number>;               set translate-column.");
        System.out.println("vt [true|false];           set vertical. empty value same true");
    }

    public static String createStringFromString(String s, int length) {
        if(length <= 0 || s == null ||  s.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(s);
        while (stringBuilder.length() < length) {
            stringBuilder.append(s);
        }
        if(stringBuilder.length() == length) {
            return stringBuilder.toString();
        }
        return stringBuilder.substring(0, length);
    }

    public static void showProgress(long complete, long total, int psWidth) {
        float p = 1.0f * complete / total;
        int c = (int)(psWidth * p);
        int s = psWidth-c;
        System.out.print(createStringFromString("\b", psWidth + 10));
        System.out.print(createStringFromString(">", c));
        System.out.print(createStringFromString("_", s));
        System.out.printf(" [%.2f%%]", p*100);
    }
}
