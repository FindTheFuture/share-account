function formatTime(time) {
	if (typeof time !== 'number' || time < 0) {
		return time
	}

	var hour = parseInt(time / 3600)
	time = time % 3600
	var minute = parseInt(time / 60)
	time = time % 60
	var second = time

	return ([hour, minute, second]).map(function(n) {
		n = n.toString()
		return n[1] ? n : '0' + n
	}).join(':')
}

function formatLocation(longitude, latitude) {
	if (typeof longitude === 'string' && typeof latitude === 'string') {
		longitude = parseFloat(longitude)
		latitude = parseFloat(latitude)
	}

	longitude = longitude.toFixed(2)
	latitude = latitude.toFixed(2)

	return {
		longitude: longitude.toString().split('.'),
		latitude: latitude.toString().split('.')
	}
}
var dateUtils = {
	UNITS: {
		'年': 31557600000,
		'月': 2629800000,
		'天': 86400000,
		'小时': 3600000,
		'分钟': 60000,
		'秒': 1000
	},
	humanize: function(milliseconds) {
		var humanize = '';
		for (var key in this.UNITS) {
			if (milliseconds >= this.UNITS[key]) {
				humanize = Math.floor(milliseconds / this.UNITS[key]) + key + '前';
				break;
			}
		}
		return humanize || '刚刚';
	},
	format: function(dateStr) {
		var date = this.parse(dateStr)
		var diff = Date.now() - date.getTime();
		if (diff < this.UNITS['天']) {
			return this.humanize(diff);
		}
		var _format = function(number) {
			return (number < 10 ? ('0' + number) : number);
		};
		return date.getFullYear() + '/' + _format(date.getMonth() + 1) + '/' + _format(date.getDate()) + '-' +
			_format(date.getHours()) + ':' + _format(date.getMinutes());
	},
	parse: function(str) { //将"yyyy-mm-dd HH:MM:ss"格式的字符串，转化为一个Date对象
		var a = str.split(/[^0-9]/);
		return new Date(a[0], a[1] - 1, a[2], a[3], a[4], a[5]);
	}
};

// 格式化数字，保留指定小数位数
function formatNumber(num, decimalPlaces = 2) {
  return num.toFixed(decimalPlaces);
}

// 格式化金额展示（无单位），整数省略小数，小数去除尾随0（保留最多两位）
function formatAmount(value) {
  if (value === null || value === undefined) return '0';
  const num = Number(value);
  if (!Number.isFinite(num)) return String(value);
  const fixed = num.toFixed(2);
  return fixed.replace(/\.0+$/, '').replace(/\.$/, '').replace(/\.([1-9])0$/, '.$1');
}

// 账单查询参数规范化（分页默认与上限、dateRange转换）
function normalizeBillQueryParams(params = {}) {
  const out = { ...params };
  // 分页默认与上限
  if (!out.pageNum || out.pageNum <= 0) out.pageNum = 1;
  if (!out.pageSize || out.pageSize <= 0) out.pageSize = 10;
  if (out.pageSize > 10) out.pageSize = 10;
  // 日期范围转换
  if (out.dateRange) {
    const formatDate = (date) => {
      if (!date) return '';
      const d = date instanceof Date ? date : new Date(date);
      if (isNaN(d.getTime())) return '';
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${y}-${m}-${day}`;
    };
    if (Array.isArray(out.dateRange) && out.dateRange.length === 2) {
      const [startDate, endDate] = out.dateRange;
      const fs = formatDate(startDate);
      const fe = formatDate(endDate);
      if (fs && fe) {
        out.startTime = `${fs} 00:00:00`;
        out.endTime = `${fe} 23:59:59`;
      }
    } else if (typeof out.dateRange === 'string') {
      const arr = out.dateRange.split('至');
      if (arr.length === 2) {
        const s = arr[0].trim();
        const e = arr[1].trim();
        out.startTime = `${s} 00:00:00`;
        out.endTime = `${e} 23:59:59`;
      }
    }
    delete out.dateRange;
  }
  return out;
}

// 账单查询参数校验（时间必填与最大30天，结束不早于开始）
// options: { skipDateValidation?: boolean }
function validateBillQueryParams(params = {}, options = {}) {
  const { skipDateValidation = false } = options || {};
  const ret = { ok: true, errorMsg: '' };
  // 首页等场景：允许不校验时间范围
  if (skipDateValidation) {
    return ret;
  }
  if (!params.startTime || !params.endTime) {
    ret.ok = false;
    ret.errorMsg = '请先选择时间范围（最多30天）';
    return ret;
  }
  const toDateOnly = (str) => {
    const d = new Date(String(str).replace(/-/g, '/'));
    return new Date(d.getFullYear(), d.getMonth(), d.getDate());
  };
  const s = toDateOnly(params.startTime);
  const e = toDateOnly(params.endTime);
  if (isNaN(s.getTime()) || isNaN(e.getTime())) {
    ret.ok = false;
    ret.errorMsg = '时间范围格式错误';
    return ret;
  }
  if (e.getTime() < s.getTime()) {
    ret.ok = false;
    ret.errorMsg = '结束时间不能早于开始时间';
    return ret;
  }
  const daysBetween = Math.floor((e.getTime() - s.getTime()) / (1000 * 60 * 60 * 24));
  if (daysBetween > 30) {
    ret.ok = false;
    ret.errorMsg = '时间范围最多允许30天';
    return ret;
  }
  return ret;
}

export {
	formatTime,
	formatLocation,
	dateUtils,
	formatNumber,
	formatAmount,
  normalizeBillQueryParams,
  validateBillQueryParams
}
