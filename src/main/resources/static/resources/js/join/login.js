"use strict";
$(()=>{
    new Login();
})
export class Login{

    constructor() {

        console.log("회원가입 페이지");
        this.event();
        this.email="" ;
        this.nickname="";
    }
    event(){
        $('#modal').addClass('hidden');
        Kakao.init('1c7dbcce4a9a8dcce06171f411ab8aaf');
        $("#kakao-login-btn").on("click", function(){
            //1. 로그인 시도
            Kakao.Auth.login({
                success: function(authObj) {

                    //2. 로그인 성공시, API 호출
                    Kakao.API.request({
                        url: '/v2/user/me',
                        success: function(res) {
                            this.email=res.kakao_account['email'];
                            this.nickname=res.properties['nickname'];
                            location.href="/kakaoJoin?nickname="+this.nickname+"&email="+this.email;
                        }
                    })
                    console.log(authObj);
                    let token = authObj.access_token;
                },
                fail: function(err) {
                    alert(JSON.stringify(err));
                }
            });
        })
    }



//
}