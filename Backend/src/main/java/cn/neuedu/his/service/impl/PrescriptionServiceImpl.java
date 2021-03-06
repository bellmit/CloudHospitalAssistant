package cn.neuedu.his.service.impl;

import cn.neuedu.his.mapper.PrescriptionMapper;
import cn.neuedu.his.model.Prescription;
import cn.neuedu.his.service.PrescriptionService;
import cn.neuedu.his.util.inter.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 *
 * Created by ccm on 2019/05/29.
 */
@Service
public class PrescriptionServiceImpl extends AbstractService<Prescription> implements PrescriptionService {

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    public  void deleteByTemplateId(Integer templateId){
        prescriptionMapper.deleteByTemplateId(templateId);
    }

    @Override
    public ArrayList<Prescription> getByMedicalRecordId(Integer id) {
        return prescriptionMapper.getByMedicalRecordId(id);
    }

    @Override
    public Prescription findPrescriptionAndDrug(Integer id) {
        return prescriptionMapper.getPrescriptionAndDrug(id);
    }

    @Override
    public ArrayList<Prescription> findPrescriptionAndDrugByMedical(Integer medicalId) {
        return prescriptionMapper.getPrescriptionAndDrugByMedical(medicalId);
    }
}
