package cn.neuedu.his.service;
import cn.neuedu.his.model.ConstantVariable;
import cn.neuedu.his.util.inter.Service;

import java.util.List;

/**
 *
 * Created by ccm on 2019/05/24.
 */
public interface ConstantVariableService extends Service<ConstantVariable> {
    List<ConstantVariable> getDepartmentType();

    void insertConstant(ConstantVariable constantVariable,String key);

    List<ConstantVariable> getPrimaryConstant();

    ConstantVariable getConstantByName(Integer type, String name);

    ConstantVariable justifyPrimaryType(Integer id);

    void deleteConstant(Integer id,String type);

    void modifyConstant(ConstantVariable constantVariable, String type);

    List<ConstantVariable> getConstantByType(Integer type);

}
