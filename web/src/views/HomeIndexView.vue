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
        <div class="card">
          <div class="card-body">
            <div class="card user" v-for="talk in talks" :key="talk.id">
              <div class="card-body">
                <div class="row">
                  <div class="col-1 img-field">
                    <img class="img-fluid" :src="talk.photo" alt="">
                  </div>
                  <div class="col-11 userinfo">
                    <div class="username">{{talk.username}}</div>
                    <div class="content">{{talk.content}}
                    </div>
                    <div class="create-time">{{talk.createtime}}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>
        <ul class="pagination" style="float: right">
          <li class="page-item" @click="click_page(-2)">
            <a class="page-link" href="#">前一页</a>
          </li>
          <li
              :class="'page-item ' + page.is_active"
              v-for="page in pages"
              :key="page.number"
              @click="click_page(page.number)"
          >
            <a class="page-link" href="#">{{ page.number }}</a>
          </li>
          <li class="page-item" @click="click_page(-1)">
            <a class="page-link" href="#">后一页</a>
          </li>
        </ul>
      </div>
    </div>
  </ContentField>
</template>
<script>

import ContentField from '@/components/ContentField.vue';
import { ref } from 'vue';
import $ from 'jquery';
export default{
  components:{
    ContentField,
  },
  setup(){
    let current_page = 1;
    let talks = ref([]);
    let total_talks = 0;
    let pages = ref([]);

    const click_page = (page) => {
      if (page === -2) {
        page = current_page - 1;
      } else if (page === -1) {
        page = current_page + 1;
      }
      let max_pages = parseInt(Math.ceil(total_talks / 10));
      if (page >= 1 && page <= max_pages) {
        pull_page(page);
      }
    };

    const update_pages = () => {
      let max_pages = parseInt(Math.ceil(total_talks / 10));
      let new_pages = [];
      for (let i = current_page - 2; i <= current_page + 2; i++) {
        if (i >= 1 && i <= max_pages) {
          new_pages.push({
            number: i,
            is_active: i === current_page ? "active" : "",
          });
        }
      }
      pages.value = new_pages;
    };

    const pull_page = (page) => {
      current_page = page;
      $.ajax({
        url: "http://localhost/api/talk/getAllList/",
        data: {
          page,
        },
        type: "get",
        success(resp) {
          talks.value = resp.talks;
          total_talks = resp.talks_count;
          update_pages();
        },
        error(resp) {
          console.log(resp);
        },
      });
    };

    pull_page(current_page);
    return {
      talks,
      total_talks,
      pages,
      click_page,
    };
  }
}
</script>
<style scoped>
.user{
  margin-top: 15px;
  cursor: pointer;
}
img{
  border-radius: 50%;
}
#user{
  background-color: pink;
}
.username{
  font-weight: bold;
  height: 50%;
}
.userinfo{
  text-align: left;
}
.user:hover{
  box-shadow: 2px 2px 15px grey;
  transition: 350ms;
}
.img-field{
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.create-time{
  font-size: 12px;
  color: gray;
  margin-top: 7px;
  float: right;
}
.fans{
  font-size: 12px;
  color: gray;
}
.content{
  display: inline-block;
  width: 300px;
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