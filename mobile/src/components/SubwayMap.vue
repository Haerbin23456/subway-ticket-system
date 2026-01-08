<template>
  <div id="mysubway" class="subway-map"></div>
</template>

<script setup>
import {onMounted} from 'vue'

const emit = defineEmits(['select'])
let subwayInstance = null

onMounted(() => {
  setTimeout(() => {
    initSubway()
  }, 500)
})

function initSubway() {
  if (!window.subway) return
  
  const mySubway = window.subway("mysubway", {
      adcode: 3301, 
      theme: "colorful",
      client: 0,
      doubleclick: { min: 0.5, max: 3.0 }
  })
  
  subwayInstance = mySubway

  mySubway.event.on("subway.complete", function() {
      mySubway.scale(0.6)
  })

  mySubway.event.on("station.touch", function(ev, info) {
      emit('select', { id: info.id, name: info.name })
  })
}

function showRoute(from, to) {
  if (subwayInstance) {
    // Set start and end markers explicitly (as requested by user documentation)
    subwayInstance.setStart(from);
    subwayInstance.setEnd(to);
    
    // Draw the route with close button enabled
    subwayInstance.route(from, to, { 
      closeBtn: true
    })
    
    // Monitor for close button clicks?
    // Since we can't easily hook into the library's close event, 
    // we can try a workaround: periodically check or re-assert markers? 
    // No, that's inefficient.
    
    // BETTER IDEA:
    // If the library clears markers when route is closed, let's restore them!
    // But we don't know WHEN it is closed.
    
    // Hack:
    // Listen to ANY click on the map container. If the user clicked the "X", the route disappears.
    // We can check if the route layer is gone?
    // Or just re-apply markers whenever `showRoute` is active but markers are missing?
    
    // Wait, the user said: "能不能就不停地设置起点终点标记".
    // We can use a setInterval to keep setting them while the route is active?
    // That might be too aggressive/flickering.
    
    // Alternative: 
    // When the user clicks the "X", the route is cleared.
    // The library doesn't seem to expose a "routeClosed" event.
    // But maybe we can listen to the DOM?
    
    // Let's go with the user's suggestion: "不停地设置" (Keep setting them).
    // Maybe not "constantly", but ensuring they are there.
    
    // Actually, if we just re-enable the Close button (`closeBtn: true`), 
    // and then use a MutationObserver or similar to detect when the route is removed?
    // Or simply: When the user selects a station, we ALWAYS call `setStart/setEnd`.
    
    // The problem is: 
    // 1. User sees route + X button.
    // 2. User clicks X. Route gone. Markers gone (Library behavior).
    // 3. User is now looking at map with NO markers.
    // 4. User wants markers to be there so they can modify one of them?
    
    // If markers are gone, user has to re-select.
    // If we want markers to PERSIST after closing route, we need to know when route closes.
    
    // Let's try to attach a click listener to the close button if we can find it in DOM?
    // It's likely inside the canvas/SVG, so DOM listener won't work easily.
    
    // Let's try the setInterval approach but only check if markers are missing? 
    // The library API doesn't seem to have `hasMarker`.
    
    // Okay, let's re-enable the close button first.
    // And then add a "watchdog" that re-adds markers every 1 second?
    // Or better: when `showRoute` is called, start a timer that re-sets Start/End every 500ms?
    // And stop it when `clearRoute` is called explicitly by our code?
    
    startMarkerWatchdog(from, to);
  }
}

let watchdogTimer = null;

function startMarkerWatchdog(from, to) {
  stopMarkerWatchdog();
  watchdogTimer = setInterval(() => {
    if (subwayInstance) {
        // Re-apply markers to ensure they stay even if user closes route
        subwayInstance.setStart(from);
        subwayInstance.setEnd(to);
    }
  }, 1000); // Check every second
}

function stopMarkerWatchdog() {
  if (watchdogTimer) {
    clearInterval(watchdogTimer);
    watchdogTimer = null;
  }
}

function clearRoute() {
  stopMarkerWatchdog();
  if (subwayInstance) {
    subwayInstance.clearRoute()
  }
}

function setStart(name) {
  if (subwayInstance) subwayInstance.setStart(name)
}

function setEnd(name) {
  if (subwayInstance) subwayInstance.setEnd(name)
}

function centerStation(name) {
  if (subwayInstance) {
    // Clear any existing info window first to be safe
    subwayInstance.clearInfoWindow();
    
    // Get center coordinates and set map center
    const center = subwayInstance.getStCenter(name);
    if (center) {
      subwayInstance.setCenter(center);
    }
  }
}

defineExpose({ clearRoute, showRoute, centerStation, setStart, setEnd })
</script>

<style scoped>
.subway-map { width: 100%; height: 100%; background: #fff; }
</style>
