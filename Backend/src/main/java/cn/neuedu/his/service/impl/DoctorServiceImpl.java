package cn.neuedu.his.service.impl;

import cn.neuedu.his.mapper.DoctorMapper;
import cn.neuedu.his.model.*;
import cn.neuedu.his.service.*;
import cn.neuedu.his.util.CommonUtil;
import cn.neuedu.his.util.constants.Constants;
import cn.neuedu.his.util.constants.ErrorEnum;
import cn.neuedu.his.util.inter.AbstractService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * Created by ccm on 2019/05/24.
 */
@Service
public class DoctorServiceImpl extends AbstractService<Doctor> implements DoctorService {

    @Autowired
    private DoctorService doctorService;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    RegistrationService registrationService;
    @Autowired
    MedicalRecordService medicalRecordService;
    @Autowired
    MedicalRecordTemplateService medicalRecordTemplateService;
    @Autowired
    DiagnoseService diagnoseService;
    @Autowired
    InspectionTemplateService inspectionTemplateService;
    @Autowired
    InspectionTemplateRelationshipService relationshipService;
    @Autowired
    DrugTemplateService drugTemplateService;

    /**
     * 获得全院检查模板
     * @param doctorID
     * @param level
     * @return
     */
    @Override
    @Transactional
    public JSONObject getHospitalCheckTemps(Integer doctorID,Integer level) {
        List<InspectionTemplate> templates=inspectionTemplateService.getHospitalCheckTemps(doctorID,level,Constants.NON_DRUG);
        if(templates==null)
            templates=new ArrayList<>();
        return CommonUtil.successJson(templates);
    }

    /**
     * 获得科室检查模板
     * @param doctorID
     * @param level
     * @return
     */
    @Override
    @Transactional
    public JSONObject getDeptCheckTemps(Integer doctorID,Integer level) {
//        Registration registration = registrationService.findById(registrationId);
//        if(!registration.getState().equals(Constants.FIRST_DIAGNOSIS)){
//            return  CommonUtil.errorJson(ErrorEnum.E_601.addErrorParamName("registration state"));
//        }
        List<InspectionTemplate> templates=inspectionTemplateService.getDeptCheckTemps(doctorID,level,Constants.NON_DRUG);
        if(templates==null)
            templates=new ArrayList<>();
        return CommonUtil.successJson(templates);
    }

    /**
     * 获得个人检查模板
     * @param doctorID
     * @param level
     * @return
     */
    @Override
    @Transactional
    public JSONObject getPersonalCheckTemps(Integer doctorID,Integer level) {
        List<InspectionTemplate> templates=inspectionTemplateService.getPersonalCheckTemps(doctorID,level,Constants.NON_DRUG);
        if(templates==null)
            templates=new ArrayList<>();
        return CommonUtil.successJson(templates);
    }


    /**
     * 获得全院病例模板
     * @param doctorID
     * @param level
     * @return
     */
    @Override
    @Transactional
    public JSONObject getHospitalMR(Integer doctorID,Integer level) {
        List<MedicalRecordTemplate> templates=medicalRecordTemplateService.getHospitalMR(doctorID,level);
        return CommonUtil.successJson(templates);
    }

    /**
     * 获得科室病例模板
     * @param doctorID
     * @param level
     * @return
     */
    @Override
    @Transactional
    public JSONObject getDeptMR(Integer doctorID,Integer level) {
        List<MedicalRecordTemplate> templates=medicalRecordTemplateService.getDeptMR(doctorID,level);
        return CommonUtil.successJson(templates);
    }

    /**
     * 获得个人病例模板
     * @param doctorID
     * @param level
     * @return
     */
    @Override
    @Transactional
    public JSONObject getPersonalMR(Integer doctorID,Integer level) {
        List<MedicalRecordTemplate> templates=medicalRecordTemplateService.getPersonalMR(doctorID,level);
        return CommonUtil.successJson(templates);
    }


    /**
     * 提交初诊信息
     * @param registrationID
     * @param medicalRecord
     * @return
     */
    @Override
    @Transactional
    public JSONObject setFirstDiagnose(Integer registrationID, MedicalRecord medicalRecord,Diagnose diagnose) throws RuntimeException {
        Registration registration = registrationService.findById(registrationID);
        if(registration==null){
            return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("registrationId"));
        } else{
            registration.setState(Constants.FIRST_DIAGNOSIS);
            registrationService.update(registration);
        }
        //检查是否有必要的参数没有填写完
        String  check=cheakMedicalRecord(medicalRecord);
        if(!check.equals("")){
            throw new RuntimeException("medicalRecord");
        }
        medicalRecord.setId(null);
        medicalRecordService.save(medicalRecord);
        diagnose.setItemId(medicalRecord.getId());
        diagnose.setIsMajor(false);
        diagnose.setCreateTime(new Date(System.currentTimeMillis()));
        diagnose.setTemplate(false);
        diagnoseService.save(diagnose);
        return CommonUtil.successJson();
    }

    //存为全院病历模板
    @Override
    @Transactional
    public JSONObject saveHospitalMRTemplate(MedicalRecord record,Integer doctorID,String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        MedicalRecordTemplate template=copyMedicalRecord(record);
        template.setId(null);
        template.setCreatedById(doctorID);
        template.setName(name);
        template.setDepartmentId(getDeptNo(doctorID));
        template.setLevelId(Constants.HOSPITALLEVEL);
        try{

            medicalRecordTemplateService.save(template);
        }catch (Exception e){
            return CommonUtil.errorJson(ErrorEnum.E_607.addErrorParamName("medicalRecoredTemplate"));
        }
        template.setCreatedById(doctorID);
        try {
            List<Diagnose> firstD=record.getFirstDiagnose();
            saveDiagnose(firstD, template.getId());
            List<Diagnose> finalD=record.getFinalDiagnose();
            saveDiagnose(firstD, template.getId());
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_607.addErrorParamName("diagnose"));
        }
        return CommonUtil.successJson();
    }

    @Override
    public Integer getDeptNo(Integer id) {
        return doctorMapper.getDeptNo(id);
    }

    private String cheakMedicalRecord(MedicalRecord record){
        if (registrationService.findById(record.getRegistrationId())==null)
            return "RegistrationId";
        if (record.getSelfDescription()==null || record.getSelfDescription().equals("")  )
            return "SelfDescription";
        if (record.getCurrentSymptom()==null || record.getCurrentSymptom().equals(""))
            return "CurrentSymptom";
        if (record.getIsWesternMedicine()==null )
            return  "isWesternMedicne";
        if (record.getFirstDiagnose()==null  || record.getFirstDiagnose().size()==0)
            return  "FirstDiagnose";
        return "";
    }

    private MedicalRecordTemplate copyMedicalRecord(MedicalRecord record) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MedicalRecordTemplate template = JSON.parseObject(JSON.toJSONString(record), MedicalRecordTemplate.class);
        return template;
    }

    @Transactional
    public void saveDiagnose(List<Diagnose> firstD,Integer id) throws Exception{
        if (firstD!=null){
            for (Diagnose diagnose:firstD){
                diagnose.setId(null);
                diagnose.setTemplate(true);
                diagnose.setItemId(id);
                diagnose.setCreateTime(new Date(System.currentTimeMillis()));
                diagnoseService.save(diagnose);
            }
        }
    }
}
