package cn.neuedu.his.controller;

import cn.neuedu.his.model.PaymentType;
import cn.neuedu.his.service.PaymentTypeService;
import cn.neuedu.his.service.impl.RedisServiceImpl;
import cn.neuedu.his.util.CommonUtil;
import cn.neuedu.his.util.PermissionCheck;
import cn.neuedu.his.util.constants.ErrorEnum;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Created by ccm on 2019/05/24.
 */
@RestController
@RequestMapping("/payment_type")
public class PaymentTypeController {

    @Autowired
    PaymentTypeService paymentTypeService;
    @Autowired
    RedisServiceImpl redisService;

    @PostMapping("/insertPaymentType")
    public JSONObject insertPaymentType(@RequestBody JSONObject jsonObject, Authentication authentication) throws Exception {
        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            try {
                PermissionCheck.isFinancialOfficer(authentication);
            } catch (Exception ew) {
                return CommonUtil.errorJson(ErrorEnum.E_602);
            }
        }

        try {
            PaymentType paymentType = JSONObject.toJavaObject(jsonObject, PaymentType.class);
            paymentTypeService.insertPaymentType(paymentType);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("501.1"))
                return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("redis"));
            else if (e.getMessage().equals("501.2"))
                return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("结算类型"));
            else if (e.getMessage().equals("605"))
                return CommonUtil.errorJson(ErrorEnum.E_605);
        }

        return CommonUtil.successJson();
    }

    @PostMapping("/deletePaymentType/{id}")
    public JSONObject deletePaymentType(@PathVariable("id") Integer id, Authentication authentication) {

        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            try {
                PermissionCheck.isFinancialOfficer(authentication);
            } catch (Exception ew) {
                return CommonUtil.errorJson(ErrorEnum.E_602);
            }
        }

        try {
            paymentTypeService.deletePaymentType(id);
        } catch (Exception e) {
            if (e.getMessage().equals("606"))
                return CommonUtil.errorJson(ErrorEnum.E_606);
        }
        return CommonUtil.successJson();
    }


    @PostMapping("/recoverPaymentType")
    public JSONObject recoverPaymentType(@RequestBody JSONObject object, Authentication authentication) {
        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            try {
                PermissionCheck.isFinancialOfficer(authentication);
            } catch (Exception ew) {
                return CommonUtil.errorJson(ErrorEnum.E_602);
            }
        }

        PaymentType payment = JSONObject.toJavaObject(object, PaymentType.class);

        PaymentType paymentType = paymentTypeService.findById(payment.getId());

        //检查结算类型是否存在
        if (paymentType == null)
            return CommonUtil.errorJson(ErrorEnum.E_810);

        payment.setDelete(false);
        redisService.setPaymentType(payment);
        paymentTypeService.update(payment);

        return CommonUtil.successJson(paymentType);
    }

    @PostMapping("/modifyPaymentType")
    public JSONObject modifyPaymentType(@RequestBody JSONObject jsonObject, Authentication authentication) {

        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            try {
                PermissionCheck.isFinancialOfficer(authentication);
            } catch (Exception ew) {
                return CommonUtil.errorJson(ErrorEnum.E_602);
            }
        }

        try {
            PaymentType paymentType = JSONObject.toJavaObject(jsonObject, PaymentType.class);
            paymentTypeService.modifyPaymentType(paymentType);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("606"))
                return CommonUtil.errorJson(ErrorEnum.E_605);
        }

        return CommonUtil.successJson();
    }


    @PostMapping("/updatePaymentType")
    public JSONObject updatePaymentType(@RequestBody JSONObject jsonObject, Authentication authentication) {

        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            try {
                PermissionCheck.isFinancialOfficer(authentication);
            } catch (Exception ew) {
                return CommonUtil.errorJson(ErrorEnum.E_602);
            }
        }

        PaymentType paymentType = JSONObject.toJavaObject(jsonObject, PaymentType.class);

        PaymentType lastPaymentType = paymentTypeService.getPaymentTypeByName(paymentType.getName());

        //判断数据是否存在
        if (lastPaymentType == null)
            return CommonUtil.errorJson(ErrorEnum.E_810);

        try {
            paymentTypeService.update(paymentType);
        } catch (Exception e) {
            throw new RuntimeException("信息不能为空");
        }

        return CommonUtil.successJson();
    }


    @GetMapping("/selectPaymentType/{paymentTypeName}")
    public JSONObject selectPaymentType(@PathVariable("paymentTypeName") String paymentTypeName, Authentication authentication) {

        //检查权限
        try {
            PermissionCheck.isHospitalAdmin(authentication);
        } catch (Exception e) {
            try {
                PermissionCheck.isFinancialOfficer(authentication);
            } catch (Exception ew) {
                return CommonUtil.errorJson(ErrorEnum.E_602);
            }
        }
        PaymentType paymentType = paymentTypeService.getPaymentTypeByName(paymentTypeName);

        //判断数据是否存在
        if (paymentType != null)
            return CommonUtil.errorJson(ErrorEnum.E_606);

        return CommonUtil.successJson(paymentType);
    }

    @GetMapping("/getByTypeId/{typeId}")
    public JSONObject getByType(@PathVariable("typeId") Integer typeId) {
        return CommonUtil.successJson(paymentTypeService.getByTypeId(typeId));

    }

    @GetMapping("/getAll")
    public JSONObject getAll() {
        try {
            List<PaymentType> list = paymentTypeService.findAll();
            return CommonUtil.successJson(list);
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }
    }
}
