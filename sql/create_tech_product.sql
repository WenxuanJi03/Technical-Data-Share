-- ============================================
-- 产品清单表 (完整对应 DX-产品清单.xlsm 全部列)
-- 包含：基础信息 + 基础参数 + 准备阶段 + 开发里程碑 + 量产里程碑 + 持续改进
-- ============================================
DROP TABLE IF EXISTS tech_product;
CREATE TABLE tech_product (
  product_id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '产品ID',

  -- ========== 基础信息（A~Q列）==========
  serial_no            INT           DEFAULT NULL              COMMENT '序号',
  wheel_code           VARCHAR(50)   NOT NULL                  COMMENT '轮型号（如05115C09）',
  front_image          VARCHAR(500)  DEFAULT NULL              COMMENT '正面图路径',
  customer             VARCHAR(100)  DEFAULT NULL              COMMENT '客户（如丰田）',
  ship_location        VARCHAR(100)  DEFAULT NULL              COMMENT '发货地（如广州）',
  product_type         VARCHAR(50)   DEFAULT NULL              COMMENT '产品类型（外部调入/常规转移等）',
  product_source       VARCHAR(50)   DEFAULT NULL              COMMENT '产品来源（散装调拨/集团转移等）',
  first_mold_no        VARCHAR(50)   DEFAULT NULL              COMMENT '首模号',
  mold_source          VARCHAR(50)   DEFAULT NULL              COMMENT '首模来源',
  product_status       VARCHAR(30)   DEFAULT '量产'            COMMENT '产品状态（量产/试制小批量/退市停产/转移）',
  status_remark        VARCHAR(500)  DEFAULT NULL              COMMENT '状态备注',
  similar_wheels       VARCHAR(500)  DEFAULT NULL              COMMENT '相似轮型',
  similar_diff         VARCHAR(500)  DEFAULT NULL              COMMENT '相似轮型差异点',
  size_spec            VARCHAR(50)   DEFAULT NULL              COMMENT '规格（如1775）',
  offset_et            VARCHAR(20)   DEFAULT NULL              COMMENT '偏距ET（如ET45）',
  pcd                  VARCHAR(20)   DEFAULT NULL              COMMENT 'PCD（如5-114.3）',
  center_hole          VARCHAR(20)   DEFAULT NULL              COMMENT '中心孔直径（如O60）',

  -- ========== 基础参数（R~U列）==========
  design_weight        VARCHAR(20)   DEFAULT NULL              COMMENT '设计单重(kg)',
  surface_treatment    VARCHAR(50)   DEFAULT NULL              COMMENT '表面处理（精车/全涂装等）',
  color                VARCHAR(50)   DEFAULT NULL              COMMENT '颜色（如11BK19、11SV14）',
  label_info           VARCHAR(200)  DEFAULT NULL              COMMENT '标签（如Ø15蓝色底点）',

  -- ========== 准备阶段里程碑（V~AC列）==========
  transfer_time        VARCHAR(30)   DEFAULT NULL              COMMENT '转移时间',
  internal_eval_time   VARCHAR(30)   DEFAULT NULL              COMMENT '内评时间',
  quality_check_time   VARCHAR(30)   DEFAULT NULL              COMMENT '工检清单时间',
  mold_open_time       VARCHAR(30)   DEFAULT NULL              COMMENT '开模时间',
  quality_arrival_time VARCHAR(30)   DEFAULT NULL              COMMENT '工检全部到厂时间',
  sample_wheel_time    VARCHAR(30)   DEFAULT NULL              COMMENT '样轮到厂时间',
  paint_arrival_time   VARCHAR(30)   DEFAULT NULL              COMMENT '油漆到厂时间',
  drawing_issue_time   VARCHAR(30)   DEFAULT NULL              COMMENT '图纸文件全部签发时间',

  -- ========== 产品开发里程碑 ==========
  fea_result           VARCHAR(200)  DEFAULT NULL              COMMENT 'FEA分析结果',
  first_mold_arrival   VARCHAR(30)   DEFAULT NULL              COMMENT '首模到厂时间',
  first_machine_time   VARCHAR(30)   DEFAULT NULL              COMMENT '首上机时间',
  sample_submit_time   VARCHAR(30)   DEFAULT NULL              COMMENT '送样时间',
  sample_pass_time     VARCHAR(30)   DEFAULT NULL              COMMENT '样件合格时间',
  sample_history       VARCHAR(500)  DEFAULT NULL              COMMENT '送样履历',
  trial_summary_time   VARCHAR(30)   DEFAULT NULL              COMMENT '试制总结时间',

  -- ========== 量产阶段里程碑 ==========
  trial_situation      VARCHAR(500)  DEFAULT NULL              COMMENT '试制情况',
  batch_summary_time   VARCHAR(30)   DEFAULT NULL              COMMENT '小批量总结时间',
  batch_situation      VARCHAR(500)  DEFAULT NULL              COMMENT '小批量情况',
  impact_delivery_time VARCHAR(30)   DEFAULT NULL              COMMENT '影响交付时间',
  impact_delivery_item VARCHAR(500)  DEFAULT NULL              COMMENT '影响交付事项',
  mass_prod_time       VARCHAR(30)   DEFAULT NULL              COMMENT '量产时间',

  -- ========== 持续改进里程碑 ==========
  latest_change_time   VARCHAR(30)   DEFAULT NULL              COMMENT '最新变更断点关闭时间',
  latest_change_content VARCHAR(500) DEFAULT NULL              COMMENT '最新变更内容',
  control_points       VARCHAR(500)  DEFAULT NULL              COMMENT '控制要点',

  -- ========== 系统字段 ==========
  del_flag             CHAR(1)       DEFAULT '0'               COMMENT '删除标志（0存在 2删除）',
  create_by            VARCHAR(64)   DEFAULT ''                COMMENT '创建者',
  create_time          DATETIME      DEFAULT NULL              COMMENT '创建时间',
  update_by            VARCHAR(64)   DEFAULT ''                COMMENT '更新者',
  update_time          DATETIME      DEFAULT NULL              COMMENT '更新时间',
  remark               VARCHAR(500)  DEFAULT NULL              COMMENT '备注',
  PRIMARY KEY (product_id),
  KEY idx_wheel_code (wheel_code),
  KEY idx_customer (customer),
  KEY idx_product_status (product_status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='产品清单（完整对应DX-产品清单全部列）';

-- 产品清单菜单（如果已执行过上一版SQL，先删除旧菜单再执行）
-- INSERT INTO sys_menu ...（菜单SQL与上一版相同，此处省略，只需执行一次）
