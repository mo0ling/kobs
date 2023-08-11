<template>
    
        <div ref="parent" class="gamemap">
            <canvas ref="canvas" tabindex="0"></canvas>
        </div>
    
</template>
<script>
import {GameMap} from "@/assets/scripts/GameMap";
import {ref, onMounted} from 'vue'
import { useStore } from "vuex";
export default{
  //tabindex=“0” ，表示元素是可聚焦的，并且可以通过键盘导航来聚焦到该元素
    setup(){
        const store = useStore();

        let parent = ref(null);
        let canvas = ref(null);
        onMounted(() => {
          console.log(parent.value)
            store.commit("updateGameObject", new GameMap(canvas.value.getContext('2d'), parent.value, store));
            //canvas标签用于绘制图像（通过脚本，通常是Js）,“2d”,创建一个CanvasRenderingContext2D对象作为2D渲染的上下文。
            //getContext() 方法可返回一个对象，该对象提供了用于在画布上绘图的方法和属性。
        });

        return {
            parent,
            canvas
        }
    }
}
</script>
<style scoped>
div.gamemap {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}

</style>