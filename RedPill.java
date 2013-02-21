package com.rally.exercise;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;


//Class to store the individual snapshots
class Snapshot {
  private Calendar validFrom;
	private Calendar validTo;
	private long objectId;
	private String scheduleState;
	
	public Snapshot(Calendar from, Calendar to, long oid, String state){
		validFrom = from;
		validTo = to;
		objectId = oid;
		scheduleState = state;
	}
	
	public Calendar getValidFrom() {
		return validFrom;
	}
	
	public Calendar getValidTo() {
		return validTo;
	}
	
	public String getScheduleState() {
		return scheduleState;
	}
	
	public long getObjectId() {
		return objectId;
	}
}

public class RedPill {
	
	public static void checkMoreAndCorrect() {
		if(hours>24) {
			hours = hours-24;
			days++;
		}
		if(minutes>60) {
			hours++;
			minutes = minutes - 60;
		}
		if(sec>60) {
			minutes++;
			sec = sec - 60;
		}
	}
	
	public static void assignTime(long time) {
		days+= time/(1000*60*60*24);
		time = time%(1000*60*60*24); 
		hours+= time/(1000*60*60);
		time = time%(1000*60*60);
		minutes+= time/(1000*60);
		time = time%(1000*60);
		sec+= time/(1000);
	}

	static int days,hours,sec,minutes;
	
	public static void main(String[] args) {
		
		List<Snapshot> list = new ArrayList<Snapshot>();
		int nofOfDays;
		JSONObject jsonObject;
		JSONArray jsonArray;
		Calendar validFrom, validTo;
		InputStream is = 
                RedPill.class.getResourceAsStream("1000-snapshots-overlap-with-Feb-2012.json");
        String jsonTxt = null;
        long time;
        
		try {
			jsonTxt = IOUtils.toString( is );
		} catch (IOException e) {
			e.printStackTrace();
		}
		Object json = JSONSerializer.toJSON( jsonTxt );
		
		//Checking whether it is a jsonObject
		if(json instanceof JSONObject) {
			jsonObject = (JSONObject) json;
			validFrom = javax.xml.bind.DatatypeConverter.parseDateTime(jsonObject.getString("_ValidFrom"));
			validTo = javax.xml.bind.DatatypeConverter.parseDateTime(jsonObject.getString("_ValidTo"));
			list.add(new Snapshot(validFrom, validTo, jsonObject.getInt("ObjectID"), jsonObject.getString("ScheduleState")));
		}
		//Checking whether it is a jsonArray
		else if(json instanceof JSONArray) { 
			jsonArray = (JSONArray) json;
			for(int i=0;i<jsonArray.size();i++) {
				validFrom = javax.xml.bind.DatatypeConverter.parseDateTime(jsonArray.getJSONObject(i).getString("_ValidFrom"));
				
				validTo = javax.xml.bind.DatatypeConverter.parseDateTime(jsonArray.getJSONObject(i).getString("_ValidTo"));
				list.add(new Snapshot(validFrom, validTo, jsonArray.getJSONObject(i).getLong("ObjectID"), jsonArray.getJSONObject(i).getString("ScheduleState")));
			}
		}

		/*First Question
		During the month of February 2012, how long is spent on each piece of work?*/
		System.out.println("1st Answer\n");
		
		List<Long> l = new ArrayList<Long>();
		List<Long> s = new ArrayList<Long>();
		
		long objId;
		
		for(int i=1;i<list.size();i++) { 
			validFrom = list.get(i).getValidFrom();
			validTo = list.get(i).getValidTo();
			days=0;
			hours=0;
			sec=0;
			minutes=0;
			objId = list.get(i).getObjectId();
			for(int j=i;j<list.size();j++){
				//Checking whether the from date is on or before February and the to date is on or before February
				if((validFrom.get(Calendar.MONTH)) <= 1 && (validFrom.get(Calendar.YEAR)) == 2012 
						&& (validTo.get(Calendar.MONTH)) >= 1 && (validTo.get(Calendar.YEAR)) == 2012 && 
						list.get(j).getObjectId() == objId)  {
					//To consider only the objId items
					if(!(l.contains(objId))) l.add(objId);
					//Checking the from month is February and the to month is greater than equal to February
					if(validFrom.get(Calendar.MONTH) == 1 && validTo.get(Calendar.MONTH) >= 1) {
						nofOfDays = 28 - validFrom.get(Calendar.DAY_OF_MONTH);
						days+= nofOfDays;
						hours+= validFrom.get(Calendar.HOUR_OF_DAY);
						minutes+= validFrom.get(Calendar.MINUTE);
						sec+= validFrom.get(Calendar.SECOND);
						RedPill.checkMoreAndCorrect();
					}
					/*Checking whether the from month is before February and to month
					is after February*/
					else if(validFrom.get(Calendar.MONTH) < 1 && validTo.get(Calendar.MONTH) > 1) {
						days+= 28;
					}
					/*Checking that the from month is before February and the to month 
					is February*/
					else if(validFrom.get(Calendar.MONTH) < 1 && validTo.get(Calendar.MONTH) == 1) {
						days+= validTo.get(Calendar.DAY_OF_MONTH) - 1;
						hours+= validTo.get(Calendar.HOUR_OF_DAY);
						minutes+= validTo.get(Calendar.MINUTE);
						sec+= validTo.get(Calendar.SECOND);
						RedPill.checkMoreAndCorrect();
					}
					//Checking that both the from and to months are in February
					else if(validFrom.get(Calendar.MONTH) == 1 && validTo.get(Calendar.MONTH) == 1) {
						time = validTo.getTimeInMillis() - validFrom.getTimeInMillis();
						RedPill.assignTime(time);
					}
				}
			}
			if(!(s.contains(objId)) && l.contains(objId)) {
				s.add(objId);
				System.out.println("Time spent in Feb 2012 on Item "+objId+" : "+days+"days "+hours+"hours "+minutes+"minutes "+sec+"seconds");
			}
			
		}

		/*Second Question
		What if we only count time during the working hours of Mon-Fri, 9am-5pm (Zulu time)?*/
		System.out.println("\n2nd Answer\n");
		l.clear();
		s.clear();
			
		for(int i=0;i<list.size();i++) { 
			validFrom = list.get(i).getValidFrom();
			validTo = list.get(i).getValidTo();
			days=0;
			hours=0;
			sec=0;
			minutes=0;
			objId = list.get(i).getObjectId();
			for(int j=i;j<list.size();j++){
				//Checking whether the from date is on or before February and the to date is on or before February
				if((validFrom.get(Calendar.MONTH)) <= 1 && (validFrom.get(Calendar.YEAR)) == 2012 
						&& (validTo.get(Calendar.MONTH)) >= 1 && (validTo.get(Calendar.YEAR)) == 2012)  {
					//To consider only the objId items
					if(!(l.contains(objId))) l.add(objId);
					//Checking the from month is February and the to month is greater than equal to February
					if(validFrom.get(Calendar.MONTH) == 1 && validTo.get(Calendar.MONTH) >= 1) {
						nofOfDays = 28 - validFrom.get(Calendar.DAY_OF_MONTH);
						days+= nofOfDays;
						//Considering only the hours, minutes and seconds between 9 am to 5 pm
						if(validFrom.get(Calendar.HOUR_OF_DAY) >=9) {
							if(validFrom.get(Calendar.HOUR_OF_DAY) <=17) {
								hours+= 16 - validFrom.get(Calendar.HOUR_OF_DAY);
								minutes+= 59 - validFrom.get(Calendar.MINUTE);
								sec+= 60 - validFrom.get(Calendar.SECOND);
							}
							else hours+=8;
						}
						RedPill.checkMoreAndCorrect();
					}
					else if(validFrom.get(Calendar.MONTH) < 1 && validTo.get(Calendar.MONTH) > 1) {
						days+= 28;
					}
					else if(validFrom.get(Calendar.MONTH) < 1 && validTo.get(Calendar.MONTH) == 1) {
						days+= validTo.get(Calendar.DAY_OF_MONTH) - 1;
						//Considering only the hours, minutes and seconds between 9 am to 5 pm
						if(validTo.get(Calendar.HOUR_OF_DAY) >=9) {
							if(validTo.get(Calendar.HOUR_OF_DAY) <=17) {
								hours+= validTo.get(Calendar.HOUR_OF_DAY) - 9;
								minutes+= validTo.get(Calendar.MINUTE);
								sec+= validTo.get(Calendar.SECOND);
							}
							else hours+=8;
						}
						RedPill.checkMoreAndCorrect();
					}
					else if(validFrom.get(Calendar.MONTH) == 1 && validTo.get(Calendar.MONTH) == 1) {
						//Considering only the hours, minutes and seconds between 9 am to 5 pm
						if(validFrom.get(Calendar.HOUR_OF_DAY) >=9) {
							if(validFrom.get(Calendar.HOUR_OF_DAY) <=17) {
								hours+= 16 - validFrom.get(Calendar.HOUR_OF_DAY);
								minutes+= 59 - validFrom.get(Calendar.MINUTE);
								sec+= 60 - validFrom.get(Calendar.SECOND);
							}
							else hours+=8;
						}
						//Considering only the hours, minutes and seconds between 9 am to 5 pm
						if(validTo.get(Calendar.HOUR_OF_DAY) >=9) {
							if(validTo.get(Calendar.HOUR_OF_DAY) <=17) {
								hours+= validTo.get(Calendar.HOUR_OF_DAY) - 9;
								minutes+= validTo.get(Calendar.MINUTE);
								sec+= validTo.get(Calendar.SECOND);
							}
							else hours+=8;
						}
						days+=validTo.get(Calendar.DAY_OF_MONTH) - validFrom.get(Calendar.DAY_OF_MONTH) -1;
						RedPill.checkMoreAndCorrect();
					}
				}
			}
			if(!(s.contains(objId)) && l.contains(objId)) {
				s.add(objId);
				System.out.println("Time spent in Feb 2012 on Item "+objId+" : "+days+"days "+hours+"hours "+minutes+"minutes "+sec+"seconds");
			}
		}
		
		/*Third Question
		How long is spent in total in each ScheduleState (across all pieces of work), taking account of working hours?*/
		System.out.println("\n3rd Answer\n");
		
		List<String> l2 = new ArrayList<String>();
		List<String> s2 = new ArrayList<String>();
		
		String schdState;
		
		for(int i=0;i<list.size();i++) { 
			validFrom = list.get(i).getValidFrom();
			validTo = list.get(i).getValidTo();
			days=0;
			hours=0;
			sec=0;
			minutes=0;
			schdState = list.get(i).getScheduleState();
			for(int j=i;j<list.size();j++){
				//Checking whether the from date is on or before February and the to date is on or before February
				if((validFrom.get(Calendar.MONTH)) <= 1 && (validFrom.get(Calendar.YEAR)) == 2012 
						&& (validTo.get(Calendar.MONTH)) >= 1 && (validTo.get(Calendar.YEAR)) == 2012)  {
					//To consider only the current schedule state
					if(!(l2.contains(schdState))) l2.add(schdState);
					//Checking the from month is February and the to month is greater than equal to February
					if(validFrom.get(Calendar.MONTH) == 1 && validTo.get(Calendar.MONTH) >= 1) {
						nofOfDays = 28 - validFrom.get(Calendar.DAY_OF_MONTH);
						days+= nofOfDays;
						//Considering only the hours, minutes and seconds between 9 am to 5 pm
						if(validFrom.get(Calendar.HOUR_OF_DAY) >=9) {
							if(validFrom.get(Calendar.HOUR_OF_DAY) <=17) {
								hours+= 16 - validFrom.get(Calendar.HOUR_OF_DAY);
								minutes+= 59 - validFrom.get(Calendar.MINUTE);
								sec+= 60 - validFrom.get(Calendar.SECOND);
							}
							else hours+=8;
						}
						RedPill.checkMoreAndCorrect();
					}
					else if(validFrom.get(Calendar.MONTH) < 1 && validTo.get(Calendar.MONTH) > 1) {
						days+= 28;
					}
					else if(validFrom.get(Calendar.MONTH) < 1 && validTo.get(Calendar.MONTH) == 1) {
						days+= validTo.get(Calendar.DAY_OF_MONTH) - 1;
						//Considering only the hours, minutes and seconds between 9 am to 5 pm
						if(validTo.get(Calendar.HOUR_OF_DAY) >=9) {
							if(validTo.get(Calendar.HOUR_OF_DAY) <=17) {
								hours+= validTo.get(Calendar.HOUR_OF_DAY) - 9;
								minutes+= validTo.get(Calendar.MINUTE);
								sec+= validTo.get(Calendar.SECOND);
							}
							else hours+=8;
						}
						RedPill.checkMoreAndCorrect();
					}
					else if(validFrom.get(Calendar.MONTH) == 1 && validTo.get(Calendar.MONTH) == 1) {
						//Considering only the hours, minutes and seconds between 9 am to 5 pm
						if(validFrom.get(Calendar.HOUR_OF_DAY) >=9) {
							if(validFrom.get(Calendar.HOUR_OF_DAY) <=17) {
								hours+= 16 - validFrom.get(Calendar.HOUR_OF_DAY);
								minutes+= 59 - validFrom.get(Calendar.MINUTE);
								sec+= 60 - validFrom.get(Calendar.SECOND);
							}
							else hours+=8;
						}
						//Considering only the hours, minutes and seconds between 9 am to 5 pm
						if(validTo.get(Calendar.HOUR_OF_DAY) >=9) {
							if(validTo.get(Calendar.HOUR_OF_DAY) <=17) {
								hours+= validTo.get(Calendar.HOUR_OF_DAY) - 9;
								minutes+= validTo.get(Calendar.MINUTE);
								sec+= validTo.get(Calendar.SECOND);
							}
							else hours+=8;
						}
						days+=validTo.get(Calendar.DAY_OF_MONTH) - validFrom.get(Calendar.DAY_OF_MONTH) -1;
						RedPill.checkMoreAndCorrect();
					}
				}
			}
			if(!(s2.contains(schdState)) && l2.contains(schdState)) {
				s2.add(schdState);
				System.out.println("Time spent in Feb 2012 in Schedule State "+schdState+" : "+days+"days "+hours+"hours "+minutes+"minutes "+sec+"seconds");
			}
		}
	}
}
