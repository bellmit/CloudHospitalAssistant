package cn.neuedu.his.controller;

import cn.neuedu.his.model.InspectionApplication;
import cn.neuedu.his.model.Prescription;
import cn.neuedu.his.service.InspectionApplicationService;
import cn.neuedu.his.service.impl.RedisServiceImpl;
import cn.neuedu.his.util.CommonUtil;
import cn.neuedu.his.util.constants.ErrorEnum;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ccm on 2019/05/24.
 */
@RestController
@RequestMapping("/inspection_application")
public class InspectionApplicationController {

    @Autowired
    InspectionApplicationService inspectionApplicationService;
    @Autowired
    RedisServiceImpl redisService;

    /**
     * 暂存检查/处置
     *
     * @param object
     * @return
     */
    @PostMapping("/saveTemporaryInspection")
    public JSONObject saveTemporaryInspection(@RequestBody JSONObject object) {
        Integer id = Integer.parseInt(object.get("registrationId").toString());
        List<InspectionApplication> record = JSONObject.parseArray(object.get("inspections").toString(), InspectionApplication.class);
        List<Prescription> prescriptions = JSONObject.parseArray(object.get("prescriptions").toString(), Prescription.class);
        try {
            redisService.setTemporaryInspection(id, record, prescriptions);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_801);
        }
        return CommonUtil.successJson();
    }


    /**
     * 获得暂存检查/处置
     *
     * @param registrationId
     * @return
     */
    @GetMapping("/getTemporaryInspection/{registrationId}")
    public JSONObject getTemporaryInspection(@PathVariable("registrationId") Integer registrationId) {
        JSONObject object = new JSONObject();
        try {
            List<Prescription> prescriptions = redisService.getTemporaryPrescription(registrationId);
            if (prescriptions == null)
                prescriptions = new ArrayList<>();
            object.put("prescriptions", prescriptions);
            List<InspectionApplication> applications = redisService.getTemporaryApplications(registrationId);
            if (applications == null)
                applications = new ArrayList<>();
            object.put("applications", applications);
            return CommonUtil.successJson(object);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }
    }

    /**
     * 获得暂存病历
     *
     * @param registrationId
     * @return
     */
    @GetMapping("/deleteTemporaryInspection/{registrationId}")
    public JSONObject deleteTemporaryInspection(@PathVariable("registrationId") Integer registrationId) {
        try {
            redisService.deleteTemporaryInspection(registrationId);
            return CommonUtil.successJson();
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_803);
        }
    }
    /**患者查询
     *
     */
}
