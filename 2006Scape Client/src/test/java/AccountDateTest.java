import static org.junit.Assert.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.junit.Test;

public class AccountDateTest {
    public static String expected(int a) {
        return LocalDate.of(2002,2,27).plusDays(a).atStartOfDay(ZoneOffset.UTC)
            .withZoneSameInstant(TimeZone.getDefault().toZoneId())
            .format(DateTimeFormatter.ofPattern("d-MMM-uuuu",Locale.ENGLISH));
    }

    @Test public void epochAndAdjacentCountsAreDaysRatherThanSecondsOrMilliseconds() {
        TimeZone a=TimeZone.getDefault();Locale b=Locale.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));Locale.setDefault(Locale.US);
            Game g=new Game();g.currentAccountDay=20000;
            assertEquals("27-Feb-2002",g.formatAccountDay(0));
            assertEquals("26-Feb-2002",g.formatAccountDay(-1));
            assertEquals("28-Feb-2002",g.formatAccountDay(1));
            assertEquals("1-Mar-2002",g.formatAccountDay(2));
            assertEquals("1-Jan-1970",g.formatAccountDay(-11745));
            for(int c=-10000;c<=20000;c+=17)assertEquals(expected(c),g.formatAccountDay(c));
        } finally {TimeZone.setDefault(a);Locale.setDefault(b);}
    }

    @Test public void monthsLeapDaysAndYearEdgesHaveUnpaddedEnglishComponents() {
        TimeZone a=TimeZone.getDefault();Locale b=Locale.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));Locale.setDefault(Locale.FRANCE);
            Game g=new Game();g.currentAccountDay=20000;
            for(String c:new String[]{"1999-12-31","2000-01-01","2000-02-28","2000-02-29","2000-03-01","2004-02-29","2004-03-01","2100-02-28","2100-03-01"}) {
                int d=(int)java.time.temporal.ChronoUnit.DAYS.between(LocalDate.of(2002,2,27),LocalDate.parse(c));
                g.currentAccountDay=d;assertEquals(expected(d),g.formatAccountDay(d));
            }
            g.currentAccountDay=20000;
            for(int c=1;c<=12;c++)for(int d:new int[]{1,9,10,28}) {
                int e=(int)java.time.temporal.ChronoUnit.DAYS.between(LocalDate.of(2002,2,27),LocalDate.of(2004,c,d));
                assertEquals(expected(e),g.formatAccountDay(e));
            }
        } finally {TimeZone.setDefault(a);Locale.setDefault(b);}
    }

    @Test public void defaultTimezoneChangesDisplayedDateWhileInputRemainsUtcDays() {
        TimeZone a=TimeZone.getDefault();Locale b=Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);Game g=new Game();g.currentAccountDay=20000;
            for(String c:new String[]{"UTC","America/Los_Angeles","Pacific/Kiritimati","Asia/Kolkata"}) {
                TimeZone.setDefault(TimeZone.getTimeZone(c));
                for(int d:new int[]{-11745,-1,0,1,2,304,305,730,731,732,20000})assertEquals(c+":"+d,expected(d),g.formatAccountDay(d));
            }
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            assertEquals("26-Feb-2002",g.formatAccountDay(0));
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"));
            assertEquals("27-Feb-2002",g.formatAccountDay(0));
        } finally {TimeZone.setDefault(a);Locale.setDefault(b);}
    }

    @Test public void cutoffIncludesTenAndSignedIntOverflowIsPreserved() {
        TimeZone a=TimeZone.getDefault();Locale b=Locale.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));Locale.setDefault(Locale.US);
            Game g=new Game();
            for(int c:new int[]{-100,0,500,20000}) {
                g.currentAccountDay=c;
                assertEquals(expected(c+9),g.formatAccountDay(c+9));
                assertEquals(expected(c+10),g.formatAccountDay(c+10));
                assertEquals("Unknown",g.formatAccountDay(c+11));
                assertEquals("Unknown",g.formatAccountDay(Integer.MAX_VALUE));
            }
            g.currentAccountDay=Integer.MAX_VALUE;
            assertEquals("Unknown",g.formatAccountDay(0));
            assertEquals("Unknown",g.formatAccountDay(Integer.MAX_VALUE));
        } finally {TimeZone.setDefault(a);Locale.setDefault(b);}
    }
}
