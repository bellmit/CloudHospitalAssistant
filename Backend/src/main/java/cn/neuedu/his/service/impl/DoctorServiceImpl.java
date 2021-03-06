package cn.neuedu.his.service.impl;

import cn.neuedu.his.mapper.DoctorMapper;
import cn.neuedu.his.mapper.RegistrationMapper;
import cn.neuedu.his.model.*;
import cn.neuedu.his.service.*;
import cn.neuedu.his.util.CommonUtil;
import cn.neuedu.his.util.StringUtils;
import cn.neuedu.his.util.constants.Constants;
import cn.neuedu.his.util.constants.ErrorEnum;
import cn.neuedu.his.util.inter.AbstractService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by ccm on 2019/05/24.
 */
@Service
public class DoctorServiceImpl extends AbstractService<Doctor> implements DoctorService {

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
    NonDrugService nonDrugService;
    @Autowired
    InspectionTemplateService inspectionTemplateService;
    @Autowired
    InspectionTemplateRelationshipService inspectionTemplateRelationshipService;
    @Autowired
    DrugTemplateService drugTemplateService;
    @Autowired
    DiseaseSecondService diseaseSecondService;
    @Autowired
    InspectionApplicationService inspectionApplicationService;
    @Autowired
    InspectionResultService resultService;
    @Autowired
    PaymentService paymentService;
    @Autowired
    DrugService drugService;
    @Autowired
    PrescriptionService prescriptionService;
    @Autowired
    JobScheduleService scheduleService;
    @Autowired
    PaymentTypeService paymentTypeService;
    @Autowired
    UserService userService;
    @Autowired
    InvoiceService invoiceService;
    @Autowired
    RedisServiceImpl redisService;
    @Autowired
    DrugTemplateRelationshipService drugTemplateRelationshipService;


    @Override
    public JSONObject getRegistrationInof(Date time, Integer doctorId) {
        Integer limit = scheduleService.getRegistrationInfo(time, doctorId);
        Integer amount = registrationService.getRegistrationInof(time, doctorId);
        JSONObject object = new JSONObject();
        object.put("limitAmount", limit);
        object.put("actualAmount", amount);
        return CommonUtil.successJson(object);
    }

    private JSONObject getMRTemp(List<MedicalRecordTemplate> templates) {
        for (MedicalRecordTemplate record : templates) {
            Iterator<Diagnose> w = record.getFinalDiagnose().iterator();
            while (w.hasNext()) {
                if (diseaseSecondService.findById(w.next().getDiseaseId()) == null)
                    w.remove();
            }
        }
        return CommonUtil.successJson(templates);
    }

    @Override
    public JSONObject getMeicalRecordTemByName(String name) {
        return getMRTemp(medicalRecordTemplateService.getMedicalRecordTemByName(name));
    }


    @Override
    public Integer getDeptNo(Integer id) {
        return doctorMapper.getDeptNo(id);
    }

    /**
     * 通过部分连续的字段获得所有疾病
     *
     * @param name
     * @return
     */
    @Override
    public JSONObject findDiseaseByName(String name) {
        List<DiseaseSecond> list = diseaseSecondService.findByName(name);
        if (list == null)
            list = new ArrayList<>();
        return CommonUtil.successJson(list);
    }

    /**
     * 获得所有疾病
     *
     * @return
     */
    @Override
    public JSONObject getAllDiease() {
        List<DiseaseSecond> list = diseaseSecondService.getAll();
        if (list == null)
            list = new ArrayList<>();
        return CommonUtil.successJson(list);
    }

    /**
     * 通过部分连续的字段获得所有非药项目
     *
     * @param name
     * @return
     */
    @Override
    public List<NonDrug> findNonDrugByName(String name) {
        return nonDrugService.findByName(name);
    }

    @Override
    public JSONObject getAllNonDrug() {
        return nonDrugService.getAll();
    }


    /**
     * 提交初诊信息
     *
     * @param registrationID
     * @param medicalRecord
     * @return
     */
    @Override
    @Transactional
    public JSONObject setFirstDiagnose(Integer registrationID, MedicalRecord medicalRecord, List<Integer> diagnoses, Integer doctorId) throws Exception {
        JobSchedule schedule = scheduleService.getByDoctorId(doctorId, new Date(System.currentTimeMillis()));

        Registration registration = registrationService.findById(registrationID);
        if (registration == null) {
            return CommonUtil.errorJson(ErrorEnum.E_705.addErrorParamName("registrationId"));
        } else if (!registration.getState().equals(Constants.INSIDE_DOCTOR)) {
            return CommonUtil.errorJson(ErrorEnum.E_710.addErrorParamName(Constants.INSIDE_DOCTOR.toString()));
        } else {
            registration.setState(Constants.FIRST_DIAGNOSIS);
            registrationService.update(registration);
        }
        //检查是否已经有初诊了
        MedicalRecord record = medicalRecordService.getByRegistrationId(registrationID);
        if (record != null && record.getFirstDiagnose() != null && !record.getFirstDiagnose().isEmpty())
            return CommonUtil.errorJson(ErrorEnum.E_615.addErrorParamName("firstDiagnose"));
        //检查是否有必要的参数没有填写完
        String check = cheakMedicalRecord(medicalRecord);
        if (!check.equals("")) {
            throw new RuntimeException("medicalRecord");
        }
        medicalRecord.setId(null);
        medicalRecordService.save(medicalRecord);
        this.saveDiagnose(diagnoses, medicalRecord.getId(), false, false);
        scheduleService.update(schedule);
        return CommonUtil.successJson();
    }

    @Override
    public JSONObject updateMR(Integer registrationID, MedicalRecord record, List<Integer> diagnoses) {
        MedicalRecord original = medicalRecordService.getMedicalRecordWithDiagnose(record.getId());
        if (original == null) {
            return CommonUtil.errorJson(ErrorEnum.E_805);
        }
        Registration registration = registrationService.findById(registrationID);
        if (registration == null) {
            return CommonUtil.errorJson(ErrorEnum.E_705);
        } else if (registrationService.findById(original.getRegistrationId()).getState() >= Constants.FINISH_DIAGNOSIS) {
            return CommonUtil.errorJson(ErrorEnum.E_807);
        }

        record.setRegistrationId(registrationID);
        String check = cheakMedicalRecord(record);
        if (!check.equals("")) {
            throw new RuntimeException("medicalRecord");
        }

        medicalRecordService.update(record);


        if (diagnoses == null) {
            return CommonUtil.errorJson(ErrorEnum.E_806);
        } else {
            List<Diagnose> dd = original.getFirstDiagnose();
            try {
                if (dd != null) {
                    for (Diagnose d : dd) {
                        diagnoseService.deleteById(d.getId());
                    }
                    this.saveDiagnose(diagnoses, original.getId(), false, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                CommonUtil.errorJson(ErrorEnum.E_808);
            }
        }
        return CommonUtil.successJson();
    }


    /**
     * 存为病历模板
     */
    @Override
    @Transactional
    public JSONObject saveMRTemplate(MedicalRecord record, Integer doctorID, String name, Integer level) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        MedicalRecordTemplate template = copyMedicalRecord(record);
        template = setImportantInfo(template, doctorID, name, level);
        return this.saveRecordAndDiagnoseAsTemp(record, template, doctorID);
    }

    private MedicalRecordTemplate setImportantInfo(MedicalRecordTemplate template, Integer doctorID, String name, Integer level) {
        template.setId(null);
        template.setCreatedById(doctorID);
        template.setName(name);
        template.setDepartmentId(getDeptNo(doctorID));
        template.setLevelId(level);
        return template;
    }

    @Override
    @Transactional
    public JSONObject updateMedicalRecordTem(MedicalRecordTemplate record, Integer doctorID) {
        Integer id = record.getId();
        if (id == null || medicalRecordTemplateService.findById(id) == null)
            return CommonUtil.errorJson(ErrorEnum.E_707.addErrorParamName("medicalRecordTemplate"));
        medicalRecordTemplateService.update(record);
        diagnoseService.deleteByMRT(record.getId());
        this.saveDiagnoseTemp(record.getFirstDiagnose(), record.getId());
        this.saveDiagnoseTemp(record.getFinalDiagnose(), record.getId());
        return CommonUtil.successJson();
    }


    @Override
    @Transactional
    public JSONObject deleteMedicalRecordTemp(Integer id, Integer doctorId) {
        MedicalRecordTemplate template = medicalRecordTemplateService.findById(id);
        if (template == null)
            return CommonUtil.errorJson(ErrorEnum.E_707.addErrorParamName("medicalRecordTemplate"));
        if (!template.getCreatedById().equals(doctorId)) {
            return CommonUtil.errorJson(ErrorEnum.E_706.addErrorParamName("medicalRecordTemplate"));
        }
        medicalRecordTemplateService.deleteById(id);
        diagnoseService.deleteByMRT(id);
        return CommonUtil.successJson();
    }


    @Override
    public JSONObject openInspection(Integer registrationId) {
        Registration registration = registrationService.findById(registrationId);
        if (!(registration.getState().equals(Constants.FIRST_DIAGNOSIS) || registration.getState().equals(Constants.SUSPECT)))
            return CommonUtil.errorJson(ErrorEnum.E_506.addErrorParamName("notFirstDiagnose"));
        return CommonUtil.successJson();
    }

    @Override
    public JSONObject getInspectionResult(Integer id) {
        return CommonUtil.successJson(resultService.getInspectionResult(id));
    }

    @Override
    public JSONObject saveFinalDiagnose(Integer registrationId, MedicalRecord medicalRecord, List<Integer> diagnoses) {
        Registration registration = registrationService.findById(registrationId);
        if (registration == null)
            return CommonUtil.errorJson(ErrorEnum.E_608.addErrorParamName("registration"));
        if (registration.getState() >= Constants.FINISH_DIAGNOSIS) {
            return CommonUtil.errorJson(ErrorEnum.E_809);
        }
        registration.setState(Constants.FINAL_DIAGNOSIS);
        registrationService.update(registration);
        this.saveDiagnose(diagnoses, medicalRecord.getId(), true, false);
        return CommonUtil.successJson();
    }


    /**
     * 提交申请检查..项目
     * @param object
     * @return
     * @throws Exception
     */
    /**
     * 保存处置
     *
     * @param object
     * @return
     * @throws Exception
     */
    @Override
    @Transactional
    public JSONObject saveInspection(JSONObject object, Boolean isDisposal, Integer doctorId) throws Exception {

        Integer registrationId = Integer.parseInt(object.get("registrationId").toString());
        MedicalRecord record = medicalRecordService.getByRegistrationId(registrationId);
        //是否有初诊
        if (record.getFirstDiagnose() == null || record.getFirstDiagnose().size() == 0) {
            return CommonUtil.errorJson(ErrorEnum.E_616.addErrorParamName("firstDiagnose"));
        }
        Integer medicalRecordId = record.getId();

        //是否能找到 挂号
        Registration registration = registrationService.findById(registrationId);
        if (registration == null)
            return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName("registrationId"));
        if (registration.getState().equals(Constants.FINISH_DIAGNOSIS)) {
            return CommonUtil.errorJson(ErrorEnum.E_809);
        }
        if (!isDisposal)
            registration.setState(Constants.SUSPECT);

        //挂号更新
        registrationService.update(registration);

        //是否是模板数据

        InspectionTemplate template = JSONObject.parseObject(JSONObject.toJSONString(object.get("template")), InspectionTemplate.class);
        List<InspectionApplication> applicationList = template.getApplications();
        //保存模板非药项目
        if (applicationList != null) {
            for (InspectionApplication r : applicationList) {
                NonDrug nonDrug = nonDrugService.findById(r.getNonDrugId());
                if (r.getNonDrugId() == null || nonDrug == null) {
                    return CommonUtil.errorJson(ErrorEnum.E_701.addErrorParamName(r.getNonDrugId().toString()));
                }
                InspectionApplication application = new InspectionApplication(medicalRecordId, r.getNonDrugId(), new Date(System.currentTimeMillis()), false, r.getEmerged(), r.getQuantity(), false, false, nonDrug.getFeeTypeId());
                if (isDisposal)
                    application.setCheck(false);
                inspectionApplicationService.save(application);
                Payment payment = setInspectionPayment(application, registration.getPatientId(), doctorId);
                paymentService.save(payment);
            }
        }

        //保存模板药项目
        String check = "";
        List<Prescription> prescriptionList = template.getPrescriptions();
        if (prescriptionList != null) {
            for (Prescription prescription : prescriptionList) {
                check = checkPrescription(prescription);
                if (!check.equals(""))
                    return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName(check));
                Drug drug = drugService.findById(prescription.getDrugId());
                if (drug == null || drug.getDelete())
                    return CommonUtil.errorJson(ErrorEnum.E_626);
                Prescription p2 = new Prescription(prescription, drug.getFeeTypeId(), medicalRecordId, false);
                p2.setTemplate(false);
                p2.setItemId(medicalRecordId);
                prescriptionService.save(p2);
                Payment p = setPrescriptionPayment(p2, registration.getPatientId(), doctorId);
                paymentService.save(p);
            }
        }

        return CommonUtil.successJson();
    }


    private Payment setInspectionPayment(InspectionApplication application, Integer patientId, Integer doctorId) {
        Payment payment = new Payment();
        NonDrug nonDrug = nonDrugService.findById(application.getNonDrugId());
        payment.setQuantity(application.getQuantity());
        payment.setUnitPrice(nonDrug.getPrice());
        payment.setCreateTime(new Date(System.currentTimeMillis()));
        payment.setPatientId(patientId);
        payment.setPaymentTypeId(nonDrug.getFeeTypeId());
        payment.setItemId(application.getId());
        payment.setState(Constants.PRODUCE_PAYMENT);
        payment.setDoctorId(doctorId);
        return payment;
    }

    private Payment setPrescriptionPayment(Prescription application, Integer patientId, Integer doctorId) {
        Payment payment = new Payment();
        Drug drug = drugService.findById(application.getDrugId());
        payment.setQuantity(application.getAmount());
        payment.setUnitPrice(drug.getPrice());
        payment.setCreateTime(new Date(System.currentTimeMillis()));
        payment.setPatientId(patientId);
        payment.setPaymentTypeId(drug.getFeeTypeId());
        payment.setItemId(application.getId());
        payment.setState(Constants.PRODUCE_PAYMENT);
        payment.setDoctorId(doctorId);
        return payment;
    }

    /**
     * 新建检查检验/检验/处置模板
     *
     * @param template
     * @param doctorId
     * @return
     * @throws Exception
     */
    @Override
    @Transactional
    public JSONObject saveInspectionAsTemplate(InspectionTemplate template, Integer doctorId) throws Exception {

        List<InspectionApplication> applicationList = template.getApplications();
        List<Prescription> prescriptionList = template.getPrescriptions();

        template.setCreatedById(doctorId);
        template.setDepartmentId(this.getDeptNo(doctorId));
        if (template.getDepartmentId() == null)
            return CommonUtil.errorJson(ErrorEnum.E_610);
        inspectionTemplateService.save(template);
        Integer tempId = template.getId();
        //保存模板非药项目
        if (applicationList != null) {
            for (InspectionApplication r : applicationList) {

                NonDrug drug = nonDrugService.findById(r.getNonDrugId());
                if (r.getNonDrugId() == null || drug == null || drug.getDelete()) {
                    return CommonUtil.errorJson(ErrorEnum.E_701.addErrorParamName(r.getNonDrugId().toString()));
                }
                InspectionApplication application = new InspectionApplication(tempId, r.getNonDrugId(), new Date(), false, r.getEmerged(), r.getQuantity(), false, true, drug.getFeeTypeId());
                inspectionApplicationService.save(application);

                saveInspectionRelationship(tempId, application.getId(), 0);
            }
        }
        //保存模板非药项目
        String check = "";
        if (prescriptionList != null) {
            for (Prescription prescription : prescriptionList) {
                check = checkPrescription(prescription);
                if (!check.equals(""))
                    return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName(check));
                Drug drug = drugService.findById(prescription.getDrugId());
                Prescription p2 = new Prescription(prescription, drug.getFeeTypeId(), prescription.getItemId(), true);
                p2.setTemplate(true);
                p2.setItemId(tempId);
                prescriptionService.save(p2);

                saveInspectionRelationship(tempId, p2.getId(), 1);
            }
        }
        return CommonUtil.successJson();
    }

    @Transactional
    public void saveInspectionRelationship(Integer tId, Integer itemId, Integer type) {
        InspectionTemplateRelationship relationship = new InspectionTemplateRelationship();
        relationship.setTemplateId(tId);
        relationship.setItemType(type);
        relationship.setItemId(itemId);
        inspectionTemplateRelationshipService.save(relationship);
    }

    @Override
    @Transactional
    public JSONObject updateInspectionTem(InspectionTemplate template, Integer doctorId) throws Exception {
        Integer tempId = template.getId();
        if (tempId == null || inspectionTemplateService.findById(tempId) == null)
            return CommonUtil.errorJson(ErrorEnum.E_707.addErrorParamName("inspectionTemplate"));
        inspectionTemplateService.deleteRelationship(tempId);
        List<Prescription> prescriptions = template.getPrescriptions();
        if (prescriptions != null) {
            for (Prescription p : prescriptions) {
                p.setItemId(tempId);
                p.setTemplate(true);
                prescriptionService.save(p);
            }
        }
        List<InspectionApplication> applications = template.getApplications();
        if (applications != null) {
            for (InspectionApplication p : applications) {
                p.setItemId(tempId);
                p.setTemplate(true);
                p.setCreateTime(new Date(System.currentTimeMillis()));
                inspectionApplicationService.save(p);
            }
        }
        return CommonUtil.successJson();
    }

    @Override
    public JSONObject getInspectionTemByName(String name) {
        return CommonUtil.successJson(inspectionTemplateService.getInspectionTemByName(name));
    }

    @Override
    @Transactional
    public JSONObject deleteInspectionTemp(Integer id, Integer doctorId) {
        InspectionTemplate template = inspectionTemplateService.findById(id);
        if (template == null)
            return CommonUtil.errorJson(ErrorEnum.E_707.addErrorParamName("inspectionTemplate"));
        if (!template.getCreatedById().equals(doctorId)) {
            return CommonUtil.errorJson(ErrorEnum.E_706.addErrorParamName("inspectionTemplate"));
        }
        inspectionTemplateService.deleteRelationship(id);
        inspectionApplicationService.deleteById(id);
        return CommonUtil.successJson();
    }


    @Override
    @Transactional
    public JSONObject savePrescriptions(List<Prescription> prescriptions, Integer registrationId, Integer doctorId) throws Exception {

        Registration registration = registrationService.findById(registrationId);

        MedicalRecord record = medicalRecordService.getByRegistrationId(registrationId);
        if (record == null) {
            return CommonUtil.errorJson(ErrorEnum.E_805);
        }
        Integer medicalRecordId = record.getId();

        if (!registration.getState().equals(Constants.FINAL_DIAGNOSIS))
            return CommonUtil.errorJson(ErrorEnum.E_703);
        if (registration.getState().equals(Constants.FINISH_DIAGNOSIS)) {
            return CommonUtil.errorJson(ErrorEnum.E_809);
        }
        String check;
        for (Prescription p : prescriptions) {
            p.setCreateTime(new Date(System.currentTimeMillis()));
            p.setId(null);
            p.setItemId(medicalRecordId);
            p.setTemplate(false);
            Drug drug = drugService.findById(p.getDrugId());
            if (drug == null || drug.getDelete() == true)
                return CommonUtil.errorJson(ErrorEnum.E_626);
            check = checkPrescription(p);
            if (!check.equals("")) {
                return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName(check));
            }
            prescriptionService.save(p);
            Payment payment = setPrescriptionPayment(p, registration.getPatientId(), doctorId);
        }
//        registration.setState(Constants.FINISH_DIAGNOSIS);
//        registrationService.update(registration);
        return CommonUtil.successJson();
    }

    @Override
    @Transactional
    public JSONObject savePrescriptionsTemp(DrugTemplate template, Integer medicalRecordId, Integer doctorId) throws Exception {
        MedicalRecord medicalRecord = medicalRecordService.findById(medicalRecordId);
        template.setHerbal(!medicalRecord.getWesternMedicine());
        template.setCreatedById(doctorId);
        template.setDepartmentId(this.getDeptNo(doctorId));
        drugTemplateService.save(template);

        for (Prescription p : template.getPrescriptions()) {
            p.setId(null);
            p.setTemplate(true);
            p.setItemId(template.getId());
            p.setCreateTime(new Date(System.currentTimeMillis()));
            String check = checkPrescription(p);
            if (!check.equals("")) {
                return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName(check));
            }
            prescriptionService.save(p);
        }
        return CommonUtil.successJson();
    }

    @Override
    @Transactional
    public JSONObject updatePrescriptionsTemp(DrugTemplate template, Integer doctorId) {
        prescriptionService.deleteByTemplateId(template.getId());

        drugTemplateService.update(template);

        for (Prescription p : template.getPrescriptions()) {
            p.setId(null);
            p.setTemplate(true);
            p.setItemId(template.getId());
            p.setCreateTime(new Date(System.currentTimeMillis()));
            String check = checkPrescription(p);
            if (!check.equals("")) {
                return CommonUtil.errorJson(ErrorEnum.E_501.addErrorParamName(check));
            }
            prescriptionService.save(p);
        }
        return CommonUtil.successJson();
    }


    @Override
    public JSONObject getPrescriptionsTemByName(String name) {
        List<DrugTemplate> drugTemplates = drugTemplateService.getPrescriptionsTemByName(name);
        for (DrugTemplate drugTemplate : drugTemplates) {
            Iterator<Prescription> iterator = drugTemplate.getPrescriptions().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getDrug().getDelete() == true)
                    iterator.remove();
            }
        }
        return CommonUtil.successJson();
    }


    private String checkPrescription(Prescription p) {
        if (p.getUsageId() == null) {
            return "usageId";
        }
        if (p.getFrequency() == null || p.getFrequency().equals("")) {
            return "frequency";
        }
        if (p.getDrugId() == null)
            return "drug";
        if (p.getAmount() == null)
            return "amount";
        if (p.getDays() == null)
            return "days";
        if (p.getNeedSkinTest() == null)
            return "needSkinTest";
        return "";
    }

    @Transactional
    public JSONObject saveRecordAndDiagnoseAsTemp(MedicalRecord record, MedicalRecordTemplate template, Integer doctorID) throws RuntimeException {
        try {
            medicalRecordTemplateService.save(template);
        } catch (Exception e) {
            throw new RuntimeException("medicalRecoredTemplate");
        }
        try {
            List<Diagnose> firstD = record.getFirstDiagnose();
            this.saveDiagnoseTemp(firstD, template.getId());
            List<Diagnose> finalD = record.getFinalDiagnose();
            this.saveDiagnoseTemp(finalD, template.getId());
        } catch (Exception e) {
            throw new RuntimeException("diagnose");
        }
        return CommonUtil.successJson();
    }

    private String cheakMedicalRecord(MedicalRecord record) {
        if (registrationService.findById(record.getRegistrationId()) == null)
            return "RegistrationId";
        if (record.getSelfDescription() == null || record.getSelfDescription().equals(""))
            return "SelfDescription";
        if (record.getCurrentSymptom() == null || record.getCurrentSymptom().equals(""))
            return "CurrentSymptom";
        if (record.getIsWesternMedicine() == null)
            return "isWesternMedicne";
        return "";
    }

    private String cheakInspection(InspectionApplication inspectionApplication) {
        if (medicalRecordService.findById(inspectionApplication.getItemId()) == null)
            return "MedicalRecord";
        if (nonDrugService.findById(inspectionApplication.getNonDrugId()) == null)
            return "NonDrug";
        if (inspectionApplication.getQuantity() == null || inspectionApplication.getQuantity().equals(""))
            return "Quantity";
        if (inspectionApplication.getCreateTime() == null || inspectionApplication.getCreateTime().equals(""))
            return "CreateTime";
        return "";
    }

    private MedicalRecordTemplate copyMedicalRecord(MedicalRecord record) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MedicalRecordTemplate template = JSON.parseObject(JSON.toJSONString(record), MedicalRecordTemplate.class);
        return template;
    }


    @Override
    public List<Doctor> getByDepartmentId(Integer departmentId) {
        return doctorMapper.getByDepartmentId(departmentId);
    }

    @Override
    @Transactional
    public void saveDiagnose(List<Integer> diagnoses, Integer itemId, Boolean isMajor, Boolean isTemplate) {
        if (diagnoses != null) {
            for (Integer integer : diagnoses) {
                Diagnose diagnose = new Diagnose();
                diagnose.setId(null);
                diagnose.setItemId(itemId);
                diagnose.setIsMajor(isMajor);
                diagnose.setCreateTime(new Date(System.currentTimeMillis()));
                diagnose.setTemplate(isTemplate);
                diagnose.setDiseaseId(integer);
                diagnoseService.save(diagnose);
            }
        }
    }

    @Transactional
    public void saveDiagnoseTemp(List<Diagnose> diagnoses, Integer templateId) {
        if (diagnoses != null) {
            for (Diagnose d : diagnoses) {
                Diagnose diagnose = new Diagnose();
                diagnose.setId(null);
                diagnose.setItemId(templateId);
                diagnose.setIsMajor(d.getIsMajor());
                diagnose.setCreateTime(d.getCreateTime());
                diagnose.setTemplate(true);
                diagnose.setDiseaseId(d.getDiseaseId());
                diagnoseService.save(diagnose);
            }
        }
    }


    @Transactional
    public JSONObject finishDiagnose(Integer registrationId) {
        Registration registration = registrationService.findById(registrationId);
        if (registration == null)
            return CommonUtil.errorJson(ErrorEnum.E_705);
        if (!registration.getState().equals(Constants.FIRST_DIAGNOSIS) && !registration.getState().equals(Constants.FINAL_DIAGNOSIS))
            return CommonUtil.errorJson(ErrorEnum.E_703);
        registration.setState(Constants.FINISH_DIAGNOSIS);
        registrationService.update(registration);
        return CommonUtil.successJson();
    }


    @Override
    @Transactional
    public JSONObject getAllPaymentDetails(Integer medicalRecordId, Integer registrationId) {
        List<Prescription> prescriptions = prescriptionService.getByMedicalRecordId(medicalRecordId);
        List<InspectionApplication> applications = inspectionApplicationService.getByMedicalRecordId(medicalRecordId);
        Map<String, Integer> map;
        try {
            map = redisService.getMapAll("paymentType");
        } catch (Exception e) {
            return CommonUtil.errorJson(ErrorEnum.E_802);
        }

        List<Payment> payments = paymentService.getByRegistrationId(registrationId, map.get("挂号费"));
        BigDecimal total = BigDecimal.valueOf(0);
        if (prescriptions == null)
            prescriptions = new ArrayList<>();
        else {
            total = addPrescriptionTotal(total, prescriptions);
        }
        if (applications == null)
            applications = new ArrayList<>();
        else {
            total = addApplicationTotal(total, applications);
        }
        if (payments == null)
            payments = new ArrayList<>();
        else {
            total = addRegistrationTotal(total, payments);
        }
        JSONObject object = new JSONObject();
        object.put("prescriptions", prescriptions);
        object.put("applications", applications);
        object.put("registrations", payments);
        object.put("total", total);
        return CommonUtil.successJson(object);
    }

    @Override
    public JSONObject getDoctorTotal(Integer doctorId, String start, String end) {
        JSONObject object = new JSONObject();
        HashMap<Integer, List<Integer>> registrationHashMap = new HashMap<>();

        List<Integer> registrationIds = registrationService.getAllByDoctor(doctorId, start, end, Constants.FIRST_DIAGNOSIS);
        registrationHashMap.put(Constants.FIRST_DIAGNOSIS, registrationIds);

        List<Integer> suspects = registrationService.getAllByDoctor(doctorId, start, end, Constants.SUSPECT);
        registrationIds.addAll(suspects);
        registrationHashMap.put(Constants.SUSPECT, suspects);

        List<Integer> finalD = registrationService.getAllByDoctor(doctorId, start, end, Constants.FINAL_DIAGNOSIS);
        registrationIds.addAll(finalD);
        registrationHashMap.put(Constants.FINAL_DIAGNOSIS, finalD);

        List<Integer> finish = registrationService.getAllByDoctor(doctorId, start, end, Constants.FINISH_DIAGNOSIS);
        registrationIds.addAll(finish);
        registrationHashMap.put(Constants.FINISH_DIAGNOSIS, finish);


        HashMap<Integer, List<InspectionApplication>> applicationHashMap = new HashMap<>();
        HashMap<Integer, List<Prescription>> prescriptionHashMap = new HashMap<>();
        HashMap<Integer, List<Payment>> otherPaymentHashMap = new HashMap<>();
        HashMap<Integer, MedicalRecord> medicalRecordHashMap = new HashMap<>();

        BigDecimal prescriptionTotal = BigDecimal.valueOf(0);
        BigDecimal applicationTotal = BigDecimal.valueOf(0);
        BigDecimal registrationTotal = BigDecimal.valueOf(0);
        for (Integer registrationId : registrationIds) {
            MedicalRecord record = medicalRecordService.getByRegistrationId(registrationId);

            List<Prescription> prescriptions = prescriptionService.getByMedicalRecordId(record.getId());
            List<InspectionApplication> applications = inspectionApplicationService.getByMedicalRecordId(record.getId());
            Map<String, Integer> map;
            try {
                map = redisService.getMapAll("paymentType");
            } catch (Exception e) {
                return CommonUtil.errorJson(ErrorEnum.E_802);
            }
            List<Payment> payments = paymentService.getByRegistrationId(registrationId, map.get("挂号费"));

            if (prescriptions == null)
                prescriptions = new ArrayList<>();
            else
                prescriptionTotal = addPrescriptionTotal(prescriptionTotal, prescriptions);
            if (applications == null)
                applications = new ArrayList<>();
            else
                applicationTotal = addApplicationTotal(applicationTotal, applications);

            if (payments == null)
                payments = new ArrayList<>();
            else
                registrationTotal = addRegistrationTotal(registrationTotal, payments);

            prescriptionHashMap.put(registrationId, prescriptions);
            applicationHashMap.put(registrationId, applications);
            otherPaymentHashMap.put(registrationId, payments);
            medicalRecordHashMap.put(registrationId, record);

        }


        object.put("registrations", registrationHashMap);
        object.put("registrationsNum", registrationIds.size());
        object.put("prescriptionHashMap", prescriptionHashMap);
        object.put("applicationHashMap", applicationHashMap);
        object.put("otherPaymentHashMap", otherPaymentHashMap);
        object.put("medicalRecordHashMap", medicalRecordHashMap);


        object.put("prescriptionNum", prescriptionTotal);
        object.put("applicationNum", applicationTotal);
        object.put("registrationNum", registrationTotal);

        return CommonUtil.successJson(object);
    }

    public Integer registrationNum(Integer doctorId, String start, String end) {
        List<Integer> registrationIds = registrationService.getAllByDoctor(doctorId, start, end, Constants.FIRST_DIAGNOSIS);

        List<Integer> suspects = registrationService.getAllByDoctor(doctorId, start, end, Constants.SUSPECT);
        registrationIds.addAll(suspects);

        List<Integer> finalD = registrationService.getAllByDoctor(doctorId, start, end, Constants.FINAL_DIAGNOSIS);
        registrationIds.addAll(finalD);

        List<Integer> finish = registrationService.getAllByDoctor(doctorId, start, end, Constants.FINISH_DIAGNOSIS);
        registrationIds.addAll(finish);
        return registrationIds.size();
    }

    /**
     * 门诊医生工作量统计
     * cjq
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public JSONObject doctorWorkCalculate(Date startDate, Date endDate) {
        JSONObject result = new JSONObject();
        JSONArray data = new JSONArray();
        ArrayList<User> doctorList = doctorMapper.getAllClinicNotDelete();

        //取出所有paymentType的id和名字
        Map<Integer, String> paymentTypeMap = new HashMap<>();
        Map<String, Integer> redisMap;
        try {
            redisMap = redisService.getMapAll("paymentType");
        } catch (Exception e) {
            throw new UnsupportedOperationException("redis");
        }
        for (String key : redisMap.keySet()) {
            paymentTypeMap.put(redisMap.get(key), key);
        }

        //参数：医生id <缴费类型id, 总金额>
        Map<Integer, Map<Integer, BigDecimal>> resultMap = new HashMap<>();
        //获取医生所有id
        ArrayList<Integer> doctorIdList = new ArrayList<>();
        for (User user : doctorList) {
            //使用map记录每个医生的每个paymentType对应的总额
            //初始化
            Map<Integer, BigDecimal> feeMap = new HashMap<>();
            for (Integer id : paymentTypeMap.keySet()) {
                feeMap.put(id, new BigDecimal(0));
            }
            resultMap.put(user.getId(), feeMap);
            doctorIdList.add(user.getId());
        }

        if(!doctorIdList.isEmpty()) {
            //获取所有缴费单
            ArrayList<Payment> paymentList = paymentService.findAllByDoctor(doctorIdList, startDate, endDate);
            for (Payment payment : paymentList) {
                //更新某缴费项目类型的金额数据
                Integer doctorId = payment.getDoctorId();
                Map<Integer, BigDecimal> feeMap = resultMap.get(doctorId);
                BigDecimal originalFee = feeMap.get(payment.getPaymentTypeId());
                originalFee = originalFee == null? new BigDecimal(0) : originalFee;
                feeMap.put(payment.getPaymentTypeId(), originalFee.add(payment.getUnitPrice().multiply(new BigDecimal(payment.getQuantity()))));
                resultMap.put(doctorId, feeMap);
            }
        }

        for(User user: doctorList) {
            JSONObject detail = new JSONObject();
            detail.put("doctor", user);
            detail.put("invoiceNumber", invoiceService.getInvoiceNumberByAllDoctor(user.getId(), startDate, endDate));
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            detail.put("visitNumber", this.registrationNum(user.getId(), format.format(startDate), format.format(endDate)));
            BigDecimal totalFee = new BigDecimal(0);
            Map<Integer, BigDecimal> feeMap = resultMap.get(user.getId());
            for (Integer key : feeMap.keySet()) {
                totalFee = totalFee.add(feeMap.get(key));
                detail.put(key.toString(), feeMap.get(key));
            }
            detail.put("total", totalFee);

            data.add(detail);
        }

        result.put("columns", setColumns(paymentTypeMap));
        result.put("data", data);

        return result;
    }

    /**
     * 前端表内列设置
     * cjq
     *
     * @param paymentTypeMap
     * @return
     */
    private JSONArray setColumns(Map<Integer, String> paymentTypeMap) {
        //设置前端column值
        JSONArray columns = new JSONArray();
        columns.add(setColumn("医生名称", "doctor.realName", 120, "left"));
        columns.add(setColumn("发票数", "invoiceNumber", 120, "left"));
        columns.add(setColumn("看诊人数", "visitNumber", 120, "left"));

        for (Integer key : paymentTypeMap.keySet())
            columns.add(setColumn(paymentTypeMap.get(key), key.toString(), 100));

        columns.add(setColumn("合计", "total", 90, "right"));

        return columns;
    }

    /**
     * 前端内列表设置（默认基本方法）
     * cjq
     *
     * @param title
     * @param dataIndex
     * @param width
     * @param fixed
     * @return
     */
    private JSONObject setColumn(String title, String dataIndex, Integer width, String fixed) {
        JSONObject column = new JSONObject();
        column.put("title", title);
        column.put("dataIndex", dataIndex);
        column.put("key", dataIndex);
        column.put("width", width);
        column.put("fixed", fixed);
        column.put("align", "center");
        return column;
    }

    /**
     * 前端内列表设置（默认基本方法）
     * cjq
     *
     * @param title
     * @param dataIndex
     * @param width
     * @return
     */
    private JSONObject setColumn(String title, String dataIndex, Integer width) {
        JSONObject column = new JSONObject();
        column.put("title", title);
        column.put("dataIndex", dataIndex);
        column.put("key", dataIndex);
        column.put("width", width);
        column.put("align", "center");
        return column;
    }


    @Autowired
    ConstantVariableService constantVariableService;

    @Autowired
    RegistrationMapper registrationMapper;

    /**
     * 获得医生某时间所有费用详情
     *
     * @param doctorId
     * @param start
     * @param end
     * @return
     */
    @Override
    public JSONObject getDoctorStatistics(Integer doctorId, String start, String end) {

        User user = userService.findById(doctorId);
        Doctor doctor = this.findById(doctorId);
        if (doctor == null) {
            return CommonUtil.errorJson(ErrorEnum.E_617);
        }
        doctor.setRealName(user.getRealName());
        doctor.setTitleName(constantVariableService.findById(doctor.getTitleId()).getName());
        //取出所有paymentType的id和名字
        Map<Integer, String> paymentTypeMap = new HashMap<>();
        ArrayList<PaymentType> smallPaymentType = paymentTypeService.getSmallPaymentType();
        for (PaymentType paymentType : smallPaymentType) {
            paymentTypeMap.put(paymentType.getId(), paymentType.getName());
        }

        //使用map记录每个医生的每个paymentType对应的总额

        JSONArray array = new JSONArray();
        Integer count = 0;
        for (Integer id : paymentTypeMap.keySet()) {
            count = paymentService.getAllPayments(doctorId, start, end, id);
            if (count != null && count.intValue() > 0) {
                JSONObject object = new JSONObject();
                object.put("name", paymentTypeMap.get(id));
                object.put("value", new BigDecimal(count));
                object.put("key", id);
                array.add(object);
            }
        }
        doctor.setFeeMap(array);
        return CommonUtil.successJson(doctor);
    }


    @Autowired
    PatientService patientService;

    @Override
    public JSONObject getRegistrationStatistics(Integer doctorId, String start, String end) {
        JSONArray patients = new JSONArray();
        ArrayList<Registration> list = registrationMapper.getPatient(doctorId, start, end, 2, 1);

        Integer patientId = null;
        Patient patient = null;
        JSONArray a = new JSONArray();
        for (Registration registration : list) {
            if (registration.getMedicalFee() == null) {
                registration.setMedicalFee((float) 0);
            }
            if (registration.getInspectionFee() == null) {
                registration.setInspectionFee((float) 0);
            }
            if (registration.getRegistrationFee() == null) {
                registration.setRegistrationFee((float) 0);
            }

            if (patientId == null || !patientId.equals(registration.getPatientId())) {
                if (patient != null) {
                    a.add(patient);
                }
                patientId = registration.getPatientId();
                patient = patientService.findById(patientId);
                patient.setAge(StringUtils.identityIdTransferToAge(patient.getIdentityId()));
                JSONArray array = new JSONArray();
                array.add(registration);
                patient.setRegistrations(array);
            } else {
                patient.addRegistrations(registration);
            }
        }
        if (patient != null) {
            a.add(patient);
        }
        return CommonUtil.successJson(a);
    }

    @Override
    public Integer findDepartmentRegistrationAmount(ArrayList<Integer> doctorIdList, Date startDate, Date endDate) {
        Integer result = 0;
        result = result + doctorMapper.getDepartmentRegistrationAmountByState(doctorIdList, startDate, endDate, Constants.FIRST_DIAGNOSIS);
        result = result + doctorMapper.getDepartmentRegistrationAmountByState(doctorIdList, startDate, endDate, Constants.SUSPECT);
        result = result + doctorMapper.getDepartmentRegistrationAmountByState(doctorIdList, startDate, endDate, Constants.FINAL_DIAGNOSIS);
        result = result + doctorMapper.getDepartmentRegistrationAmountByState(doctorIdList, startDate, endDate, Constants.FINISH_DIAGNOSIS);
        return result;
    }

    private BigDecimal addPrescriptionTotal(BigDecimal prescriptionTotal, List<Prescription> prescriptions) {

        for (Prescription p : prescriptions) {
            prescriptionTotal = prescriptionTotal.add(BigDecimal.valueOf(p.getAmount()).multiply(p.getDrug().getPrice()));
        }
        return prescriptionTotal;
    }

    private BigDecimal addApplicationTotal(BigDecimal applicationTotal, List<InspectionApplication> applications) {


        for (InspectionApplication a : applications) {
            applicationTotal = applicationTotal.add(BigDecimal.valueOf(a.getQuantity()).multiply(a.getNonDrug().getPrice()));
        }
        return applicationTotal;
    }

    private BigDecimal addRegistrationTotal(BigDecimal registrationTotal, List<Payment> payments) {

        for (Payment p : payments) {
            registrationTotal = registrationTotal.add(p.getUnitPrice().multiply(BigDecimal.valueOf(p.getQuantity())));
        }
        return registrationTotal;
    }
}
