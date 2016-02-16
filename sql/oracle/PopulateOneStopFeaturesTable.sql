declare
  seg_year t100_segments.year%type;
  seg_quarter t100_segments.quarter%type;
  seg_month t100_segments.month%type;
  cursor month_cursor is
    select distinct t100.year, 
      t100.quarter, t100.month
    from t100_segments t100
    order by t100.year, t100.quarter,
      t100.month;
begin
  open month_cursor;
  loop
    fetch month_cursor into seg_year, seg_quarter, seg_month;
    exit when month_cursor%notfound;

  insert /*+ append */ into one_stop_features
    (month, first_carrier, second_carrier,
     origin, destination, num_itineraries)
  select seg_month, it.first_operating_carrier,
    it.second_operating_carrier,
    it.origin, it.destination,
    count(distinct it.planned_departure_time)
      as num_itineraries
  from itineraries it
  where it.year = seg_year
    and it.quarter = seg_quarter
    and it.month = seg_month
    and it.num_flights = 2
    and exists
    (
     select *
     from t100_segments
     where year = seg_year
       and quarter = seg_quarter
       and month = seg_month
       and carrier = it.first_operating_carrier
       and origin = it.origin
       and destination = it.connection
       and seats > 0
    )
    and exists
    (
     select *
     from t100_segments
     where year = seg_year
       and quarter = seg_quarter
       and month = seg_month
       and carrier = it.second_operating_carrier
       and origin = it.connection
       and destination = it.destination
       and seats > 0
    )
  group by it.first_operating_carrier,
    it.second_operating_carrier,
    it.origin, it.destination;

  commit;

  end loop;
  close month_cursor;
end;
/


