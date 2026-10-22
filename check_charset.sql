-- 检查数据库字符集
SELECT
    SCHEMA_NAME,
    DEFAULT_CHARACTER_SET_NAME,
    DEFAULT_COLLATION_NAME
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'real_time_api';

-- 检查表字符集
SELECT
    TABLE_NAME,
    TABLE_COLLATION
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'real_time_api'
AND TABLE_NAME = 'job_cleaned';

-- 检查字段字符集
SELECT
    COLUMN_NAME,
    CHARACTER_SET_NAME,
    COLLATION_NAME,
    COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'real_time_api'
AND TABLE_NAME = 'job_cleaned';

-- 查看几条实际数据
SELECT keyword, province, city, job_title, salary FROM job_cleaned LIMIT 5;