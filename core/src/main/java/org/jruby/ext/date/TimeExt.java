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
import org.jruby.*;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import static org.jruby.ext.date.DateUtils.*;
import static org.jruby.ext.date.RubyDate.*;

/**
 * Time's extensions from `require 'date'`
 *
 * @author kares
 */
public abstract class TimeExt {

    private TimeExt() { /* no instances */ }

    static void load(Ruby runtime) {
        runtime.getTime().defineAnnotatedMethods(TimeExt.class);
    }

    @JRubyMethod
    public static RubyTime to_time(IRubyObject self) { return (RubyTime) self; }

    @JRubyMethod(name = "to_date")
    public static RubyDate to_date(ThreadContext context, IRubyObject self) {
        final DateTime dt = ((RubyTime) self).getDateTime();
        long jd = civil_to_jd(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(), GREGORIAN);
        return new RubyDate(context, getDate(context.runtime), jd_to_ajd(context, jd), CHRONO_ITALY_UTC, 0);
    }

    @JRubyMethod(name = "to_datetime")
    public static RubyDateTime to_datetime(ThreadContext context, IRubyObject self) {
        final Ruby runtime = context.runtime;

        final DateTime dt = ((RubyTime) self).getDateTime();
        RubyFixnum jd = RubyFixnum.newFixnum(runtime, civil_to_jd(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(), ITALY));

        final RubyFixnum DAY_IN_SECS = RubyFixnum.newFixnum(runtime, DAY_IN_SECONDS);

        int sec = dt.getSecondOfMinute(); // if (sec > 59) sec = 59;
        IRubyObject subsec = RubyRational.newRationalConvert(context, ((RubyTime) self).subsec(), DAY_IN_SECS);
        RubyNumeric fr = timeToDayFraction(context, dt.getHourOfDay(), dt.getMinuteOfHour(), sec);
        fr = (RubyNumeric) ((RubyNumeric) subsec).op_plus(context, fr);

        final int off = dt.getZone().getOffset(dt.getMillis()) / 1000;
        final Chronology chronology = getChronology(context, ITALY, off);
        return new RubyDateTime(context, getDateTime(context.runtime), jd_to_ajd(context, jd, fr, off), chronology, off);
    }

}
