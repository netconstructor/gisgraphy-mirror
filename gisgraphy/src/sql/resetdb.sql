-- usage : psql -UYOURUSER -f /path/to/file/resetdb.sql


\connect gisgraphy
-- we must delete the table because of foreign key constraint 




delete from Airport;
delete from AlternateName;
delete from AmusePark;
delete from Aqueduc;
delete from ATM;
delete from Bank;
delete from Bar;
delete from Bay;
delete from Beach;
delete from Bridge;
delete from Building;
delete from BusStation;
delete from Camp;
delete from Canyon;
delete from Casino;
delete from Castle;
delete from Cemetery;
delete from Cirque;
delete from City;
delete from CitySubdivision;
delete from Cliff;
delete from Coast;
delete from Continent;
delete from country_language;
delete from Country; 
delete from CourtHouse;
delete from CustomsPost;
delete from Dam;
delete from Desert;
delete from Factory;
delete from Falls;
delete from Farm;
delete from Field;
delete from FishingArea;
delete from Fjord;
delete from Forest;
delete from Garden;
delete from GisFeature;
delete from Golf;
delete from Gorge;
delete from GrassLand;
delete from Gulf;
delete from Hill;
delete from Hospital;
delete from Hotel;
delete from House;
delete from Ice;
delete from Island;
delete from Lake;
delete from Language; 
delete from Library;
delete from LightHouse;
delete from Mall;
delete from Marsh;
delete from MetroStation;
delete from Military;
delete from Mill;
delete from Mine;
delete from Mole;
delete from Monument;
delete from Mound;
delete from Mountain;
delete from Museum;
delete from Oasis;
delete from ObservatoryPoint;
delete from Ocean;
delete from OperaHouse;
delete from Park;
delete from Parking;
delete from Plantation;
delete from PolicePost;
delete from PoliticalEntity;
delete from Pond;
delete from openstreetmap;
delete from Port;
delete from PostOffice;
delete from Prison;
delete from Pyramid;
delete from Quay;
delete from Rail;
delete from RailRoadStation;
delete from Ranch;
delete from Ravin;
delete from Reef;
delete from Religious;
delete from Reserve;
delete from Restaurant;
delete from Road;
delete from School;
delete from Sea;
delete from Spring;
delete from Stadium;
delete from Strait;
delete from Stream;
delete from Street;
delete from Theater;
delete from Tower;
delete from Tree;
delete from Tunnel;
delete from UnderSea;
delete from Vineyard;
delete from Volcano;
delete from WaterBody;
delete from Zoo;



-- usage : psql -UYOURUSER -f /path/to/file/resetdb.sql


-- we must delete the table because of foreign key constraint 



DROP TABLE  adm CASCADE;
DROP TABLE  Airport CASCADE;
DROP TABLE  AlternateName CASCADE;
DROP TABLE  AmusePark CASCADE;
DROP TABLE  Aqueduc CASCADE;
DROP TABLE  ATM CASCADE;
DROP TABLE  Bank CASCADE;
DROP TABLE  Bar CASCADE;
DROP TABLE  Bay CASCADE;
DROP TABLE  Beach CASCADE;
DROP TABLE  Bridge CASCADE;
DROP TABLE  Building CASCADE;
DROP TABLE  BusStation CASCADE;
DROP TABLE  Camp CASCADE;
DROP TABLE  Canyon CASCADE;
DROP TABLE  Casino CASCADE;
DROP TABLE  Castle CASCADE;
DROP TABLE  Cemetery CASCADE;
DROP TABLE  Cirque CASCADE;
DROP TABLE  City CASCADE;
DROP TABLE  CitySubdivision CASCADE;
DROP TABLE  Cliff CASCADE;
DROP TABLE  Coast CASCADE;
DROP TABLE  Continent CASCADE;
DROP TABLE  country_language CASCADE;
DROP TABLE  Country CASCADE; 
DROP TABLE  CourtHouse CASCADE;
DROP TABLE  CustomsPost CASCADE;
DROP TABLE  Dam CASCADE;
DROP TABLE  Desert CASCADE;
DROP TABLE  Factory CASCADE;
DROP TABLE  Falls CASCADE;
DROP TABLE  Farm CASCADE;
DROP TABLE  Field CASCADE;
DROP TABLE  FishingArea CASCADE;
DROP TABLE  Fjord CASCADE;
DROP TABLE  Forest CASCADE;
DROP TABLE  Garden CASCADE;
DROP TABLE  GisFeature CASCADE;
DROP TABLE  Golf CASCADE;
DROP TABLE  Gorge CASCADE;
DROP TABLE  GrassLand CASCADE;
DROP TABLE  Gulf CASCADE;
DROP TABLE  Hill CASCADE;
DROP TABLE  Hospital CASCADE;
DROP TABLE  Hotel CASCADE;
DROP TABLE  House CASCADE;
DROP TABLE  Ice CASCADE;
DROP TABLE  Island CASCADE;
DROP TABLE  Lake CASCADE;
DROP TABLE  Language CASCADE; 
DROP TABLE  Library CASCADE;
DROP TABLE  LightHouse CASCADE;
DROP TABLE  Mall CASCADE;
DROP TABLE  Marsh CASCADE;
DROP TABLE  MetroStation CASCADE;
DROP TABLE  Military CASCADE;
DROP TABLE  Mill CASCADE;
DROP TABLE  Mine CASCADE;
DROP TABLE  Mole CASCADE;
DROP TABLE  Monument CASCADE;
DROP TABLE  Mound CASCADE;
DROP TABLE  Mountain CASCADE;
DROP TABLE  Museum CASCADE;
DROP TABLE  Oasis CASCADE;
DROP TABLE  ObservatoryPoint CASCADE;
DROP TABLE  Ocean CASCADE;
DROP TABLE  OperaHouse CASCADE;
DROP TABLE  Openstreetmap CASCADE;
DROP TABLE  Park CASCADE;
DROP TABLE  Parking CASCADE;
DROP TABLE  Plantation CASCADE;
DROP TABLE  PolicePost CASCADE;
DROP TABLE  PoliticalEntity CASCADE;
DROP TABLE  Pond CASCADE;
DROP TABLE  Port CASCADE;
DROP TABLE  PostOffice CASCADE;
DROP TABLE  Prison CASCADE;
DROP TABLE  Pyramid CASCADE;
DROP TABLE  Quay CASCADE;
DROP TABLE  Rail CASCADE;
DROP TABLE  RailRoadStation CASCADE;
DROP TABLE  Ranch CASCADE;
DROP TABLE  Ravin CASCADE;
DROP TABLE  Reef CASCADE;
DROP TABLE  Religious CASCADE;
DROP TABLE  Reserve CASCADE;
DROP TABLE  Restaurant CASCADE;
DROP TABLE  Road CASCADE;
DROP TABLE  School CASCADE;
DROP TABLE  Sea CASCADE;
DROP TABLE  Spring CASCADE;
DROP TABLE  Stadium CASCADE;
DROP TABLE  Strait CASCADE;
DROP TABLE  Stream CASCADE;
DROP TABLE  Street CASCADE;
DROP TABLE  Theater CASCADE;
DROP TABLE  Tower CASCADE;
DROP TABLE  Tree CASCADE;
DROP TABLE  Tunnel CASCADE;
DROP TABLE  UnderSea CASCADE;
DROP TABLE  Vineyard CASCADE;
DROP TABLE  Volcano CASCADE;
DROP TABLE  WaterBody CASCADE;
DROP TABLE  Zoo CASCADE;

VACUUM FULL ANALYZE;


