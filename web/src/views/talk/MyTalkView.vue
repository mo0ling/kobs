<template>
  <ContentField>
    <div class="row">
      <div class="col-3">
        <div class="card" id="user">
          <div class="card-body">
            <div class="row">
              <div class="col-4 img-field">
                <img class="img-fluid" :src="$store.state.user.photo"  alt="">
              </div>
              <div  class="col-8 userinfo">
                <div class="username">{{$store.state.user.username}}</div>
                <div class="fans">Followers: {{$store.state.user.followerCount}}</div>
                <div>
                  <button   type="button" class="btn btn-primary btn-sm info-btn">Follow</button>
                  <button   type="button" class="btn btn-warning btn-sm info-btn">取消</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="card edit-field">
          <div class="card-body">
            <label for="edit-post" class="form-label">发帖子</label>
            <textarea v-model="content" class="form-control" id="exampleFormControlTextarea1" rows="3"></textarea>
            <button @click="submitPost"  type="button" class="btn btn-primary btn-sm edit-btn">提交</button>
          </div>
        </div>
      </div>
      <div class="col-9">
        <!-- ActivityPosts -->
        <div class="card">
          <div class="card-body">
            <h1>我的帖子</h1>
            <div>
              <div v-for="post in posts" :key="post.id" class="card single-post">
                <div class="card single-post">
                  <div class="card-body">
                    <span class="content">{{post.content}}</span>
                    <span class="create-time">{{post.createtime}}</span>

                    <button @click="deletePost(post.id)" type="button" class="btn btn-danger btn-sm delete-btn">
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </ContentField>
</template>
<script>
import ContentField from '@/components/ContentField.vue';
import {useStore} from 'vuex';
import {ref} from 'vue';
import $ from 'jquery';

export default {
  name: 'ActivityView',
  components: {
    ContentField,
  },
  setup(){
    const store = useStore();
    let posts = ref([]);
    let content = ref("");
    const refresh_posts = () => {
      $.ajax({
        url: "http://127.0.0.1:3000/user/activity/getlist/",
        type: "get",
        headers: {
          Authorization: "Bearer " + store.state.user.token,
        },
        success(resp) {
          console.log(resp);
          posts.value = resp;
        }
      });
    }
    refresh_posts();
    const submitPost = () =>{
      $.ajax({
        url: "http://127.0.0.1:3000/user/activity/add/",
        type: "post",
        headers: {
          Authorization: "Bearer " + store.state.user.token,
        },
        data:{
          content:content.value,
        },
        success(resp) {
          console.log(resp);
          content.value="";
          refresh_posts();
        }
      });
    }
    const deletePost=(postid)=>{
      $.ajax({
        url: "http://127.0.0.1:3000/user/activity/remove/",
        type: "post",
        headers: {
          Authorization: "Bearer " + store.state.user.token,
        },
        data:{
          activity_id:postid,
        },
        success(resp) {
          console.log(resp);
          refresh_posts();
        }
      });
    }

    return{
      submitPost,
      posts,
      content,
      deletePost,
    }
  }
}
</script>
<style scoped>
img{
  border-radius: 50%;
}
#user{
  background-color: pink;
}
.username{
  font-weight: bold;
}

.fans{
  font-size: 12px;
  color: gray;
}
.content{
  display: inline-block;
  width: 300px;
}
.create-time{
  display: inline-block;
  font-size: 12px;
  color: gray;
  padding-left: 70%;
}

.img-field{
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.userinfo{
  text-align: left;
}
.single-post{
  margin: 10px;
}
.delete-btn{
  float: right;
}
.edit-btn{
  margin-top: 10px;
  float: right;
}
.info-btn{
  padding: 2px 4px;
  font-size: 12px;
  margin-right: 5px;
}
</style>