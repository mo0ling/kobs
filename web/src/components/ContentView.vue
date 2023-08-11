<template>
  <div class="container content-field">
    <div class="card">
      <div class="card-body">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script>
</script>

<style scoped>
div.content-field {
  margin-top: 50px;
}
.card {
  height: 600px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 2px 1px 19px rgba(0, 0, 0, 0.1);
  background: linear-gradient(to right, #fddb92, #d1fdff);
}
</style>
