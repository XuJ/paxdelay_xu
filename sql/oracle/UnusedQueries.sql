
create table
  tempInnovataParents
as
  select iv.record_id as childID, prt.record_id as parentID
  from innovata iv
  join innovata prt
    on iv.origin = prt.origin
    and iv.destination = prt.destination
    and prt.carrier = substr(iv.codeshare_info, 1, 
      decode(instr(iv.codeshare_info, ' ', 1, 1), 0, length(iv.codeshare_info),
        instr(iv.codeshare_info, ' ', 1, 1) + 1))
    and prt.flight_number = decode(instr(iv.codeshare_info, ' ', 1, 1),
      0, iv.flight_number,
      substr(iv.codeshare_info, instr(iv.codeshare_info, ' ', 1, 1) + 1,
        length(iv.codeshare_info) - instr(iv.codeshare_info, ' ', 1, 1)))
    and to_date(prt.effective_start, 'DD-MM-YYYY') <=
      to_date(iv.effective_start, 'DD-MM-YYYY')
    and to_date(prt.effective_end, 'DD-MM-YYYY') >=
      to_date(iv.effective_end, 'DD-MM-YYYY')
    and (prt.sunday_flag = 1 or iv.sunday_flag = 0)
    and (prt.monday_flag = 1 or iv.monday_flag = 0)
    and (prt.tuesday_flag = 1 or iv.tuesday_flag = 0)
    and (prt.wednesday_flag = 1 or iv.wednesday_flag = 0)
    and (prt.thursday_flag = 1 or iv.thursday_flag = 0)
    and (prt.friday_flag = 1 or iv.friday_flag = 0)
    and (prt.saturday_flag = 1 or iv.saturday_flag = 0)
  where iv.codeshare_flag = 1;
  
create table
  tempOfferingParents
as
  select coff.id as childFlight, poff.id as parentFlight
  from tempInnovataParents
  join offerings coff
    on coff.innovata_id = tempInnovataParents.childID
  join offerings poff
    on poff.innovata_id = tempInnovataParents.parentID;

select count(*) from tempInnovataParents;
select count(*) from tempOfferingParents;

select count(*) from innovata
where codeshare_flag = 0
and length(codeshare_info) > 0;

select count(*) from innovata
where codeshare_flag = 0
and instr(codeshare_info, ' ', 1, 3) != 0;

update offerings coff
set coff.parent_id =
(select parentFlight
 from tempOfferingParents
 where childFlight = coff.id);
 
select count(*)
from offerings
where parent_id is not null;

update offerings coff
set coff.parent_id =
(
  where iv.codeshare_flag = 1 and iv.record_id = coff.innovata_id
)