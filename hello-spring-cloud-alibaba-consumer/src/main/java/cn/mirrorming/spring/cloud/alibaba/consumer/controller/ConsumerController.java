package cn.mirrorming.spring.cloud.alibaba.consumer.controller;

import cn.mirrorming.spring.cloud.alibaba.consumer.enumeration.CodeMsg;
import cn.mirrorming.spring.cloud.alibaba.consumer.exception.GlobalException;
import cn.mirrorming.spring.cloud.alibaba.consumer.entity.User;
import cn.mirrorming.spring.cloud.alibaba.consumer.entity.UserExcel;
import cn.mirrorming.spring.cloud.alibaba.consumer.utils.ExcelUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author mirror
 */
@RestController
public class ConsumerController {

    private static final int MAX_USER_IMPORT = 1000;

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping(value = "/echo/app/name")
    public String echo() {
        //使用 LoadBalanceClient 和 RestTemplate 结合的方式来访问
        ServiceInstance serviceInstance = loadBalancerClient.choose("provider");
        String url = String.format("http://%s:%s/echo/%s", serviceInstance.getHost(), serviceInstance.getPort(), appName);
        return restTemplate.getForObject(url, String.class);
    }

    @GetMapping("/excel/template")
    public void downloadTemplate(HttpServletResponse response) {
        String fileName = "导入用户模板";
        String sheetName = "导入用户模板";
        List<UserExcel> userList = new ArrayList<>();
        userList.add(new UserExcel("saysky", "言曌", "123456", "847064370@qq.com", "http://xxx.com/xx.jpg", "0", "2017-12-31 12:13:14"));
        userList.add(new UserExcel("qiqi", "琪琪", "123456", "666666@qq.com", "http://xxx.com/xx.jpg", "0", "2018-5-20 13:14:00"));
        try {
            ExcelUtil.writeExcel(response, userList, fileName, sheetName, UserExcel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/excel/export")
    public void exportData(HttpServletResponse response) {
        String fileName = "用户列表";
        String sheetName = "用户列表";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        List<User> userList = userService.findAll();
        List<User> userList = new ArrayList<>();
        List<UserExcel> userExcelList = new ArrayList<>();
        for (User user : userList) {
            UserExcel userExcel = UserExcel.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .avatar(user.getAvatar())
                    .status(String.valueOf(user.getStatus()))
                    .createdTime(dateFormat.format(user.getCreatedTime())).build();
            userExcelList.add(userExcel);
        }
        try {
            ExcelUtil.writeExcel(response, userExcelList, fileName, sheetName, UserExcel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/excel/import")
    public void importData(@RequestParam("file") MultipartFile file, User user) throws ParseException {
        List<UserExcel> userExcelList = null;
        // 1.excel同步读取数据
        try {
            userExcelList = EasyExcel.read(new BufferedInputStream(file.getInputStream())).head(UserExcel.class).sheet().doReadSync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 2.检查是否大于1000条
        if (userExcelList.size() > MAX_USER_IMPORT) {
            throw new GlobalException(CodeMsg.OVER_MAX_USER_IMPORT_LIMIT.fillArgs(MAX_USER_IMPORT));
        }
        // 3.导入校验所有行列格式
        checkImportData(userExcelList);
        // 4.将 userExcelList 转成 userList
        List<User> userList = userExcelList2UserList(userExcelList);
        // 5.入库操作
//        userService.batchInsertOrUpdate(userList);
    }
    public static void checkImportData(List<UserExcel> userExcelList) {
        // 行号从第2行开始
        int rowNo = 2;
        // 遍历校验所有行和列
        for (UserExcel userExcel : userExcelList) {
            // 1.校验用户名
            String username = userExcel.getUsername();
            if (StringUtils.isEmpty(username)) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_IS_EAMPTY.fillArgs(rowNo, "用户名"));
            }
            if (username.length() > 20 || username.length() < 4) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_FORMAT_ERROR.fillArgs(rowNo, "用户名"));
            }
            // 2.校验密码
            String password = userExcel.getPassword();
            if (StringUtils.isEmpty(password)) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_IS_EAMPTY.fillArgs(rowNo, "密码"));
            }
            if (password.length() > 100 || password.length() < 6) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_FORMAT_ERROR.fillArgs(rowNo, "密码"));
            }
            // 3.校验昵称
            String nickname = userExcel.getNickname();
            if (StringUtils.isEmpty(nickname)) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_IS_EAMPTY.fillArgs(rowNo, "昵称"));
            }
            if (nickname.length() > 20 || nickname.length() < 2) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_FORMAT_ERROR.fillArgs(rowNo, "昵称"));
            }
            // 4.校验Email
            String email = userExcel.getEmail();
            if (StringUtils.isEmpty(email)) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_IS_EAMPTY.fillArgs(rowNo, "邮箱"));
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_FORMAT_ERROR.fillArgs(rowNo, "邮箱"));
            }
            // 5.校验状态
            String status = userExcel.getStatus();
            if (StringUtils.isEmpty(status)) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_IS_EAMPTY.fillArgs(rowNo, "状态"));
            }
            if (!"0".equals(status) && !"1".equals(status)) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_FORMAT_ERROR.fillArgs(rowNo, "状态"));
            }
            // 6.校验注册时间
            String createdTime = userExcel.getCreatedTime();
            if (StringUtils.isEmpty(createdTime)) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_IS_EAMPTY.fillArgs(rowNo, "注册时间"));
            }
            try {
                DATE_TIME_FORMAT.parse(createdTime);
            } catch (ParseException e) {
                throw new GlobalException(CodeMsg.IMPORT_FIELD_FORMAT_ERROR.fillArgs(rowNo, "注册时间"));
            }
        }
    }
    /**
     * userExcelList转成userList
     *
     * @param userExcelList
     * @return
     */
    private List<User> userExcelList2UserList(List<UserExcel> userExcelList) throws ParseException {
        Date now = new Date();
        List<User> userList = new ArrayList<>();
        for (UserExcel userExcel : userExcelList) {
            User user = User.builder()
                    .username(userExcel.getUsername())
                    .password(userExcel.getPassword())
                    .nickname(userExcel.getNickname())
                    .email(userExcel.getEmail())
                    .avatar(userExcel.getAvatar())
                    .status(Integer.valueOf(userExcel.getStatus()))
                    .createdTime(DATE_TIME_FORMAT.parse(userExcel.getCreatedTime())).build();
            user.setCreatedBy("import");
            user.setUpdatedBy("import");
            user.setUpdatedTime(now);
            userList.add(user);
        }
        return userList;
    }

    @PostMapping("/excel/importMore")
    public void importDataByMoreSheet(MultipartFile file) throws ParseException, IOException {
        List<UserExcel> userExcelList = new ArrayList<>();
        // 1.excel同步读取数据
        try {
            ExcelReader excelReader = EasyExcel.read(new BufferedInputStream(file.getInputStream())).head(UserExcel.class).build();
            List<ReadSheet> sheetList = excelReader.excelExecutor().sheetList();
            List<UserExcel> childUserExcelList = new ArrayList<>();
            for (ReadSheet sheet : sheetList) {
                childUserExcelList = EasyExcel.read(new BufferedInputStream(file.getInputStream())).head(UserExcel.class).sheet(sheet.getSheetNo()).doReadSync();
            }
            userExcelList.addAll(childUserExcelList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 2.检查是否大于1000条
        if (userExcelList.size() > MAX_USER_IMPORT) {
            throw new GlobalException(CodeMsg.OVER_MAX_USER_IMPORT_LIMIT.fillArgs(MAX_USER_IMPORT));
        }
        // 3.导入校验所有行列格式
        checkImportData(userExcelList);
        // 4.将 userExcelList 转成 userList
        List<User> userList = userExcelList2UserList(userExcelList);
        // 5.入库操作
//        userService.batchInsertOrUpdate(userList);
    }
}