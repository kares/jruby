/*
 **** BEGIN LICENSE BLOCK *****
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2018 The JRuby Team
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.ext.date;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.GJChronology;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyFixnum;
import org.jruby.RubyInteger;
import org.jruby.RubyNumeric;
import org.jruby.RubyRational;
import org.jruby.RubyString;
import org.jruby.RubyTime;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;

/**
 * JRuby's <code>DateTime</code> implementation - 'native' parts.
 * In MRI, since 2.x, all of date.rb has been moved to native (C) code.
 *
 * NOTE: There's still date.rb, where this gets bootstrapped from.
 *
 * @see RubyDate
 *
 * @author kares
 */
@JRubyClass(name = "DateTime")
public class RubyDateTime extends RubyDate {

    static RubyClass createDateTimeClass(Ruby runtime, RubyClass Date) {
        RubyClass DateTime = runtime.defineClass("DateTime", Date, ALLOCATOR);
        DateTime.setReifiedClass(RubyDateTime.class);
        DateTime.defineAnnotatedMethods(RubyDateTime.class);
        return DateTime;
    }

    private static final ObjectAllocator ALLOCATOR = new ObjectAllocator() {
        @Override
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new RubyDateTime(runtime, klass, defaultDateTime);
        }
    };

    protected RubyDateTime(Ruby runtime, RubyClass klass) {
        super(runtime, klass);
    }

    public RubyDateTime(Ruby runtime, RubyClass klass, DateTime dt) {
        super(runtime, klass, dt);
    }

    public RubyDateTime(Ruby runtime, DateTime dt) {
        super(runtime, getDateTime(runtime), dt);
    }

    public RubyDateTime(Ruby runtime, long millis, Chronology chronology) {
        super(runtime, getDateTime(runtime), new DateTime(millis, chronology));
    }

    private RubyDateTime(ThreadContext context, RubyClass klass, IRubyObject ajd, int off, int start) {
        super(context, klass, ajd, off, start);
    }

    RubyDateTime(Ruby runtime, RubyClass klass, DateTime dt, int off, int start) {
        super(runtime, klass);

        this.dt = dt;
        this.off = off; this.start = start;
    }

    RubyDateTime(Ruby runtime, RubyClass klass, DateTime dt, int off, int start, int subMillisNum, int subMillisDen) {
        super(runtime, klass);

        this.dt = dt;
        this.off = off; this.start = start;
        this.subMillisNum = subMillisNum; this.subMillisDen = subMillisDen;
    }

    RubyDateTime(ThreadContext context, RubyClass klass, IRubyObject ajd, Chronology chronology, int off) {
        super(context, klass, ajd, chronology, off);
    }

    @Override
    RubyDate newInstance(final ThreadContext context, final DateTime dt, int off, int start, int subNum, int subDen) {
        return new RubyDateTime(context.runtime, getMetaClass(), dt, off, start, subNum, subDen);
    }

    /**
     # Create a new DateTime object corresponding to the specified
     # Civil Date and hour +h+, minute +min+, second +s+.
     #
     # The 24-hour clock is used.  Negative values of +h+, +min+, and
     # +sec+ are treating as counting backwards from the end of the
     # next larger unit (e.g. a +min+ of -2 is treated as 58).  No
     # wraparound is performed.  If an invalid time portion is specified,
     # an ArgumentError is raised.
     #
     # +of+ is the offset from UTC as a fraction of a day (defaults to 0).
     # +sg+ specifies the Day of Calendar Reform.
     #
     # +y+ defaults to -4712, +m+ to 1, and +d+ to 1; this is Julian Day
     # Number day 0.  The time values default to 0.
     **/
    // DateTime.civil([year=-4712[, month=1[, mday=1[, hour=0[, minute=0[, second=0[, offset=0[, start=Date::ITALY]]]]]]]])
    // DateTime.new([year=-4712[, month=1[, mday=1[, hour=0[, minute=0[, second=0[, offset=0[, start=Date::ITALY]]]]]]]])

    @JRubyMethod(name = "civil", alias = "new", meta = true)
    public static RubyDateTime civil(ThreadContext context, IRubyObject self) {
        return new RubyDateTime(context.runtime, (RubyClass) self, defaultDateTime);
    }

    @JRubyMethod(name = "civil", alias = "new", meta = true)
    public static RubyDateTime civil(ThreadContext context, IRubyObject self, IRubyObject year) {
        return new RubyDateTime(context.runtime, (RubyClass) self, civilImpl(context, year));
    }

    @JRubyMethod(name = "civil", alias = "new", meta = true)
    public static RubyDateTime civil(ThreadContext context, IRubyObject self, IRubyObject year, IRubyObject month) {
        return new RubyDateTime(context.runtime, (RubyClass) self, civilImpl(context, year, month));
    }

    //@JRubyMethod(name = "civil", alias = "new", meta = true)
    //public static RubyDateTime civil(ThreadContext context, IRubyObject self, IRubyObject year, IRubyObject month, IRubyObject mday) {
    //    return new RubyDateTime(context.runtime, (RubyClass) self, civilImpl(context, year, month, mday));
    //}

    @JRubyMethod(name = "civil", alias = "new", meta = true, optional = 8)
    public static RubyDateTime civil(ThreadContext context, IRubyObject self, IRubyObject[] args) {
        // year=-4712, month=1, mday=1,
        //  hour=0, minute=0, second=0, offset=0, start=Date::ITALY

        final int len = args.length;

        int hour = 0, minute = 0, second = 0; long millis = 0; int subMillisNum = 0, subMillisDen = 1;
        int off = 0, sg = ITALY;

        if (len == 8) sg = val2sg(context, args[7]);
        if (len >= 7) off = val2off(context, args[6]);

        final int year = (sg > 0) ? getYear(args[0]) : args[0].convertToInteger().getIntValue();
        final int month = getMonth(args[1]);
        final int[] rest = new int[] { 0, 1 };
        final int day = getDay(context, args[2], rest);

        final Chronology chronology = getChronology(context, sg, off);
        DateTime dt = civilDate(context, year, month, day, chronology); // hour: 0, minute: 0, second: 0

        //System.out.println(" 1.day " + day + " rest = " + rest[0] + " / " + rest[1]);

        if (len >= 4 || rest[0] != 0) {
            hour = getHour(context, len >= 4 ? args[3] : RubyFixnum.zero(context.runtime), rest);
        }

        //System.out.println(" 2.hour " + hour + " rest = " + rest[0] + " / " + rest[1]);

        if (len >= 5 || rest[0] != 0) {
            minute = getMinute(context, len >= 5 ? args[4] : RubyFixnum.zero(context.runtime), rest);
        }

        //System.out.println(" 3.minu " + minute + " rest = " + rest[0] + " / " + rest[1]);

        if (len >= 6 || rest[0] != 0) {
            IRubyObject sec = len >= 6 ? args[5] : RubyFixnum.zero(context.runtime);
            second = getSecond(context, sec, rest);
            final int r0 = rest[0], r1 = rest[1];
            if (r0 != 0) {
                millis = ( 1000 * r0 ) / r1;
                subMillisNum = (int) ((1000 * r0) - (millis * r1)); subMillisDen = r1;
            }
        }

        //System.out.println(" 4.seco " + second + " rest = " + rest[0] + " / " + rest[1] + "\n  millis = " + millis);

        try {
            long ms = dt.getMillis();
            ms = chronology.hourOfDay().set(ms, hour);
            ms = chronology.minuteOfHour().set(ms, minute);
            ms = chronology.secondOfMinute().set(ms, second);
            dt = dt.withMillis(ms + millis);
        }
        catch (IllegalArgumentException ex) {
            debug(context, "invalid date", ex);
            throw context.runtime.newArgumentError("invalid date");
        }

        RubyDateTime dateTime = new RubyDateTime(context.runtime, (RubyClass) self, dt, off, sg);
        dateTime.subMillisNum = subMillisNum; dateTime.subMillisDen = subMillisDen;
        return dateTime;
    }

    private static int getDay(ThreadContext context, IRubyObject day, final int[] rest) {
        long d = day.convertToInteger().getLongValue();

        if (day instanceof RubyRational) {
            long num = ((RubyRational) day).getNumerator().getLongValue();
            int den = ((RubyRational) day).getDenominator().getIntValue();
            rest[0] = (int) (num - d * den); rest[1] = den;
        }

        return (int) d;
    }

    private static int getHour(ThreadContext context, IRubyObject hour, final int[] rest) {
        long h = hour.convertToInteger().getLongValue();
        int i = 0;
        final int r0 = rest[0], r1 = rest[1];
        if (r0 != 0) {
            i = (24 * r0) / r1;
            rest[0] = (24 * r0) - (i * r1);
        }
        addRationalModToRest(context, hour, h, rest);

        h += i;
        return (int) (h < 0 ? h + 24 : h); // JODA will handle invalid value
    }

    private static int getMinute(ThreadContext context, IRubyObject val, final int[] rest) {
        long v = val.convertToInteger().getLongValue();
        int i = 0;
        final int r0 = rest[0], r1 = rest[1];
        if (r0 != 0) {
            i = (60 * r0) / r1;
            rest[0] = (60 * r0) - (i * r1);
        }
        addRationalModToRest(context, val, v, rest);

        v += i;
        return (int) (v < 0 ? v + 60 : v); // JODA will handle invalid value
    }

    private static int getSecond(ThreadContext context, IRubyObject sec, final int[] rest) {
        return getMinute(context, sec, rest);
    }

    private static void addRationalModToRest(ThreadContext context, IRubyObject val, long ival, final int[] rest) {
        if (val instanceof RubyRational) {
            long num = ((RubyRational) val).getNumerator().getLongValue();
            int den = ((RubyRational) val).getDenominator().getIntValue();
            num -= ival * den;
            if (num != 0) {
                IRubyObject res = RubyRational.newRational(context.runtime, rest[0], rest[1]).
                        op_add(context, RubyRational.newRationalCanonicalize(context, num, den));
                if (res instanceof RubyRational) {
                    rest[0] = ((RubyRational) res).getNumerator().getIntValue();
                    rest[1] = ((RubyRational) res).getDenominator().getIntValue();
                } else {
                    rest[0] = res.convertToInteger().getIntValue();
                    rest[1] = 1;
                }
            }
        }
    }

    private static void assertValidFraction(ThreadContext context, IRubyObject val, long ival) {
        if (val instanceof RubyRational) {
            IRubyObject eql = ((RubyRational) val).op_equal(context, RubyFixnum.newFixnum(context.runtime, ival));
            if (eql != context.tru) throw context.runtime.newArgumentError("invalid fraction");
        }
    }

    /**
     # Create a new DateTime object corresponding to the specified
     # Julian Day Number +jd+ and hour +h+, minute +min+, second +s+.
     #
     # The 24-hour clock is used.  Negative values of +h+, +min+, and
     # +sec+ are treating as counting backwards from the end of the
     # next larger unit (e.g. a +min+ of -2 is treated as 58).  No
     # wraparound is performed.  If an invalid time portion is specified,
     # an ArgumentError is raised.
     #
     # +of+ is the offset from UTC as a fraction of a day (defaults to 0).
     # +sg+ specifies the Day of Calendar Reform.
     #
     # All day/time values default to 0.
     */ // jd(jd=0, h=0, min=0, s=0, of=0, sg=ITALY)

    @JRubyMethod(name = "jd", meta = true)
    public static RubyDate jd(ThreadContext context, IRubyObject self) { // jd = 0
        return new RubyDateTime(context.runtime, (RubyClass) self, defaultDateTime);
    }

    @JRubyMethod(name = "jd", meta = true, optional = 6)
    public static RubyDate jd(ThreadContext context, IRubyObject self, IRubyObject[] args) {
        final int len = args.length;
        final RubyFixnum zero = RubyFixnum.zero(context.runtime);

        final RubyInteger jd = args[0].convertToInteger();
        final IRubyObject hour = (len > 1) ? args[1] : zero;
        final IRubyObject min = (len > 2) ? args[2] : zero;
        final IRubyObject sec = (len > 3) ? args[3] : zero;

        final RubyNumeric fr;
        if (hour != zero || min != zero || sec != zero) {
            IRubyObject tmp = _valid_time_p(context, self, hour, min, sec);
            if (tmp == context.nil) throw context.runtime.newArgumentError("invalid date");
            fr = (RubyNumeric) tmp;
        }
        else {
            fr = zero;
        }

        int off = 0, sg = ITALY;
        if (len > 4) off = val2off(context, args[4]);
        if (len > 5) sg = val2sg(context, args[5]);

        return new RubyDateTime(context, (RubyClass) self, jd_to_ajd(context, jd, fr, off), off, sg);
    }

    /**
     # Create a new DateTime object representing the current time.
     #
     # +sg+ specifies the Day of Calendar Reform.
     **/

    @JRubyMethod(meta = true)
    public static RubyDateTime now(ThreadContext context, IRubyObject self) { // sg=ITALY
        final DateTimeZone zone = RubyTime.getLocalTimeZone(context.runtime);
        if (zone == DateTimeZone.UTC) {
            return new RubyDateTime(context.runtime, (RubyClass) self, new DateTime(CHRONO_ITALY_UTC));
        }
        final DateTime dt = new DateTime(GJChronology.getInstance(zone));
        final int off = zone.getOffset(dt.getMillis()) / 1000;
        return new RubyDateTime(context.runtime, (RubyClass) self, dt, off, ITALY);
    }

    @JRubyMethod(meta = true)
    public static RubyDateTime now(ThreadContext context, IRubyObject self, IRubyObject sg) {
        final int start = val2sg(context, sg);
        final DateTimeZone zone = RubyTime.getLocalTimeZone(context.runtime);
        final DateTime dt = new DateTime(getChronology(context, start, zone));
        final int off = zone.getOffset(dt.getMillis()) / 1000;
        return new RubyDateTime(context.runtime, (RubyClass) self, dt, off, start);
    }

    @Override
    public IRubyObject prev_day(ThreadContext context, IRubyObject n) {
        return prevNextDay(context, n, true);
    }

    @Override
    public IRubyObject next_day(ThreadContext context, IRubyObject n) {
        return prevNextDay(context, n, false);
    }

    private RubyDate prevNextDay(ThreadContext context, IRubyObject n, final boolean negate) {
        long seconds = timesIntDiff(context, n, DAY_IN_SECONDS);
        if (negate) seconds = -seconds;
        final int days = RubyNumeric.checkInt(context.runtime, seconds / DAY_IN_SECONDS);
        return newInstance(context, this.dt.plusDays(days).plusSeconds((int) (seconds % DAY_IN_SECONDS)), off, start);
    }

    private static final ByteList TO_S_FORMAT = new ByteList(ByteList.plain("%.4d-%02d-%02dT%02d:%02d:%02d%s"), false);

    @JRubyMethod
    public RubyString to_s(ThreadContext context) {
        // format('%.4d-%02d-%02dT%02d:%02d:%02d%s', year, mon, mday, hour, min, sec, zone)
        return format(context, TO_S_FORMAT,
                year(context),
                mon(context),
                mday(context),
                hour(context),
                minute(context),
                second(context),
                zone(context)
        );
    }

    @JRubyMethod // Date.civil(year, mon, mday, @sg)
    public RubyDate to_date(ThreadContext context) {
        return new RubyDate(context.runtime, getDate(context.runtime), dt.withTimeAtStartOfDay(), 0, start);
    }

    @JRubyMethod
    public RubyDateTime to_datetime() { return this; }

    @JRubyMethod // Time.new(year, mon, mday, hour, min, sec + sec_fraction, (@of * 86400.0))
    public RubyTime to_time(ThreadContext context) {
        final Ruby runtime = context.runtime;
        RubyTime time = new RubyTime(runtime, runtime.getTime(), dt);
        if (subMillisNum != 0) {
            RubyNumeric usec = (RubyNumeric)
                    subMillis(runtime).op_mul(context, RubyFixnum.newFixnum(runtime, 1_000_000));
            time.setNSec(usec.getLongValue());
        }
        return time;
    }

    // date/format.rb

    private static final ByteList STRF_FORMAT_BYTES = ByteList.create("%FT%T%:z");

    @JRubyMethod // def strftime(fmt='%FT%T%:z')
    public RubyString strftime(ThreadContext context) {
        return super.strftime(context, RubyString.newStringLight(context.runtime, STRF_FORMAT_BYTES));
    }

    @JRubyMethod // alias_method :format, :strftime
    public RubyString strftime(ThreadContext context, IRubyObject fmt) {
        return super.strftime(context, fmt);
    }

    private static final String DEFAULT_FORMAT = "%FT%T%z";

    @JRubyMethod(meta = true)
    public static IRubyObject _strptime(ThreadContext context, IRubyObject self, IRubyObject string) {
        return parse(context, string, DEFAULT_FORMAT);
    }

    @JRubyMethod(meta = true)
    public static IRubyObject _strptime(ThreadContext context, IRubyObject self, IRubyObject string, IRubyObject format) {
        return RubyDate._strptime(context, self, string, format);
    }

}
