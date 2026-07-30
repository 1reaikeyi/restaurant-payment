package start.controller.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import common.excel.UserStatistics;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojo.entity.User;
import service.ISevcive.UserService;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("report")
public class ExcelReportController {
    @Autowired
    private UserService userService;

    /**
     * write
     * @return
     */
    @GetMapping("/excel/export")
    public Result dowrite() {
        File file = new File("云/excel/report.xlsx");
        String filePath = file.getAbsolutePath();
        File checkFile = new File(filePath);
        if (!checkFile.exists()) {
            checkFile.mkdirs();
        }
        System.out.println("文件路径::" + filePath);
        List<User> userList = userService.list();
        List<UserStatistics> userStatisticsList = userList.stream()
                .map(user -> UserStatistics.builder()
                        .id(user.getId())
                        .name(user.getUserName())
                        .build())
                .toList();
        ExcelWriter excelWriter = EasyExcel.write(filePath, UserStatistics.class).build();
        WriteSheet writeSheet = EasyExcel.writerSheet("用户信息表").build();
        excelWriter.write(userStatisticsList, writeSheet);
        // 完成写入
        excelWriter.finish();
        return Result.success(filePath);
    }
    /**
     * 读取
     */
    @PostMapping("/excel/read")
    public Result doread() {
        File file = new File("云/excel/report.xlsx");
        String filePath = file.getAbsolutePath();

        List<UserStatistics> dataList = new ArrayList<>();
        EasyExcel.read(filePath, UserStatistics.class, new AnalysisEventListener<UserStatistics>() {
            @Override
            public void invoke(UserStatistics data, AnalysisContext context) {
                if (data.getId() == null) {
                    throw new RuntimeException("文件为null");
                }
                dataList.add(data);
            }
            @Override
            public void onException(Exception exception, AnalysisContext context) {
                System.out.println("解析异常，跳过该行: " + exception.getMessage());
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("读取完成，共 " + dataList.size() + " 条数据");
            }
        }).sheet().doRead();
       return Result.success(dataList);
    }
}
