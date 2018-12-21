package util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    public static final int Excel2003 = 0;
    public static final int Excel2007 = 1;
    public static final String  SUFFIX_XLS = "xls";
    public static final String  SUFFIX_XLSX = "xlsx";

    public static Workbook getWorkbook(File file) throws IOException {
        int edition = -1;
        if(file == null) {
            throw new IllegalArgumentException("invalid file");
        }
        if(file.getAbsoluteFile().getName().endsWith(SUFFIX_XLS)) {
            edition = Excel2003;
        } else if(file.getAbsoluteFile().getName().endsWith(SUFFIX_XLSX)) {
            edition = Excel2007;
        } else {
            throw new IllegalArgumentException("invalid file");
        }

        if(!file.exists() || !file.isFile()) {
            throw new IOException("no such file with specified path"+file.getAbsoluteFile());
        }
        return getWorkbook(edition, new FileInputStream(file));
    }

    /**
     *
     * @param edition
     * @param in
     * @return
     * @throws IOException
     */
    public static Workbook getWorkbook(int edition, InputStream in) throws IOException {
        if (edition == 0) {
            return new HSSFWorkbook(in);
        } else if (edition == 1) {
            return new XSSFWorkbook(in);
        }
        return null;
    }

    /**
     *
     * @param workbook
     * @param startRow
     * @param startCol
     * @param indexSheet
     * @return
     */
    public static List<List<String>> getExcelString(Workbook workbook, int startRow, int startCol, int indexSheet) {
        List<List<String>> stringTable = new ArrayList<List<String>>();
        // 获取指定表对象
        Sheet sheet = workbook.getSheetAt(indexSheet);
        // 获取最大行数
        int rowNum = sheet.getLastRowNum();
        for (int i = startRow; i <= rowNum; i++) {
            List<String> oneRow = new ArrayList<String>();
            Row row = sheet.getRow(i);
            // 根据当前指针所在行数计算最大列数
            int colNum = row.getLastCellNum();
            for (int j = startCol; j <= colNum; j++) {
                // 确定当前单元格
                Cell cell = row.getCell(j);
                String cellValue = null;
                if (cell != null) {
                    // 验证每一个单元格的类型
                    switch (cell.getCellTypeEnum()) {
                        case NUMERIC:
                            // 表格中返回的数字类型是科学计数法因此不能直接转换成字符串格式
                            cellValue = new BigDecimal(cell.getNumericCellValue()).toPlainString();
                            break;
                        case STRING:
                            cellValue = cell.getStringCellValue();
                            break;
                        case FORMULA:
                            cellValue = new BigDecimal(cell.getNumericCellValue()).toPlainString();
                            break;
                        case BLANK:
                            cellValue = "";
                            break;
                        case BOOLEAN:
                            cellValue = Boolean.toString(cell.getBooleanCellValue());
                            break;
                        case ERROR:
                            cellValue = "ERROR";
                            break;
                        default:
                            cellValue = "UNDEFINE";
                    }
                } else {
                    cellValue = "";
                }
                // 生成一行数据
                oneRow.add(cellValue);
            }
            stringTable.add(oneRow);
        }
        return stringTable;
    }
}
