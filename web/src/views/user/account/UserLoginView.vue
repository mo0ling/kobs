<template>
  <ContentView v-if="!$store.state.user.loading_info">
    <div class="row justify-content-md-center">
      <div class="pre-box col-6">
        <h1>WELCOME</h1>
        <p>JOIN US!</p>
        <div class="img-box">
          <img src="@/assets/images/favicon1.jpg" alt="" id="avatar" />
        </div>
      </div>
      <div class="col-6">
        <div class="register-box">
          <!-- 标题盒子 -->
          <div class="title-box">
            <h1>注册</h1>
          </div>
          <!-- 输入框盒子 -->
          <div class="row justify-content-md-center">
            <div class="col-6">
              <form @submit.prevent="register">
                <div class="mb-3">
                  <label for="username" class="form-label" style="color:white">用户名</label>
                  <input
                      v-model="registerUser.username"
                      type="text"
                      class="form-control"
                      id="username"
                      placeholder="请输入用户名"
                  />
                </div>
                <div class="mb-3">
                  <label for="password" class="form-label" style="color:white">密码</label>
                  <input
                      v-model="registerUser.password"
                      type="password"
                      class="form-control"
                      id="password"
                      placeholder="请输入密码"
                  />
                </div>
                <div class="mb-3">
                  <label for="password" class="form-label" style="color:white">再次输入</label>
                  <input
                      v-model="registerUser.confirmedPassword"
                      type="password"
                      class="form-control"
                      id="confirmedPassword"
                      placeholder="请再次输入密码"
                  />
                </div>
                <div class="error-message">{{ registerUser.error_message }}</div>
              </form>
            </div>
          </div>
          <div class="btn-box">
            <button type="submit" @click="login">注册</button>
            <p @click="mySwitch">登录</p>
          </div>
        </div>
      </div>
      <div class="col-6">
        <div class="login-box">
          <div class="title-box">
            <h1>登录</h1>
          </div>
          <div class="row justify-content-md-center">
            <div class="col-6">
              <form @submit.prevent="login">
                <div class="mb-3">
                  <label for="username" class="form-label" style="color:white">用户名</label>
                  <input
                      v-model="loginUser.username"
                      type="text"
                      class="form-control"
                      id="username"
                      placeholder="请输入用户名"
                  />
                </div>
                <div class="mb-3">
                  <label for="password" class="form-label" style="color:white">密码</label>
                  <input
                      v-model="loginUser.password"
                      type="password"
                      class="form-control"
                      id="password"
                      placeholder="请输入密码"
                  />
                </div>
                <div class="error-message">{{ loginUser.error_message }}</div>
              </form>
            </div>
          </div>
          <div class="btn-box">
            <button type="submit" @click="login">登录</button>
            <p @click="mySwitch">没账号？请注册</p>
          </div>
        </div>
      </div>
    </div>
  </ContentView>
</template>

<script>
import { ref } from 'vue';
import ContentView from "@/components/ContentView.vue";
import router from "@/router/index";
import {useStore} from "vuex";
import $ from "jquery";
export default{
  components: {
    ContentView,
  },
  setup(){
    const store = useStore();
    const loginUser = ref({
      username: '',
      password: '',
      error_message: ''
    });
    const registerUser = ref({
      username: '',
      password: '',
      confirmPassword: '',
      error_message: ''
    });

    const jwt_token = localStorage.getItem("jwt_token");

    if(jwt_token){
      store.commit("updateToken", jwt_token);
      store.dispatch("getinfo", {
        success(){
          router.push({name: "home"});
          store.commit("updateLoadingInfo",true);
        },
        error(){
          store.commit("updateLoadingInfo",false);
        }
      })
    }else{
      store.commit("updateLoadingInfo",false);
    }
    const login = () => {
      loginUser.value.error_message = "";
      store.dispatch("login", {
        username: loginUser.value.username,
        password: loginUser.value.password,
        success() {
          store.dispatch("getinfo", {
            success() {
              router.push({ name: "home" });
              console.log(store.state.user);
            },
          });
        },
        error() {
          loginUser.value.error_message = "用户名或密码错误";
        },
      });
    };


    let flag = ref(true);
    const mySwitch = () => {
      const pre_box = document.querySelector('.pre-box')
      const img = document.querySelector("#avatar")
      if (flag.value) {
        pre_box.style.transform = "translateX(100%)"
        pre_box.style.backgroundColor = "#d1fdff"
        img.src = require("@/assets/images/favicon1.jpg")
      }
      else {
        pre_box.style.transform = "translateX(0%)"
        pre_box.style.backgroundColor = "#fddb92"
        img.src = require("@/assets/images/favicon1.jpg")
      }
      flag.value = !flag.value
    }

    const register = () => {
      $.ajax({
        url: "http://localhost/api/user/account/register/",
        type: "post",
        data: {
          username: registerUser.value.username,
          password: registerUser.value.password,
          confirmedPassword: registerUser.value.confirmedPassword,
        },
        success(resp) {
          console.log(resp);
          if (resp.error_message === "success") {
            router.push({ name: "user_account_login" });
          } else {
            registerUser.value.error_message = resp.error_message;
          }
        },
        error(resp) {
          console.log(resp);
        },
      });
    };

    return{
      loginUser,
      registerUser,
      login,
      mySwitch,
      register,
    }
  }
}

</script>

<style scoped>
.pre-box {
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  z-index: 99;
  border-radius: 10px;
  background-color: #fddb92;
  /* 动画过渡，先加速再减速 */
  transition: 0.5s ease-in-out;
}
.pre-box h1 {
  margin-top: 150px;
  text-align: center;
  letter-spacing: 5px;
  color: white;
  user-select: none;
  text-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1);
}
.pre-box p {
  height: 30px;
  line-height: 30px;
  text-align: center;
  margin: 20px 0;
  user-select: none;
  font-weight: bold;
  color: white;
  text-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1);
}
.img-box {
  width: 200px;
  height: 200px;
  margin: 20px auto;
  border-radius: 50%;
  user-select: none;
  overflow: hidden;
  box-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1);
}
.img-box img {
  width: 100%;
  transition: 0.5s;
}
.login-box,
.register-box {
  flex: 1;
  height: 100%;
}
.title-box {
  margin-top: 50px;
  height: 150px;
  line-height: 500px;
}
.title-box h1 {
  padding-top: 50px;
  text-align: center;
  color: white;
  /* 禁止选中 */
  user-select: none;
  letter-spacing: 5px;
  text-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1);
}
.el-form {
  display: flex;
  /* 纵向布局 */
  flex-direction: column;
  /* 水平居中 */
  align-items: center;
}
.el-form-item {
  width: 65%;
}
/* 输入框 */
input {
  /* width: 60%; */
  height: 40px;
  margin-bottom: 20px;
  text-indent: 10px;
  border: 1px solid #fff;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 120px;
  /* 增加磨砂质感 */
  backdrop-filter: blur(10px);
}
input:focus {
  /* 光标颜色 */
  color: #b0cfe9;
}
/* 聚焦时隐藏文字 */
input:focus::placeholder {
  opacity: 0;
}
/* 按钮盒子 */
.btn-box {
  display: flex;
  justify-content: center;
}
/* 按钮 */
button {
  width: 100px;
  height: 30px;
  margin: 0 7px;
  line-height: 30px;
  border: none;
  border-radius: 4px;
  background-color: #69b3f0;
  color: white;
}
/* 按钮悬停时 */
button:hover {
  /* 鼠标小手 */
  cursor: pointer;
  /* 透明度 */
  opacity: 0.8;
}
/* 按钮文字 */
.btn-box p {
  height: 30px;
  line-height: 30px;
  /* 禁止选中 */
  user-select: none;
  font-size: 14px;
  color: white;
}
.btn-box p:hover {
  cursor: pointer;
  border-bottom: 1px solid white;
}
.error-message{
  color: red;
}
</style>