# 택시 타기 좋은 위치 | 손님 태우기 좋은 위치
택시 승하차 데이터를 분석하여 사용자는 택시 타기 좋은 위치, 손님 태우기 좋은 위치를 탐색하는 앱니다. :)
(2015/01 ~ 2016/04 서울시 택시운행 분석데이터 기준) 

## 주요기능
- 현재 위치 탐색하기
- 택시 타기 좋은 위치 탐색하기

## 주요화면
![demo](./img/demo.gif)

## 구현
- 코틀린 언어로 구현함
- [Clean Architecture](https://github.com/android10/Android-CleanArchitecture) 를 참고하여 프로젝트의 틀을 잡고 Rxjava, Retrofit2 로 비지니스 로직을 구현함
- Dagger2 을 활용하여 클래스 간 의존성 주입함  

## 디렉토리 구조
- src  
    - api : Api 서버 통신
    - di : 의존성 주입 (Dagger2 - Component, Module)
    - usecase : 비지니스 로직 (택시 타기 좋은 위치 탐색하)
    - presenter : Presenter 레이어
    - view : View 레이어 (안드로이드 액티비티, 뷰 컴포넌트)
    
## 서버측