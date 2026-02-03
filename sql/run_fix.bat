@echo off
chcp 65001
echo 修改 priority 字段长度...
"F:\OneDrive\Desk\HLDX\PHP\phpstudy_pro\Extensions\MySQL5.7.26\bin\mysql.exe" -u root DWS -e "ALTER TABLE tech_project MODIFY COLUMN priority VARCHAR(50) COMMENT '优先级';"
if %errorlevel%==0 (
    echo 修改成功！
) else (
    echo 修改失败，请检查数据库连接
)
pause
