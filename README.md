# dl-3cushion-hint
딥러닝을 이용한 3쿠션 힌트 안드로이드앱
* Tensorflow를 이용한 Android앱을 개인적으로 공부하기 위해 앱을 제작하였습니다.(또 다른 이유는.. 저랑 당구를 즐기는 회사 동료분들을 위해..ㅎㅎ) 
* 예측 결과는 실제와 많이 다를 수 있습니다.
* 캡쳐 이미지들은 제대로 예측한 것만 올렸습니다.-_-a

![capture1](https://github.com/choonguri/dl-3cushion-hint/blob/master/temp_img/1.png?raw=true) ![capture2](https://github.com/choonguri/dl-3cushion-hint/blob/master/temp_img/2.png?raw=true)
![capture3](https://github.com/choonguri/dl-3cushion-hint/blob/master/temp_img/3.png?raw=true) ![capture4](https://github.com/choonguri/dl-3cushion-hint/blob/master/temp_img/4.png?raw=true)
![capture5](https://github.com/choonguri/dl-3cushion-hint/blob/master/temp_img/5.png?raw=true) ![capture6](https://github.com/choonguri/dl-3cushion-hint/blob/master/temp_img/6.png?raw=true)

## 사용방법
* 공을 드래그해서 원하는 위치에 놓으세요.
* 'PREDICTION' 버튼을 누르면 해당 포지션에 맞는 3쿠션 길을 알려줍니다.

# 개발과정

## 학습데이터 준비
* 데이터셋 개수 : 약 4,000장
* 클래스 개수 : 8개
* 이미지 크기 : 64x32
* 실제 학습에 사용된 이미지 샘플(학습하는데 알맞은 이미지인지 긴가민가 합니다.) 

 ![data1](https://github.com/choonguri/dl-3cushion-hint/blob/master/temp_img/image_data1.jpg?raw=true) ![data2](https://github.com/choonguri/dl-3cushion-hint/blob/master/temp_img/image_data2.jpg?raw=true) 

## 학습하기
* '텐서플로 첫걸음'책 5장 CNN 소스코드를 기반으로 레이어 하나 더 추가한 뉴럴넷을 구성하였습니다.
* 최초 1,000장 정도로 학습을 시도했는데, 정확도가 20%도 안나와서 4,000장을 준비했습니다.(수집하는데 두어시간 투자했습니다.)
  * 학습데이터(이미지) 모으는 앱도 별도로 만들어서 이미지를 수집하였습니다.
  * 당구 특성상 좌우대칭, 상하대칭, 180도 회전해도 같은 클래스로 볼 수 있기 때문에 데이터 뻥튀기도 수월했습니다.
* 최대한 이것 저것 숫자 조정해가면서 drop-out 50%, batch-size 100장, epoch 10,000번 돌려서 정확도 70% 정도 나왔습니다.
  * 하나의 볼 포지션에 3쿠션 길이 딱 1개만 있는게 아니기 때문에 저정도 정확도로 제 자신과 타협을 봤습니다.
  * 물론 말도 안되는 예측도 많으니 진지하게 이 앱을 이용하실 분은 다시 생각해 보셔야 합니다.
  ![accuracy](https://github.com/choonguri/dl-3cushion-hint/blob/master/temp_img/accuracy.png?raw=true)

## Android앱에 Tensorflow와 학습모델 탑재, 구현
* 사실 이 부분을 계속 경험하기 위함이였구요, 아래 사이트를 참고하였습니다.
  * https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android
  * https://omid.al/posts/2017-02-20-Tutorial-Build-Your-First-Tensorflow-Android-App.html
* Android용 Tensorflow SO파일과 jar파일은 직접 소스를 내려받아 빌드하셔도 되긴합니다만, nightly build(https://ci.tensorflow.org/view/Nightly/job/nightly-android/) 결과물을 사용하는걸 권장합니다.
* 급하게 만든거라 소스코드가 지저분 합니다만 저와 같은 시도를 하시거나 하려는 분들에게 조금이나마 도움이 되었으면 합니다. 
* 그리고 저보다 더 경험 많은 분들의 진심어린 조언, 가르침 꼭 부탁드립니다.(당구는 제가 가르침을 드리겠습니다.)
