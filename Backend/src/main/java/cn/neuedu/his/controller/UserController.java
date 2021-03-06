package cn.neuedu.his.controller;

import cn.neuedu.his.model.Doctor;
import cn.neuedu.his.model.User;
import cn.neuedu.his.service.DepartmentService;
import cn.neuedu.his.service.DoctorService;
import cn.neuedu.his.service.UserService;
import cn.neuedu.his.service.impl.RedisServiceImpl;
import cn.neuedu.his.util.CommonUtil;
import cn.neuedu.his.util.PermissionCheck;
import cn.neuedu.his.util.constants.ErrorEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by ccm on 2019/05/24.
 */
@RestController
@RequestMapping("/user")
public class UserController {
    class url {
        String name;
        String url;
        String key;

        url(String name, String url, String key) {
            this.name = name;
            this.url = url;
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getKey() {
            return key;
        }
    }

    private Logger logger = Logger.getLogger(UserController.class);

    @Autowired
    UserService userService;
    @Autowired
    DoctorService doctorService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RedisServiceImpl redisService;

    @GetMapping("/getDocByDept/{deptId}")
    public JSONObject getDocByDept(@PathVariable("deptId") Integer deptId) {
        Map<String, Integer> map2 = null;
        try {
            map2 = redisService.getMapAll("userType");
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_638);
        }
        Integer typeId = map2.get("门诊医生");
        List<User> users = userService.getUserWithDocByDept(typeId, deptId);
        return CommonUtil.successJson(users);
    }


    @GetMapping("/function")
    public JSONObject fuction(Authentication authentication) throws Exception {
        Map<String, Object> data = (Map<String, Object>) authentication.getCredentials();
        Integer typeId = (Integer) data.get("typeId");
        JSONArray urls = new JSONArray();
        Map<String, Integer> map = redisService.getMapAll("userType");
        if (typeId.equals(map.get("医院管理员"))) {//606
            urls.add(new url("账户管理", "/admin/user", "admin"));
            urls.add(new url("排班管理", "/admin/rule", "rule"));
            urls.add(new url("医疗信息管理", "/admin/other", "other"));
        } else if (typeId.equals(map.get("门诊医生"))) {//602
            urls.add(new url("看诊", "/doctor/index", "doctor"));
            urls.add(new url("个人工作量","/doctor/statistics","statistics"));
        } else if (typeId.equals(map.get("挂号收费员"))) {//601
            urls.add(new url("挂号", "/patient/register", "register"));
            urls.add(new url("缴费", "/patient/charge", "charge"));
            urls.add(new url("日结", "/patient/make", "make"));
        } else if (typeId.equals(map.get("财务管理员"))) { //605
            urls.add(new url("门诊财务管理", "/finance/manage", "manage"));
            urls.add(new url("日结核对", "/finance/check", "check"));
            urls.add(new url("工作量统计", "/finance/workload", "workload"));
        } else if (typeId.equals(map.get("药房操作员"))) {
            urls.add(new url("药房管理", "/medicine/index", "medicine"));
        } else if (typeId.equals(map.get("医技医生"))) {
            urls.add(new url("检查", "/inspection/index", "inspection"));
        }
        return CommonUtil.successJson(urls);
    }

    /**
     * 注册用户信息
     *
     * @param jsonObject 用户信息
     * @return 是否成功
     */
    @PostMapping("/register")
    public JSONObject register(@RequestBody JSONObject jsonObject, Authentication authentication) throws Exception {

        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_502);
        }

        User user = JSONObject.toJavaObject(jsonObject, User.class);
        //创立医生对象
        Doctor doctor = JSONObject.toJavaObject(jsonObject, Doctor.class);

        try {
            userService.insertUser(user, doctor);
        } catch (RuntimeException e) {
            switch (e.getMessage()) {
                case "501.1":
                    return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("身份证信息"));
                case "600":
                    return CommonUtil.errorJson(ErrorEnum.E_600);
                case "501.2":
                    return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("部门"));
                case "501.3":
                    return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("用户类别"));
                case "501.4":
                    return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("医生职称"));
                default:
                    logger.error(e);
                    return CommonUtil.errorJson(ErrorEnum.E_500);
            }
        }
        return CommonUtil.successJson();
    }

    /**
     * 管理员删除信息
     *
     * @param id
     * @param authentication
     * @return
     */
    @PostMapping("/delete/{id}")
    public JSONObject deleteUserInformation(@PathVariable("id") Integer id, Authentication authentication) throws Exception {

        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_502);
        }

        try {
            userService.deleteUser(id);
        } catch (Exception e) {
            if (e.getMessage().equals("601"))
                return CommonUtil.errorJson(ErrorEnum.E_601);
        }

        return CommonUtil.successJson();
    }

    /**
     * 修改个人信息（个人级别）
     *
     * @param jsonObject
     * @param authentication
     * @return
     */
    @PostMapping("/modify")
    public JSONObject modifyUserInformation(@RequestBody JSONObject jsonObject, Authentication authentication) throws Exception {


        User user = JSONObject.toJavaObject(jsonObject, User.class);
        Doctor doctor = JSONObject.toJavaObject(jsonObject, Doctor.class);

        //是否是个人账号
        try {
            PermissionCheck.isIndivual(authentication, user.getUsername());
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }
        user.setId(PermissionCheck.getIdByUser(authentication));
        return updateUser(user, doctor);
    }

    private JSONObject updateUser(User user, Doctor doctor) {
        try {
            userService.updateUser(user, doctor);
        } catch (Exception e) {
            switch (e.getMessage()) {
                case "600":
                    return CommonUtil.errorJson(ErrorEnum.E_600);
                case "802":
                    return CommonUtil.errorJson(ErrorEnum.E_802);
                case "501.1":
                    return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("用户类型"));
                case "501.2":
                    return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("身份信息"));
                case "501.3":
                    return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("医生职称"));
                default:
                    logger.error(e);
                    return CommonUtil.errorJson(ErrorEnum.E_500);
            }

        }

        return CommonUtil.successJson();
    }


    /**
     * 超级管理员修改信息
     *
     * @param jsonObject     传入的数据比个人传入多一个id
     * @param authentication
     * @return
     */
    @PostMapping("/adminModify")
    public JSONObject adminModifyUserInformation(JSONObject jsonObject, Authentication authentication) throws Exception {

        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_502);
        }

        User user = JSONObject.toJavaObject(jsonObject, User.class);
        Doctor doctor = JSONObject.toJavaObject(jsonObject, Doctor.class);

        return updateUser(user, doctor);
    }

    /**
     * 查询个人信息（个人级别）
     *
     * @param username
     * @param authentication
     * @return
     */
    @GetMapping("/selectUser/{username}")
    public JSONObject selectUserInformation(@PathVariable("username") String username, Authentication
            authentication) throws Exception {

        //是否是个人账号
        try {
            PermissionCheck.isIndivual(authentication, username);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }

        User user = userService.getUserByUsername(username);

        if (user == null)
            return CommonUtil.errorJson(ErrorEnum.E_601);

        Map<String, Integer> map = redisService.getMapAll("doctor");

        if (map.values().contains(user.getTypeId()))
            user = userService.getUserAllInformationByName(username);

        return CommonUtil.successJson(user);
    }


    /**
     * 查询个人信息（管理员级别）
     *
     * @param username
     * @param authentication
     * @return
     */
    @GetMapping("/adminSelectUser/{username}")
    public JSONObject adminSelectUserInformation(@PathVariable("username") String username, Authentication
            authentication) throws Exception {

        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }
        User user = userService.getUserByUsername(username);

        if (user == null)
            return CommonUtil.errorJson(ErrorEnum.E_601);

        Map<String, Integer> map = redisService.getMapAll("doctor");
        if (map.values().contains(user.getTypeId()))
            user = userService.getUserAllInformationByName(username);

        return CommonUtil.successJson(user);
    }

    /**
     * 模糊搜索用户
     *
     * @param name
     * @param authentication
     * @return
     */
    @GetMapping("/findUser/{name}")
    public JSONObject findUser(@PathVariable("name") String name, Authentication authentication) {

        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }

        try {
            List<User> users = userService.findUser(name);
            return CommonUtil.successJson(users);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return CommonUtil.errorJson(ErrorEnum.E_500);
        }
    }

    @GetMapping("/findAll")
    public JSONObject findAll() {
        try {
            List<User> users = userService.findAllWithName();
            return CommonUtil.successJson(users);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return CommonUtil.errorJson(ErrorEnum.E_500);
        }
    }

    @GetMapping("/getAllTollKeeper")
    public JSONObject getAllTollKeeper(Authentication authentication) {
        try {
            PermissionCheck.isFinancialOfficer(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }

        try {
            return CommonUtil.successJson(userService.findAllTollKeeper());
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }

    }

    @GetMapping("/getDoctorByDept")
    public JSONObject getDoctorByDept(@Param("deptId") Integer deptId) {
        Map<String, Integer> map2 = null;
        try {
            map2 = redisService.getMapAll("userType");
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_638);
        }
        Integer typeId = map2.get("门诊医生");
        List<User> list = userService.getUserWithDocByDept(typeId, deptId);
        if (list == null) {
            list = new ArrayList<>();
        }
        return CommonUtil.successJson(list);
    }
}
