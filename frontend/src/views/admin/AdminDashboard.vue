<template>
  <div class="admin-dashboard">
    <div class="dashboard-bg" :style="{ backgroundImage: 'url(/img/背景.jpg)' }">
      <span class="dashboard-title">哔哩轻小说</span>
    </div>
  </div>
</template>

<style scoped>
.admin-dashboard {
  position: absolute;
  top: -24px;
  left: -24px;
  right: -24px;
  bottom: -24px;
  padding: 0;
  overflow: hidden;
}

.dashboard-bg {
  width: 100%;
  height: 100%;
  background-repeat: no-repeat;
  background-position: center center;
  background-size: 100% 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dashboard-title {
  font-size: 3.25rem;
  font-weight: 900;
  font-family: "SimHei", "Microsoft YaHei", "PingFang SC", "Helvetica Neue", sans-serif;
  color: #e91e8c;
  letter-spacing: 0.15em;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2), 0 0 20px rgba(233, 30, 140, 0.3);
}
</style>
