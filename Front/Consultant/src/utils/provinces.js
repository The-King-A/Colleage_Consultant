/** 省级行政区标准排序（用于下拉筛选） */
export const CHINA_PROVINCES = [
  '北京', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江',
  '上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南',
  '湖北', '湖南', '广东', '广西', '海南', '重庆', '四川', '贵州',
  '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆',
]

const provinceOrder = new Map(CHINA_PROVINCES.map((p, i) => [p, i]))

/** 按省份顺序排序；未在列表中的项排在末尾并按拼音排序 */
export function sortProvinces(list) {
  if (!list?.length) return []
  return [...list].sort((a, b) => {
    const ia = provinceOrder.has(a) ? provinceOrder.get(a) : 999
    const ib = provinceOrder.has(b) ? provinceOrder.get(b) : 999
    if (ia !== ib) return ia - ib
    return String(a).localeCompare(String(b), 'zh-CN')
  })
}
