<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
const themeOverrides = {
  common: {
    primaryColor: "#1a365d",
    primaryColorHover: "#2b6cb0",
    primaryColorPressed: "#153450",
    primaryColorSuppl: "#2b6cb0"
  }
};
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
