package com.chrisimi.casinoplugin.utils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.chrisimi.casinoplugin.serializables.Leaderboardsign.Cycle;

public class CycleHelper
{
	/**
	 * Get start date from the cycle
	 * @param cycleMode 
	 * @return {@link Calendar} instance with the beginning of the cycle
	 */
	public static Calendar getStartDateOfSign(Cycle cycleMode)
	{
		Calendar now = new GregorianCalendar();
		switch (cycleMode)
		{
		case YEAR:
			return new GregorianCalendar(now.get(Calendar.YEAR), 0, 1, 0, 0, 1);
		case MONTH:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1);
		case WEEK:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 2 + (now.get(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_WEEK)), 0, 0, 1);
		case DAY:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		case HOUR:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), 0, 0);
		default:
			return new GregorianCalendar(2000, 1, 0);
		}
	}
	
	/**
	 * Get the end date from the cycle
	 * @param cycleMode
	 * @return {@link Calendar} instance with the ending of the cylce
	 */
	
	public static Calendar getEndDateOfSign(Cycle cycleMode)
	{
		Calendar now = new GregorianCalendar();
		switch (cycleMode)
		{
		case YEAR:
			Calendar nowYear = new GregorianCalendar();
			nowYear.set(Calendar.MONTH, 11);
			return new GregorianCalendar(now.get(Calendar.YEAR), 11, nowYear.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
		case MONTH:
			Calendar nowMonth = new GregorianCalendar();
			nowMonth.set(Calendar.MONTH, nowMonth.get(Calendar.MONTH) + 1);
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, nowMonth.getActualMinimum(Calendar.DAY_OF_MONTH) - 1, 23, 59, 59);
		case WEEK:
			
			
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 2 + (now.get(Calendar.DAY_OF_MONTH) + (7 - now.get(Calendar.DAY_OF_WEEK)) - 1), 23, 59, 59);
		case DAY:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
		case HOUR:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), 59, 59);
		default:
			return new GregorianCalendar(2100, 1, 0);
		}
	}
	
	
	public static String getDateStringFromCycle(Cycle cycleModeCycle, Calendar date)
	{
		return getDateStringFromCycle(cycleModeCycle, date, "§6-------");
	}
	public static String getDateStringFromCycle(Cycle cycleMode, Calendar date, String defaultValue)
	{
		switch (cycleMode)
		{
		case YEAR:
		case MONTH:
		case WEEK:
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			return "§c" + df.format(date.getTime());
		case DAY:	
		case HOUR:
			DateFormat dfa = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			return "§c" + dfa.format(date.getTime());

		default:
			return defaultValue;
		}
	}
}
