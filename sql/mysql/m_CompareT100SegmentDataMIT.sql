-- XuJiao
-- Add primary key cymod (carrier, year, month, origin, destination) and compare the difference
-- use test

alter table t100_segments add column cymod varchar(50);
update t100_segments set cymod = concat(carrier,'_',year,'_',month,'_',origin,'_',destination);
alter table t100_segments_MIT add column cymod varchar(50);
update t100_segments_MIT set cymod = concat(carrier,'_',year,'_',month,'_',origin,'_',destination);

select count(*) from t100_segments where cymod not in (select cymod from t100_segments_MIT); 
-- 81733
-- International airlines
select * from t100_segments where cymod not in (select cymod from t100_segments_MIT) limit 20;

select count(*) from t100_segments_MIT where cymod not in (select cymod from t100_segments);
-- 11
-- Alaska to Alaska airlines
select * from t100_segments_MIT where cymod not in (select cymod from t100_segments);