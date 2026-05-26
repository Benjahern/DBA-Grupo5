import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import 'leaflet/dist/leaflet.css'

// Import leaflet-draw FIRST before any patches, so our override takes precedence
import 'leaflet-draw/dist/leaflet.draw.css'
import * as L from 'leaflet'
import 'leaflet-draw'

// Apply patch after DOM is ready to ensure L.GeometryUtil is fully initialized
// Bug in leaflet-draw 1.0.4 - readableArea uses undeclared 'type' variable
window.patchGeometryUtil = function() {
  if (typeof L !== 'undefined' && L.GeometryUtil && L.GeometryUtil.readableArea) {
    L.GeometryUtil.readableArea = function(area, isMetric, precision) {
      var type = typeof isMetric
      var units, areaStr
      var _precision = L.Util.extend({}, {
        km: 2, ha: 2, m: 0,
        mi: 2, ac: 2, yd: 0
      }, precision)

      if (isMetric) {
        units = ['ha', 'm']
        if (type === 'string') {
          units = [isMetric]
        } else if (type !== 'boolean') {
          units = isMetric
        }

        if (area >= 1000000 && units.indexOf('km') !== -1) {
          areaStr = L.GeometryUtil.formattedNumber(area * 0.000001, _precision['km']) + ' km²'
        } else if (area >= 10000 && units.indexOf('ha') !== -1) {
          areaStr = L.GeometryUtil.formattedNumber(area * 0.0001, _precision['ha']) + ' ha'
        } else {
          areaStr = L.GeometryUtil.formattedNumber(area, _precision['m']) + ' m²'
        }
      } else {
        area /= 0.836127

        if (area >= 3097600) {
          areaStr = L.GeometryUtil.formattedNumber(area / 3097600, _precision['mi']) + ' mi²'
        } else if (area >= 4840) {
          areaStr = L.GeometryUtil.formattedNumber(area / 4840, _precision['ac']) + ' acres'
        } else {
          areaStr = L.GeometryUtil.formattedNumber(area, _precision['yd']) + ' yd²'
        }
      }
      return areaStr
    }
  }
}

createApp(App)
    .use(router)
    .mount('#app')

// Apply patch after Vue mounts to ensure leaflet-draw has initialized L.GeometryUtil
window.patchGeometryUtil()
