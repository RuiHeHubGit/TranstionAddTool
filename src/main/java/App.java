import addit.Addit;
import constant.GlobalConstant;
import org.apache.commons.cli.*;
import org.apache.poi.ss.usermodel.Workbook;
import ui.UI;
import util.ExcelUtil;
import util.PID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class App {
    private Options opts = new Options();

    public static void main(String[] args) {
        App app = new App();
        app.start(args);
//        app.test();
    }

    private void test() {
        try {
            Workbook workbook = ExcelUtil.getWorkbook(new File("C:\\Users\\ruihe\\Desktop\\translation\\translation\\ro.lb.purchasefee_cyclefee_month_details.xlsx"));
            List<List<String>> data = ExcelUtil.getExcelString(workbook, 0, 0, 0);
            System.out.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start(String[] args) {
        if(PID.isRunCMD()) {
            definedOptions();
            Addit.start(parseOptions(args));
        } else {
            UI.startUI();
        }
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
    private HashMap<String, Object> parseOptions(String[] args) {
        HashMap<String, Object> params = getDefaultParams();
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        // 解析命令行参数
        try {
            line = parser.parse(opts, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (args == null || args.length == 0 || line.hasOption("h")) {
            HelpFormatter help = new HelpFormatter();
            help.printHelp("translate addi", opts);
        }

        if(line.hasOption("v")) {
            System.out.println("The version is 1.0 of translate addi.");
            System.exit(0);
        }

        if (line.hasOption("i")) {
            String[] paths = line.getOptionValues("i");
            List<File> files = new ArrayList<File>();
            for (String path : paths) {
                File file = new File(path);
                if (!file.exists() || !file.isDirectory()) {
                    System.err.println("The -i or -input option must is specify dir or path of file.");
                    System.exit(1);
                } else {
                    files.add(file);
                }
            }
            params.put(GlobalConstant.PARAM_KEY.INPUT, files);
        }

        if (line.hasOption("o")) {
            String path = line.getOptionValue("i");
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("The -o or -out option must is specify dir.");
                System.exit(1);
            }
            params.put(GlobalConstant.PARAM_KEY.OUTPUT, file);
        }

        if (line.hasOption("cs")) {
            String[] columns = line.getOptionValues("cs");
            if (columns.length != 3) {
                System.err.println("The -cs or -columns option must is 3 number and separator sign of ','.");
                System.exit(1);
            }
            for (String f : columns) {
                System.out.println(f);
            }
        }

        if (line.hasOption("kc")) {
            try {
                params.put(GlobalConstant.PARAM_KEY.KEY_COLUMN, Integer.parseInt(line.getOptionValue("kc")));
            } catch (Exception e) {
                System.err.println("invalid key-column value on kc");
                System.exit(1);
            }
        }

        if (line.hasOption("lc")) {
            try {
                params.put(GlobalConstant.PARAM_KEY.LOCAL_COLUMN, Integer.parseInt(line.getOptionValue("lc")));
            } catch (Exception e) {
                System.err.println("invalid local-column value on kc");
                System.exit(1);
            }
        }

        if (line.hasOption("tc")) {
            try {
                params.put(GlobalConstant.PARAM_KEY.TRANSLATE_COLUMN, Integer.parseInt(line.getOptionValue("tc")));
            } catch (Exception e) {
                System.err.println("invalid translate-column value on kc");
                System.exit(1);
            }
        }

        if (line.hasOption("ot")) {
            String type = line.getOptionValue("tc");
            GlobalConstant.OUT_TYPE outType = GlobalConstant.OUT_TYPE.valueOf(type);
            if(outType != null) {
                params.put(GlobalConstant.PARAM_KEY.OUT_TYPE, GlobalConstant.OUT_TYPE.valueOf(type));
            } else {
                System.err.println("invalid translate-column value on kc");
                System.exit(1);
            }
        }

        if(line.hasOption("vt")) {
            params.put(GlobalConstant.PARAM_KEY.VERTICAL, true);
        }

        return params;
    }

    private HashMap<String, Object> getDefaultParams() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        List<File> files = new ArrayList<File>();
        for (String path : GlobalConstant.PATH.INPUT) {
            files.add(new File(path));
        }
        params.put(GlobalConstant.PARAM_KEY.INPUT, files);

        params.put(GlobalConstant.PARAM_KEY.OUTPUT, new File(GlobalConstant.PATH.OUTPUT));

        params.put(GlobalConstant.PARAM_KEY.OUT_TYPE, GlobalConstant.OUT_TYPE.TYPE_JSON);

        params.put(GlobalConstant.PARAM_KEY.VERTICAL, false);

        return params;
    }
}
