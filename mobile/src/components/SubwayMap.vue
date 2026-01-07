<template>
  <div id="mysubway" class="subway-map"></div>
</template>

<script setup>
import { onMounted, defineEmits, defineExpose } from 'vue'

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

function clearRoute() {
  if (subwayInstance) subwayInstance.clearRoute()
}

function showRoute(from, to) {
  if (subwayInstance) subwayInstance.route(from, to)
}

defineExpose({ clearRoute, showRoute })
</script>

<style scoped>
.subway-map { width: 100%; height: 100%; background: #fff; }
</style>
