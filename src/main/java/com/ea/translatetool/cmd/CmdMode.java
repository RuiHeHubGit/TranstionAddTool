package com.ea.translatetool.cmd;

import com.ea.translatetool.App;
import com.ea.translatetool.addit.Addit;
import com.ea.translatetool.addit.AdditAssist;
import com.ea.translatetool.addit.WorkCallback;
import com.ea.translatetool.addit.exception.AlreadyExistKeyException;
import com.ea.translatetool.addit.mode.Translation;
import com.ea.translatetool.addit.mode.TranslationLocator;
import com.ea.translatetool.addit.mode.WorkStage;
import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.util.LoggerUtil;
import com.ea.translatetool.util.ShutdownHandler;
import com.ea.translatetool.util.StringUtil;
import com.ea.translatetool.util.WindowTool;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.ea.translatetool.util.ExcelUtil.SUFFIX_XLS;
import static com.ea.translatetool.util.ExcelUtil.SUFFIX_XLSX;

/**
 * Created by HeRui on 2018/12/22.
 */
public class CmdMode {
    private App app;
    private static CmdMode cmdMode;
    private Options opts = new Options();
    private List<Translation> translations;
    private WorkConfig workConfig;
    private int exitStatus;

    private CmdMode(App app) {
        this.app = app;
        translations = new ArrayList<>();
        workConfig = AdditAssist.createWorkConfig(app.getAppConfig());
        exitStatus = 1;
        addShutdownHandler();
    }

    public synchronized static void start(App app, String[] args) {
        if(cmdMode == null) {
            cmdMode = new CmdMode(app);
            cmdMode.definedOptions();
            try {
                cmdMode.workConfig = cmdMode.parseOptions(args, cmdMode.workConfig, 1);
                if(cmdMode.workConfig != null) {
                    cmdMode.doStart(cmdMode.workConfig);
                }
            } catch (Exception e) {
                LoggerUtil.error(e.getMessage());
            }
        }
    }

    private void doStart(WorkConfig config) {
        Addit.doWork(config, Addit.WORK_SCAN_FILE, Addit.WORK_TRANSLATION_TO_FILE, new WorkCallback() {

            @Override
            public void onStart(WorkStage stage) {
                WindowTool.getInstance().enableSystemMenu(com.ea.translatetool.util.WindowTool.SC_CLOSE, false);
                if(stage.getIndex() == 1) {
                    System.out.println("doWork ..");
                }
                System.out.println(stage.getName()+" ("+stage.getIndex()+"/"+stage.getCount()+")");
            }

            @Override
            public void onProgress(long complete, long total) {
                showProgress(complete, total, 50);
            }

            @Override
            public void onDone(WorkStage stage) {
                System.out.println("\n"+stage.getName()+" finished.\n");
                WindowTool.getInstance().enableSystemMenu(WindowTool.SC_CLOSE, true);
            }

            @Override
            public boolean onError(Throwable t) {
                if(t instanceof AlreadyExistKeyException) {
                    if(app.getAppConfig().isCoverKey()) {
                        return false;
                    }
                    try {
                        AdditAssist.saveKeyExistTranslation(((AlreadyExistKeyException)t).getExistList(),
                                AdditAssist.createExistKeySaveFile(app.getAppConfig()));
                    } catch (IOException e) {
                        LoggerUtil.error(e.getMessage());
                    }
                    return true;
                } else {
                    LoggerUtil.exceptionLog(t);
                    return true;
                }
            }
        });
    }

    private void addShutdownHandler() {
        // 异常终止处
        ShutdownHandler.addShutdownHandler(new ShutdownHandler() {
            @Override
            public void run() {
                if(Addit.isRunning()) {
                    System.out.println(StringUtil.createStringFromString("\b", 60)
                            + StringUtil.createStringFromString(" ", 60) + StringUtil.createStringFromString("\b", 60)
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
        Option optHelp = new Option("h", "help", false,"Help of translation tool");
        Option optVersion = new Option("v", "version", false,"Look version for translation addi.");
        Option optVertical = new Option("vt", "vertical", false,"Set read vertical in excel");

        Option optInput = Option.builder("i").longOpt("input")
                .hasArgs().argName("path1,path2...").valueSeparator(',').desc("Source file of excel.").build();

        Option optOutput = Option.builder("o").longOpt("out")
                .hasArg().argName("dir").desc("out json or properties").build();

        Option optOutputType = Option.builder("ot").longOpt("outType")
                .hasArg().argName("json or properties").desc("Default value is json").build();

        Option optColumns = Option.builder("l").longOpt("locator")
                .hasArgs().argName("rnumber|cnumber,number1,number2,v|h").valueSeparator(',').desc("r or c,local,translation,vertical or horizontal").build();

        opts.addOption(optHelp);
        opts.addOption(optVersion);
        opts.addOption(optVertical);
        opts.addOption(optInput);
        opts.addOption(optOutput);
        opts.addOption(optOutputType);
        opts.addOption(optColumns);
    }

    // 解析处理命令行参数
    private WorkConfig parseOptions(String[] args, WorkConfig config, int mode) throws IOException, InterruptedException {
        if(config == null) {
            config = AdditAssist.createWorkConfig(app.getAppConfig());
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
                    enterInputMode(config);
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

        if (line.hasOption("l")) {
            String[] columns = line.getOptionValues("l");
            boolean pass = false;
            if (columns.length == 4) {
                File file = new File(columns[0]);
                Pattern patternKey = Pattern.compile("[VvRr]\\d{1,3}");
                Pattern patternNum = Pattern.compile("\\d{1,3}");
                Pattern patternOri = Pattern.compile("[VvHh]]");
                String fileName = file.getName().toUpperCase();
                if(file.exists() && file.isFile() && (fileName.endsWith(SUFFIX_XLS) || fileName.endsWith(SUFFIX_XLSX))
                        && patternKey.matcher(columns[1]).matches()
                        && patternNum.matcher(columns[2]).matches()
                        && patternNum.matcher(columns[3]).matches()
                        && patternOri.matcher(columns[4]).matches()) {

                    TranslationLocator translationLocator = new TranslationLocator();
                    translationLocator.setKeyLocator(columns[1]);
                    translationLocator.setLocalLocator(Integer.parseInt(columns[2]));
                    translationLocator.setTranslationLocator(Integer.parseInt(columns[3]));
                    translationLocator.setTranslationLocator("v".equalsIgnoreCase(columns[4])?
                            GlobalConstant.Orientation.VERTICAL.ordinal():GlobalConstant.Orientation.HORIZONTAL.ordinal());
                    workConfig.getTranslationLocatorMap().put(columns[1], translationLocator);
                }
            }

            if(!pass) {
                throw new IllegalArgumentException("The -l or -locator option must is like path,rnumber|cnumber,number1,number2,v|h, path is excel file path and exist.");
            }
        }

        if (line.hasOption("ot")) {
            String type = line.getOptionValue("ot");
            boolean findType = false;
            StringBuffer stringBuffer = new StringBuffer("[");
            for (GlobalConstant.OutType outType : GlobalConstant.OutType.values()) {
                if(outType.getValue().endsWith(type)) {
                    config.setOutType(outType);
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

        return config;
    }

    private void enterInputMode(WorkConfig config) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            byte[] startBytes = "> ".getBytes();
            System.out.write("\nnow entry input mode, please set the parameters and input 'doWork' cmd to work.\n".getBytes());
            System.out.write("input cmd of 'help' look for help of input mode.\n".getBytes());
            System.out.write(startBytes);
            List<String> notEndCmdLines = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                switch (line.toLowerCase()) {
                    case "help":
                        printInputModeHelp();
                        break;
                    case "doWork":
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
                    LoggerUtil.error(e1.getMessage());
                }
            }
        }
    }

    private void printKeyList() {
        if(translations.size() == 0) {
            System.out.println("not translations");
        }
        for (Translation t : translations) {
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
        System.out.println("add <key,local,translate>; add translation.");
        System.out.println("clear                      clear the screen.");
        System.out.println("del <key>;                 delete key from list.");
        System.out.println("dll                        delete last line of input.");
        System.out.println("exit                       exit tool.");
        System.out.println("help                       look for help of input mode.");
        System.out.println("i <path1,path2...>;        set source.");
        System.out.println("list                       show the list of keys.");
        System.out.println("l                          show the list of keys.");
        System.out.println("o <dir>;                   set out dir.");
        System.out.println("ot <[json|xml]>;           set out type,default is json.");
        System.out.println("doWork                      doWork do work.");
    }


    public static void showProgress(long complete, long total, int psWidth) {
        if(total == 0) {
            return;
        }
        float p = 1.0f * complete / total;
        int c = (int)(psWidth * p);
        int s = psWidth-c;
        String progressText = StringUtil.createStringFromString("█", c)
                + StringUtil.createStringFromString("░", s)
                + String.format(" [%.2f%%]", p*100);
        System.out.print(progressText);
        System.out.print(StringUtil.createStringFromString("\b", progressText.length()));
    }
}
