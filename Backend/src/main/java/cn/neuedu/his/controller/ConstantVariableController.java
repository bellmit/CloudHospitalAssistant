package cn.neuedu.his.controller;

import cn.neuedu.his.model.ConstantVariable;
import cn.neuedu.his.service.ConstantVariableService;
import cn.neuedu.his.service.impl.RedisServiceImpl;
import cn.neuedu.his.util.CommonUtil;
import cn.neuedu.his.util.PermissionCheck;
import cn.neuedu.his.util.constants.ErrorEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by ccm on 2019/05/24.
 */
@RestController
@RequestMapping("/constant_variable")
public class ConstantVariableController {

    @Autowired
    ConstantVariableService constantVariableService;
    @Autowired
    RedisServiceImpl redisService;

    @GetMapping("/get")
    public JSONObject get(Authentication authentication) {
        Integer userId = PermissionCheck.getIdByUser(authentication);
        return CommonUtil.successJson(constantVariableService.findAll());
    }
    /**
     * 增加常量小类(type 及以后)
     *
     * @param jsonObject
     * @param authentication
     * @return
     */
    @PostMapping("/insert")
    public JSONObject insertConstant(@RequestBody JSONObject jsonObject, Authentication authentication) {
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }
        Map<String, Integer> map = null;
        try {
            map = redisService.getMapAll("typeKind");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(jsonObject.get("type"))) {
                    String type = entry.getKey();
                    ConstantVariable constantVariable = JSONObject.toJavaObject(jsonObject, ConstantVariable.class);
                    constantVariableService.insertConstant(constantVariable, type);
                    break;
                }
            }
        } catch (RuntimeException e) {
            if (e.getMessage().equals("629"))
                return CommonUtil.errorJson(ErrorEnum.E_629);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }
        return CommonUtil.successJson();
    }

    @PostMapping("/modify")
    public JSONObject modifyConstant(@RequestBody JSONObject jsonObject, Authentication authentication) {
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }

        Map<String, Integer> map = null;
        try {
            map = redisService.getMapAll("typeKind");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(jsonObject.get("type"))) {
                    String type = entry.getKey();
                    ConstantVariable constantVariable = JSONObject.toJavaObject(jsonObject, ConstantVariable.class);
                    constantVariableService.modifyConstant(constantVariable, type);
                    break;
                }
            }
        } catch (RuntimeException e) {
            if (e.getMessage().equals("629"))
                return CommonUtil.errorJson(ErrorEnum.E_629);
            else if (e.getMessage().equals("633"))
                return CommonUtil.errorJson(ErrorEnum.E_633);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }
        return CommonUtil.successJson();
    }

    @GetMapping("/delete/{id}")
    public JSONObject deleteConstant(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_602);
        }

        Map<String, Integer> map = null;
        try {
            map = redisService.getMapAll("typeKind");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ConstantVariable constantVariable = constantVariableService.findById(id);
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(constantVariable.getType())) {
                    String type = entry.getKey();
                    constantVariableService.deleteConstant(id, type);
                    break;
                }
            }
        }catch (RuntimeException e){
            if (e.getMessage().equals("633"))
                return CommonUtil.errorJson(ErrorEnum.E_633);
            else if (e.getMessage().equals("629"))
                return CommonUtil.errorJson(ErrorEnum.E_629);
        }
        return CommonUtil.successJson();
    }

    @GetMapping("/getSettlementType")
    public JSONObject getSettlementType() {
        try {
            Map<String, Integer> map = redisService.getMapAll("settlementType");
            JSONArray array = new JSONArray();
            map.forEach((k, v) -> {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", v);
                        jsonObject.put("name", k);
                        array.add(jsonObject);
                    }
            );
            return CommonUtil.successJson(array);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_500);
        }
    }

    @GetMapping("/getForm")
    public JSONObject getDrugForm() {
        try {
            Map<String, Integer> map = redisService.getMapAll("formulation");
            ArrayList<Map> list = new ArrayList<>();
            for (String string : map.keySet()) {
                Map map1 = new HashMap();
                map1.put("name", string);
                map1.put("id", map.get(string));
                list.add(map1);
            }
            return CommonUtil.successJson(list);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }
    }

    @GetMapping("/getType/{type}")
    public JSONObject getConstantByType(@PathVariable("type") Integer typeId, Authentication authentication) {


        try {
            List<ConstantVariable> list = new ArrayList<>();
            String type = null;
            Map<String, Integer> map = redisService.getMapAll("typeKind");
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(typeId)) {
                    type = entry.getKey();
                    break;
                }
            }
            if (type.equals(null))
                return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("类型"));

            map = redisService.getMapAll(type);
            ConstantVariable constantVariable;

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                constantVariable = new ConstantVariable();
                constantVariable.setName(entry.getKey());
                constantVariable.setId(entry.getValue());
                list.add(constantVariable);
            }
            return CommonUtil.successJson(list);

        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }
    }


    @GetMapping("/getAll")
    public JSONObject getAll() {

        List<ConstantVariable> constantVariables = constantVariableService.findAll();
        return CommonUtil.successJson(constantVariables);
    }

}
