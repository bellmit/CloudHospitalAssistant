package cn.neuedu.his.controller;

import cn.neuedu.his.mapper.DepartmentMapper;
import cn.neuedu.his.model.ConstantVariable;
import cn.neuedu.his.model.Department;
import cn.neuedu.his.model.DepartmentKind;
import cn.neuedu.his.model.User;
import cn.neuedu.his.service.ConstantVariableService;
import cn.neuedu.his.service.DepartmentKindService;
import cn.neuedu.his.service.DepartmentService;
import cn.neuedu.his.service.UserService;
import cn.neuedu.his.service.impl.RedisServiceImpl;
import cn.neuedu.his.util.CommonUtil;
import cn.neuedu.his.util.PermissionCheck;
import cn.neuedu.his.util.constants.ErrorEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by ccm on 2019/05/24.
 */
@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    DepartmentService departmentService;
    @Autowired
    DepartmentKindService departmentKindService;
    @Autowired
    ConstantVariableService constantVariableService;
    @Autowired
    RedisServiceImpl redisService;
    @Autowired
    DepartmentMapper departmentMapper;
    @Autowired
    UserService userService;

    @GetMapping("/getByKind/{kindId}")
    public JSONObject getByKind(@PathVariable("kindId") Integer kindId) {
        List<Department> departments = departmentService.getByKindId(kindId);
        if (departments == null) {
            departments = new ArrayList<>();
        }
        return CommonUtil.successJson(departments);
    }

    /**
     * 获得部门的详细信息
     *
     * @return
     */
    @GetMapping("/get")
    public JSONObject getDepartmentInformation() {
        List<Department> departments = departmentService.getDepartmentInformation();
        if (departments == null) {
            departments = new ArrayList<>();
        }
        return CommonUtil.successJson(departments);

    }

    @PostMapping("/delete/{id}")
    public JSONObject deleteDepartmentInformation(@PathVariable("id") Integer id, Authentication authentication) {

        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }
        try {
            departmentService.deleteDepartmentInformation(id);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("610"))
                return CommonUtil.errorJson(ErrorEnum.E_610);
            else if (e.getMessage().equals("636"))
                return CommonUtil.errorJson(ErrorEnum.E_636);
            else if (e.getMessage().equals("637"))
                return CommonUtil.errorJson(ErrorEnum.E_637);
        }
        return CommonUtil.successJson();
    }

    @PostMapping("/add")
    public JSONObject addDepartment(@RequestBody JSONObject jsonObject, Authentication authentication) {

        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }

        try {
            Department department = JSONObject.toJavaObject(jsonObject, Department.class);
            departmentService.addDepartment(department);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("611"))
                return CommonUtil.errorJson(ErrorEnum.E_611);
            else if (e.getMessage().equals("612"))
                return CommonUtil.errorJson(ErrorEnum.E_612);
        }
        return CommonUtil.successJson();
    }

    @PostMapping("/modify")
    public JSONObject modifyDepartment(@RequestBody JSONObject jsonObject, Authentication authentication) {
        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }
        try {
            Department department = JSONObject.toJavaObject(jsonObject, Department.class);
            departmentService.modifyDepartment(department);
        } catch (Exception e) {
            switch (e.getMessage()) {
                case "611":
                    return CommonUtil.errorJson(ErrorEnum.E_611);
                case "612":
                    return CommonUtil.errorJson(ErrorEnum.E_612);
                case "802":
                    return CommonUtil.errorJson(ErrorEnum.E_802);
            }
        }
        return CommonUtil.successJson();
    }

    @PostMapping("/retreat/{id}")
    public JSONObject modifyDepartment(@PathVariable("id") Integer id, Authentication authentication) {
        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }

        try {
            departmentService.retreatDepartment(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CommonUtil.successJson();
    }

    @GetMapping("/getAllDepartmentKind")
    public JSONObject getAllDepartmentMatchKind() {
        try {
            //获得科室大类
            List<ConstantVariable> constantVariables = constantVariableService.getDepartmentType();
            JSONObject returnJSON = new JSONObject();
            returnJSON.put("type", constantVariables);

            JSONArray departments = new JSONArray();
            //获得分别与大类对应的科室信息
            constantVariables.forEach(kind -> {
                departments.add(departmentService.getAllDepartmentInformationByClassificationId(kind.getId()));
            });
            //{kind:[],depas:[[],[]] }
            returnJSON.put("departments", departments);
            return CommonUtil.successJson(returnJSON);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("数据库连接"));
        }
    }

    @GetMapping({"/getDepartmentList/{name}", "/getDepartmentList/"})
    public JSONObject getDepartmentListByname(@PathVariable(name = "name", required = false) String name, Authentication authentication) {
        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }

        try {
            if (name == null)
                name = "";
            List<Department> departments = departmentService.getDepartmentListByName(name);
            return CommonUtil.successJson(departments);
        } catch (RuntimeException e) {
            return CommonUtil.errorJson(ErrorEnum.E_500);
        }
    }

    /**
     * 临床科室工作量统计
     *
     * @param jsonObject
     * @param authentication
     * @return
     */
    @PostMapping("/departmentClinicWorkload")
    public JSONObject getClinicDepartmentWorkLoad(@RequestBody JSONObject jsonObject, Authentication authentication) {
        try {
            PermissionCheck.isFinancialOfficer(authentication);
        } catch (AuthenticationServiceException a) {
            return CommonUtil.errorJson(ErrorEnum.E_502.addErrorParamName(a.getMessage()));
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }

        Date end = jsonObject.getDate("end");
        if (end == null)
            end = new Date(System.currentTimeMillis());

        try {
            Map<String, Integer> map = redisService.getMapAll("departmentType");
            return CommonUtil.successJson(departmentService.workCalculate(map.get("临床科室"), jsonObject.getDate("start"), end));
        } catch (IllegalArgumentException e) {
            return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName(e.getMessage()));
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }
    }

    /**
     * 医技科室工作量统计
     *
     * @param jsonObject
     * @param authentication
     * @return
     */
    @PostMapping("/departmentTechniqueWorkload")
    public JSONObject getTechniqueDepartmentWorkLoad(@RequestBody JSONObject jsonObject, Authentication authentication) {
        try {
            PermissionCheck.isFinancialOfficer(authentication);
        } catch (AuthenticationServiceException a) {
            return CommonUtil.errorJson(ErrorEnum.E_502.addErrorParamName(a.getMessage()));
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }

        Date end = jsonObject.getDate("end");
        if (end == null)
            end = new Date(System.currentTimeMillis());

        try {
            Map<String, Integer> map = redisService.getMapAll("departmentType");
            return CommonUtil.successJson(departmentService.workCalculate(map.get("医技科室"), jsonObject.getDate("start"), end));
        } catch (IllegalArgumentException e) {
            return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName(e.getMessage()));
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }
    }



}
