var _mon = new Array('JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC');
var _day = new Array('SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT');

function _isGmt(i) {
    return typeof i == 'string' && i == 'GMT';
}

function dnsDomainIs(host, domain) {
    if (domain != null && domain.charAt(0) != '.') return false;
    return shExpMatch(host, '*' + domain);
}

function isInNet(host, pattern, mask) {
    if (typeof host != 'string' || typeof pattern != 'string' || typeof mask != 'string') return false;
    if (dnsDomainLevels(pattern) != 3 || dnsDomainLevels(mask) != 3) return false;
    var offset1 = 0, offset2 = 0, ofs1 = 0, ofs2 = 0;
    var m = '', result;
    for (var i = 0; i < 4; i++) {
        ofs1 = pattern.indexOf('.', offset1);
        ofs2 = mask.indexOf('.', offset2);
        if (i != 3) result = (pattern.substring(offset1, ofs1) - 0) & (mask.substring(offset2, ofs2) - 0); else result = (pattern.substring(offset1) & mask.substring(offset2));
        if (result == 0) result = '*';
        m = m + result;
        if (i != 3) m = m + '.';
        offset1 = ofs1 + 1;
        offset2 = ofs2 + 1;
    }
    return shExpMatch(host, m);
}

function isInNet(host, pattern, mask) {
    if (typeof host != 'string' || typeof pattern != 'string' || typeof mask != 'string') return false;
    if (dnsDomainLevels(pattern) != 3 || dnsDomainLevels(mask) != 3) return false;
    var hostInIpFormat = dnsResolve(host);
    var offset1 = 0, offset2 = 0, ofs1 = 0, ofs2 = 0;
    var m = '', result;
    for (var i = 0; i < 4; i++) {
        ofs1 = pattern.indexOf('.', offset1);
        ofs2 = mask.indexOf('.', offset2);
        if (i != 3) result = (pattern.substring(offset1, ofs1) - 0) & (mask.substring(offset2, ofs2) - 0); else result = (pattern.substring(offset1) & mask.substring(offset2));
        if (result == 0) result = '*';
        m = m + result;
        if (i != 3) m = m + '.';
        offset1 = ofs1 + 1;
        offset2 = ofs2 + 1;
    }
    return shExpMatch(hostInIpFormat, m);
}

function isPlainHostName(host) {
    return (dnsDomainLevels(host) == 0);
}

function isResolvable(host) {
    return (dnsResolve(host) != '');
}

function localHostOrDomainIs(host, hostdom) {
    return shExpMatch(hostdom, host + '*');
}

function dnsDomainLevels(host) {
    var s = host + '';
    for (var i = 0, j = 0; i < s.length; i++) if (s.charAt(i) == '.') j++;
    return j;
}

function dnsResolve(host) {
    if (typeof host != 'string' || dnsDomainLevels(host) != 3) return '';
    for (var i = 0; i < host.length; i++) if ((host.charAt(i) < '0' || host.charAt(i) > '9') && host.charAt(i) != '.') return '';
    return host;
}

function dnsResolve(host) {
    if (typeof host != 'string') return '';
    var isIpFormat = false;
    if (dnsDomainLevels(host) == 3) {
        for (var i = 0; i < host.length; i++) {
            if ((host.charAt(i) >= '0' && host.charAt(i) <= '9') || host.charAt(i) == '.') isIpFormat = true; else {
                isIpFormat = false;
                break;
            }
        }
    }
}

function myIpAddress() {
    return '';
}

function shExpMatch(str, shexp) {
    if (typeof str != 'string' || typeof shexp != 'string') return false;
    if (shexp == '*') return true;
    if (str == '' && shexp == '') return true;
    str = str.toLowerCase();
    shexp = shexp.toLowerCase();
    var index = shexp.indexOf('*');
    if (index == -1) {
        return (str == shexp);
    } else if (index == 0) {
        for (var i = 0; i <= str.length; i++) {
            if (shExpMatch(str.substring(i), shexp.substring(1))) return true;
        }
        return false;
    } else {
        var sub = null, sub2 = null;
        sub = shexp.substring(0, index);
        if (index <= str.length) sub2 = str.substring(0, index);
        if (sub != '' && sub2 != '' && sub == sub2) {
            return shExpMatch(str.substring(index), shexp.substring(index));
        } else {
            return false;
        }
    }
}

function _dateRange(day1, month1, year1, day2, month2, year2, gmt) {
    if (typeof day1 != 'number' || day1 <= 0 || typeof month1 != 'string' || typeof year1 != 'number' || year1 <= 0 || typeof day2 != 'number' || day2 <= 0 || typeof month2 != 'string' || typeof year2 != 'number' || year2 <= 0 || typeof gmt != 'boolean') return false;
    var m1 = -1, m2 = -1;
    for (var i = 0; i < _mon.length; i++) {
        if (_mon[i] == month1) m1 = i;
        if (_mon[i] == month2) m2 = i;
    }
    var cur = new Date();
    var d1 = new Date(year1, m1, day1, 0, 0, 0);
    var d2 = new Date(year2, m2, day2, 23, 59, 59);
    if (gmt == true) cur = new Date(cur.getTime() - cur.getTimezoneOffset() * 60 * 1000);
    return ((d1.getTime() <= cur.getTime()) && (cur.getTime() <= d2.getTime()));
}

function dateRange(p1, p2, p3, p4, p5, p6, p7) {
    var cur = new Date();
    if (typeof p1 == 'undefined') return false; else if (typeof p2 == 'undefined' || _isGmt(p2)) {
        if ((typeof p1) == 'string') return _dateRange(1, p1, cur.getFullYear(), 31, p1, cur.getFullYear(), _isGmt(p2)); else if (typeof p1 == 'number' && p1 > 31) return _dateRange(1, 'JAN', p1, 31, 'DEC', p1, _isGmt(p2)); else {
            for (var i = 0; i < _mon.length; i++) if (_dateRange(p1, _mon[i], cur.getFullYear(), p1, _mon[i], cur.getFullYear(), _isGmt(p2))) return true;
            return false;
        }
    } else if (typeof p3 == 'undefined' || _isGmt(p3)) {
        if ((typeof p1) == 'string') return _dateRange(1, p1, cur.getFullYear(), 31, p2, cur.getFullYear(), _isGmt(p3)); else if (typeof p1 == 'number' && typeof p2 == 'number' && (p1 > 31 || p2 > 31)) return _dateRange(1, 'JAN', p1, 31, 'DEC', p2, _isGmt(p3)); else {
            if ((typeof p2) == 'string') {
                return _dateRange(p1, p2, cur.getFullYear(), p1, p2, cur.getFullYear(), _isGmt(p3));
            } else {
                for (var i = 0; i < _mon.length; i++) if (_dateRange(p1, _mon[i], cur.getFullYear(), p2, _mon[i], cur.getFullYear(), _isGmt(p3))) return true;
                return false;
            }
        }
    } else if (typeof p4 == 'undefined' || _isGmt(p4)) return _dateRange(p1, p2, p3, p1, p2, p3, _isGmt(p4)); else if (typeof p5 == 'undefined' || _isGmt(p5)) {
        if (typeof p2 == 'number') return _dateRange(1, p1, p2, 31, p3, p4, _isGmt(p5)); else return _dateRange(p1, p2, cur.getFullYear(), p3, p4, cur.getFullYear(), _isGmt(p5))
    } else if (typeof p6 == 'undefined') return false; else return _dateRange(p1, p2, p3, p4, p5, p6, _isGmt(p7));
}

function timeRange(p1, p2, p3, p4, p5, p6, p7) {
    if (typeof p1 == 'undefined') return false; else if (typeof p2 == 'undefined' || _isGmt(p2)) return _timeRange(p1, 0, 0, p1, 59, 59, _isGmt(p2)); else if (typeof p3 == 'undefined' || _isGmt(p3)) return _timeRange(p1, 0, 0, p2, 0, 0, _isGmt(p3)); else if (typeof p4 == 'undefined') return false; else if (typeof p5 == 'undefined' || _isGmt(p5)) return _timeRange(p1, p2, 0, p3, p4, 0, _isGmt(p5)); else if (typeof p6 == 'undefined') return false; else return _timeRange(p1, p2, p3, p4, p5, p6, _isGmt(p7));
}

function _timeRange(hour1, min1, sec1, hour2, min2, sec2, gmt) {
    if (typeof hour1 != 'number' || typeof min1 != 'number' || typeof sec1 != 'number' || hour1 < 0 || min1 < 0 || sec1 < 0 || typeof hour2 != 'number' || typeof min2 != 'number' || typeof sec2 != 'number' || hour2 < 0 || min2 < 0 || sec2 < 0 || typeof gmt != 'boolean') return false;
    var cur = new Date();
    var d1 = new Date();
    var d2 = new Date();
    d1.setHours(hour1);
    d1.setMinutes(min1);
    d1.setSeconds(sec1);
    d2.setHours(hour2);
    d2.setMinutes(min2);
    d2.setSeconds(sec2);
    if (gmt == true) cur = new Date(cur.getTime() - cur.getTimezoneOffset() * 60 * 1000);
    return ((d1.getTime() <= cur.getTime()) && (cur.getTime() <= d2.getTime()));
}

function weekdayRange(wd1, wd2, gmt) {
    if (typeof wd1 == 'undefined') return false; else if (typeof wd2 == 'undefined' || _isGmt(wd2)) return _weekdayRange(wd1, wd1, _isGmt(wd2)); else return _weekdayRange(wd1, wd2, _isGmt(gmt));
}

function _weekdayRange(wd1, wd2, gmt) {
    if (typeof wd1 != 'string' || typeof wd2 != 'string' || typeof gmt != 'boolean') return false;
    var w1 = -1, w2 = -1;
    for (var i = 0; i < _day.length; i++) {
        if (_day[i] == wd1) w1 = i;
        if (_day[i] == wd2) w2 = i;
    }
    var cur = new Date();
    if (gmt == true) cur = new Date(cur.getTime() - cur.getTimezoneOffset() * 60 * 1000);
    var w3 = cur.getDay();
    if (w1 > w2) w2 = w2 + 7;
    if (w1 > w3) w3 = w3 + 7;
    return (w1 <= w3 && w3 <= w2);
}

