package sql.generator.hackathon.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import sql.generator.hackathon.model.ColumnInfo;

@Service
public class ExcelExporter {
	private  Map<String, List<List<ColumnInfo>>> dataList ;

    private  HSSFCellStyle createHeaderRow(HSSFWorkbook workbook) {
        //dataList = new HashMap<>();
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;

    }

    public HSSFWorkbook createEex(Map<String, List<List<ColumnInfo>>> dataList)  {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFCellStyle style = createHeaderRow(workbook);
        HSSFFont font = workbook.createFont();
        style.setFont(font);

        dataList.entrySet().forEach(entry -> {

                HSSFSheet sheet = workbook.createSheet(entry.getKey());

                    List<List<ColumnInfo>> isNameTable= entry.getValue();

                            System.out.println("reocrd"+ isNameTable.size());
                            int rownum = 0;
                            for (List<ColumnInfo> imtem :isNameTable ){
                                Row  row = sheet.createRow(rownum);
                                System.out.println(rownum);
                                if (rownum == 0){
                                    createheader(sheet,imtem,row,style);
                                    rownum++;
                                    row = sheet.createRow(rownum);
                                    createValiheader(sheet,imtem,row,style);
                                }else{
                                    createRow(sheet,imtem,row,style);

                                }
                                rownum++;
                            }
            });

        return workbook;
    }
    private void createRow(Sheet sheet, List<ColumnInfo> isNameTable,Row  row,HSSFCellStyle style ) {
        Cell cell;
        for (int i = 0 ; i <  isNameTable.size() ;i ++){
                cell = row.createCell(i, CellType.STRING);
                cell.setCellValue(isNameTable.get(i).getVal());
        }
    }

    private void createheader(Sheet sheet, List<ColumnInfo> isNameTable,Row  row,HSSFCellStyle style ){
            Cell cell;
            for (int i = 0 ; i < isNameTable.size() ;i ++) {
                cell = row.createCell(i, CellType.STRING);
                cell.setCellValue(isNameTable.get(i).getName());
                cell.setCellStyle(style);
            }
    }
    private void createValiheader(Sheet sheet, List<ColumnInfo> isNameTable,Row  row,HSSFCellStyle style ){
        Cell cell;
        for (int i = 0 ; i <   isNameTable.size() ;i ++) {
            cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(isNameTable.get(i).val);
            cell.setCellStyle(style);
            System.out.println(isNameTable.get(i).val + "header");
        }
    }

	public byte[] outputFieSql(List<String> inputSQL) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			for (String x : inputSQL) {
				bos.write(x.getBytes());
				bos.write("\n".getBytes());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
	
    public FileOutputStream outputFile(String fileName) throws FileNotFoundException {
       // HSSFWorkbook workbook =  this.createEex();

        File file = null;
        FileOutputStream outFile =null;
            try {
                    file= new File(fileName);
                    outFile = new FileOutputStream(file);
                    file.getParentFile().mkdirs();
                    //workbook.write(outFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
        System.out.println("Created file: " + file.getAbsolutePath());
        return  outFile;

    }
    public FileOutputStream outputFileSQL(ServiceParse serviceParse, String fileName) throws FileNotFoundException{
        File file = null;
        FileOutputStream outFile =null;
        try {
            file= new File(fileName);
            outFile = new FileOutputStream(file);
            file.getParentFile().mkdirs();
         //   serviceParse.dataToSqlInsert();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  outFile;
    }
}
