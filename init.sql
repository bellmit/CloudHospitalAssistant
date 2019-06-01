create table if not exists constant_variable
(
    id   int auto_increment
        primary key,
    name varchar(255) not null,
    type int          not null comment '0: 本表类别'
);

create table if not exists department_kind
(
    id                int auto_increment
        primary key,
    kind_name         varchar(64) not null,
    classification_id int         not null comment '参照常量表'
);

create table if not exists department
(
    id      int auto_increment
        primary key,
    name    varchar(64) not null,
    kind_id int         not null,
    code    varchar(24) not null,
    constraint department_department_kind_id_fk
        foreign key (kind_id) references department_kind (id)
);

create table if not exists disease_first
(
    id   int         not null
        primary key,
    name varchar(64) not null
);

create table if not exists disease_second
(
    id               int         not null
        primary key,
    icd_id           varchar(64) not null,
    disease_coding   varchar(64) not null,
    name             varchar(64) not null,
    disease_first_id int         not null,
    constraint disease_second_disease_first_id_fk
        foreign key (disease_first_id) references disease_first (id)
);

create table if not exists drug
(
    id              int auto_increment
        primary key,
    code            varchar(24)       not null comment '编码',
    name            varchar(24)       not null,
    standard        varchar(24)       not null comment '规格',
    package_company varchar(64)       not null comment '包装单位',
    factory         varchar(64)       null,
    formulation     varchar(24)       not null comment '药剂剂型',
    drug_type       varchar(24)       not null comment '中草药
西药
中成药
之类的
',
    price           decimal(10, 2)    not null,
    spell           varchar(64)       not null,
    stock_amount    int default 10000 not null
);

create table if not exists frequently_used
(
    user_id int not null,
    item_id int not null,
    type    int not null
)
    comment '常用项目表';

create table if not exists non_drug
(
    id                   int auto_increment
        primary key,
    code                 varchar(24)            not null,
    name                 varchar(24)            not null,
    standard             varchar(24) default '' null,
    price                decimal(10, 2)         not null,
    fee_type_id          int                    not null comment '非药物费用类别',
    executive_department int                    not null
)
    comment 'standard-规格';

create table if not exists patient
(
    id                        int auto_increment
        primary key,
    identity_id               varchar(18)  null comment '身份证号',
    password                  varchar(255) not null,
    sex                       tinyint(1)   not null comment 'true:women
false:men',
    real_name                 varchar(24)  not null,
    username                  varchar(64)  not null,
    last_login_time           datetime     null,
    create_time               datetime     not null,
    last_password_modify_time datetime     null,
    constraint patient_identity_id_uindex
        unique (identity_id),
    constraint patient_username_uindex
        unique (username)
);

create table if not exists payment_type
(
    id   int auto_increment
        primary key,
    code varchar(24) not null,
    name varchar(64) not null,
    type int         not null comment '大类：0 -- 挂号费
1 -- 处方
2 -- 项目'
)
    comment '费用科目，e.g:西药费，挂号费，可直接由老师给的表导入';

create table if not exists registration_type
(
    id                      int auto_increment
        primary key,
    name                    varchar(64)    not null,
    is_default              tinyint(1)     not null,
    price                   decimal(10, 2) not null,
    display_sequence_number int default 50 not null
);

create table if not exists user
(
    id                        int auto_increment
        primary key,
    username                  varchar(64)  not null,
    real_name                 varchar(64)  not null,
    password                  varchar(255) not null,
    create_time               datetime     not null,
    last_login_time           datetime     null,
    last_password_modify_time datetime     null,
    type_id                   int          not null comment '记录医生类别，具体参考常量表',
    department_id             int          not null,
    identify_id               varchar(18)  not null,
    constraint user_username_uindex
        unique (username),
    constraint user_department_id_fk
        foreign key (department_id) references department (id)
)
    comment '储存医生，操作员';

create table if not exists doctor
(
    id          int auto_increment
        primary key,
    title_id    int                  not null comment '职称,参照常量表',
    can_arrange tinyint(1) default 0 not null comment '是否排班',
    constraint doctor_user_id_fk
        foreign key (id) references user (id)
)
    comment '医生表';

create table if not exists drug_template
(
    id            int         not null
        primary key,
    department_id int         not null,
    level         int         not null,
    name          varchar(24) not null,
    created_by_id int         not null,
    constraint drug_template_user_id_fk
        foreign key (created_by_id) references user (id)
);

create table if not exists drug_template_relationship
(
    template_id int         not null,
    drug_id     int         not null comment '对应某次药id',
    frequency   varchar(64) null,
    amount      int         null,
    use_amount  int         null,
    days        int         null,
    constraint `drug_template_relationship_drug __fk`
        foreign key (drug_id) references drug (id),
    constraint drug_template_relationship_drug_template_id_fk
        foreign key (template_id) references drug_template (id)
);

create table if not exists inspection_template
(
    id            int         not null
        primary key,
    department_id int         not null,
    level         int         not null,
    name          varchar(24) not null comment '模板名称',
    created_by_id int         not null,
    constraint Inspection_template_user_id_fk
        foreign key (created_by_id) references user (id),
    constraint inspection_template_department_id_fk
        foreign key (department_id) references department (id)
);

create table if not exists inspection_template_relationship
(
    id          int auto_increment
        primary key,
    template_id int           not null,
    item_id     int           not null,
    item_type   int default 1 not null comment '1-西药
2-中成药
3-中草药
4-非药',
    amount      int default 1 not null,
    constraint inspection_template_relationship_inspection_template_id_fk
        foreign key (template_id) references inspection_template (id)
);

create table if not exists job_schedule
(
    id                          int auto_increment
        primary key,
    doctor_id                   int        not null,
    current_registration_amount int        not null,
    registration_type_id        int        not null,
    is_valid                    tinyint(1) not null,
    limit_registration_amount   int        not null,
    period                      int        not null comment '午别，参照常量表',
    date                        datetime   not null,
    constraint job_schedule_doctor_id_fk
        foreign key (doctor_id) references doctor (id),
    constraint job_schedule_registration_type_id_fk
        foreign key (registration_type_id) references registration_type (id)
);

create table if not exists medical_record_template
(
    id                  int auto_increment
        primary key,
    self_description    varchar(255) not null,
    is_pregnant         tinyint(1)   not null,
    current_symptom     varchar(255) not null,
    history_symptom     varchar(255) not null,
    previous_treatment  text         not null,
    is_western_medicine tinyint(1)   not null,
    allergy_history     varchar(255) null,
    body_examination    varchar(255) not null,
    created_by_id       int          not null,
    department_id       int          not null,
    level_id            int          not null comment '权限级别，参照常量表',
    name                varchar(24)  not null comment '模板名称',
    constraint medical_record_template_user_id_fk
        foreign key (created_by_id) references user (id)
);

create table if not exists payment
(
    id                 int auto_increment
        primary key,
    quantity           int        default 1 not null,
    unit_price         decimal(10, 2)       not null,
    create_time        datetime             not null,
    have_completed     tinyint(1) default 0 not null,
    is_retreat         tinyint(1) default 0 not null comment '是否为退费',
    is_frozen          tinyint(1) default 0 not null comment '对账的时候冻结账单',
    operator_id        int                  not null comment '缴费员',
    patient_id         int                  not null,
    settlement_type_id int                  not null comment '参考常量表
结费类型：自费/医保等',
    payment_type_id    int                  not null comment '费用类型：西药费/挂号费之类的',
    item_id            int                  not null,
    constraint payment_patient_id_fk
        foreign key (patient_id) references patient (id),
    constraint payment_payment_type_id_fk
        foreign key (payment_type_id) references payment_type (id),
    constraint payment_user_id_fk
        foreign key (operator_id) references user (id)
)
    comment 'item_type:处方，检查项目，挂号';

create table if not exists invoice
(
    id           int auto_increment
        primary key,
    price_amount decimal(10, 2) not null,
    created_date datetime       not null,
    payment_id   int            not null,
    constraint invoice_payment_id_fk
        foreign key (payment_id) references payment (id)
);

create table if not exists registration
(
    id           int auto_increment
        primary key,
    need_book    tinyint(1) default 0 not null comment '是否需要病历本',
    patient_id   int                  not null,
    doctor_id    int                  not null,
    state        int                  not null comment '状态，参考常量表',
    schedule_id  int                  not null,
    registrar_id int                  not null,
    age          int(3)               not null,
    constraint registration_doctor_id_fk
        foreign key (doctor_id) references doctor (id),
    constraint registration_patient_id_fk
        foreign key (patient_id) references patient (id)
);

create table if not exists medical_record
(
    id                  int auto_increment
        primary key,
    self_description    varchar(255)             not null,
    is_pregnant         tinyint(1)               not null,
    current_symptom     varchar(255)             not null,
    history_symptom     varchar(255) default '无' not null comment '无',
    previous_treatment  text                     not null comment '无',
    is_western_medicine tinyint(1)               not null comment '是否为西医诊断',
    allergy_history     varchar(255) default '无' null comment '无',
    body_examination    varchar(255) default '无' null comment '无',
    registration_id     int                      not null,
    check_advice        varchar(255) default '无' not null comment '检察建议
',
    notification        varchar(255) default '无' not null comment '注意事项
',
    constraint medical_record_registration_id_fk
        foreign key (registration_id) references registration (id)
)
    comment '病历';

create table if not exists diagnose
(
    id                int        not null
        primary key,
    medical_record_id int        not null,
    disease_id        int        not null,
    date              datetime   not null,
    is_major          tinyint(1) not null comment '是否为确诊',
    constraint diagnose_disease_second_id_fk
        foreign key (disease_id) references disease_second (id),
    constraint diagnose_medical_record_id_fk
        foreign key (medical_record_id) references medical_record (id)
)
    comment '诊断结果';

create table if not exists inspection_application
(
    id                int                  not null
        primary key,
    medical_record_id int                  not null,
    non_drug_id       int                  not null,
    create_time       datetime             not null,
    state             int                  not null comment '-1:作废
0: 暂存状态
1：开立状态
2: 做了项目',
    quantity          int                  not null,
    is_canceled       tinyint(1)           not null,
    is_emerged        tinyint(1) default 0 not null comment '是否为加急项目',
    constraint inspection_application_medical_record_id_fk
        foreign key (medical_record_id) references medical_record (id),
    constraint inspection_application_non_drug_id_fk
        foreign key (non_drug_id) references non_drug (id)
)
    comment 'is_canceled-是否取消项目';

create table if not exists inspection_result
(
    id                        int auto_increment
        primary key,
    operator_id               int      not null,
    department_id             int      not null,
    create_time               datetime not null,
    text                      text     not null,
    picture                   text     not null,
    inspection_application_id int      not null,
    constraint inspection_result_department_id_fk
        foreign key (department_id) references department (id),
    constraint inspection_result_inspection_application_id_fk
        foreign key (inspection_application_id) references inspection_application (id),
    constraint inspection_result_user_id_fk
        foreign key (operator_id) references user (id)
);

create table if not exists prescription
(
    id                int                  not null
        primary key,
    usage_id          int                  not null comment '用法，参照常量表',
    medical_record_id int                  not null,
    frequency         varchar(64)          not null comment '频率',
    drug_id           int                  not null,
    amount            int                  not null,
    days              int                  not null,
    use_amount        varchar(64)          not null comment '单次用量',
    note              varchar(255)         null,
    need_skin_test    tinyint(1) default 0 not null,
    skin_test_result  tinyint(1)           null,
    state             int        default 0 not null comment '处方状态： -1-作废 0-暂存 1-开立',
    constraint prescription_drug_id_fk
        foreign key (drug_id) references drug (id),
    constraint prescription_medical_record_id_fk
        foreign key (medical_record_id) references medical_record (id)
)
    comment '处方';

create table if not exists schedule_rule
(
    id                    int auto_increment
        primary key,
    period                int      not null,
    registration_quantity int      not null,
    operator_id           int      null,
    doctor_id             int      null,
    registration_type_id  int      not null,
    day                   datetime not null,
    constraint schedule_rule_doctor_id_fk
        foreign key (doctor_id) references doctor (id),
    constraint schedule_rule_registration_type_id_fk
        foreign key (registration_type_id) references registration_type (id),
    constraint schedule_rule_user_id_fk
        foreign key (operator_id) references user (id)
)
    comment '排班规则';

