package cn.neuedu.his.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

public class Drug implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "formulation")
    private Integer formulation;

    private String factory;

    /**
     * 包装单位
     */
    @Column(name = "package_company")
    private String packageCompany;

    private String spell;

    /**
     * 规格
     */
    private String standard;

    private BigDecimal price;

    private String name;

    private String code;
    private Integer stockAmount;
    private Integer drugType;
    private Integer feeTypeId;

    private ConstantVariable feeType;
    private ConstantVariable formul;

    public ConstantVariable getFormul() {
        return formul;
    }

    public void setFormul(ConstantVariable formul) {
        this.formul = formul;
    }

    public ConstantVariable getFeeType() {
        return feeType;
    }

    public void setFeeType(ConstantVariable feeType) {
        this.feeType = feeType;
    }

    @Column(name = "is_delete")
    private Boolean isDelete;

    public Boolean getDelete() {
        return isDelete;
    }

    public void setDelete(Boolean delete) {
        isDelete = delete;
    }

    public Integer getFeeTypeId() {
        return feeTypeId;
    }

    public void setFeeTypeId(Integer feeTypeId) {
        this.feeTypeId = feeTypeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getStockAmount() {
        return stockAmount;
    }

    public void setStockAmount(Integer stockAmount) {
        this.stockAmount = stockAmount;
    }

    public Integer getDrugType() {
        return drugType;
    }

    public void setDrugType(Integer drugType) {
        this.drugType = drugType;
    }

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return formulation
     */
    public Integer getFormulation() {
        return formulation;
    }

    /**
     * @param formulation
     */
    public void setFormulation(Integer formulation) {
        this.formulation = formulation;
    }

    /**
     * @return factory
     */
    public String getFactory() {
        return factory;
    }

    /**
     * @param factory
     */
    public void setFactory(String factory) {
        this.factory = factory;
    }

    /**
     * 获取包装单位
     *
     * @return package_company - 包装单位
     */
    public String getPackageCompany() {
        return packageCompany;
    }

    /**
     * 设置包装单位
     *
     * @param packageCompany 包装单位
     */
    public void setPackageCompany(String packageCompany) {
        this.packageCompany = packageCompany;
    }

    /**
     * @return spell
     */
    public String getSpell() {
        return spell;
    }

    /**
     * @param spell
     */
    public void setSpell(String spell) {
        this.spell = spell;
    }

    /**
     * 获取规格
     *
     * @return standard - 规格
     */
    public String getStandard() {
        return standard;
    }

    /**
     * 设置规格
     *
     * @param standard 规格
     */
    public void setStandard(String standard) {
        this.standard = standard;
    }

    /**
     * @return price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * @param price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}