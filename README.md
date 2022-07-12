# BloomBuddy
몰입캠프 2주차
* 사용자 계정에 로그인하여 근처 친구들과 화상통화를 하기 위한 앱입니다. 
* 팀원 : 정찬우, 이지현

## 개발 환경
* OS: Android (minSdk: 21, targetSdk: 32)
* Language: Kotlin
* IDE: Android Studio
* Target Device: Galaxy S7

## 0. 플래시화면
<p align="center">
  <img src="https://user-images.githubusercontent.com/76734678/178497214-d5689a09-dffd-4d8d-89f5-5dda2d41ac09.png" width="270" height="520"> 
</p>

## 1. 로그인화면
 * 자체 계정 혹은 네이버, 카카오, 구글 계정으로 로그인 할 수 있습니다.
 * 자체 계정은 회원가입을 진행한 계정이 DB에 저장되고, 다른 API를 통한 로그인은 첫 로그인 시에만 DB에 저장됩니다.
<p align="center">
  <img src="https://user-images.githubusercontent.com/76734678/178495281-7645fb59-4556-47bc-ab8d-079b921133b3.png" width="270" height="520"> 
</p>

## 2. 위치권한
 * 친구들과 위치 정보를 공유하는 탭이 존재하여 첫 화면에 위치 정보 접근 권한을 확인하고 요청합니다.
<p align="center">
  <img src="https://user-images.githubusercontent.com/76734678/178499194-907c025d-efbb-4227-a856-03cef81c7b72.png" width="270" height="520"> 
</p>

## 3. 메뉴바와 탭 세 개
### 3-1. 지도
* 현재 접속해있는 유저들의 위치 정보와 프로필 정보를 받아와 마커를 찍는 페이지입니다.
* 서버 코드가 완성되지 않아 지금은 임의로 마커를 찍어놓은 상태입니다.
<p align="center">
  <img src="https://user-images.githubusercontent.com/76734678/178502331-6050d72f-2965-42e7-9198-3320e50a6ae7.png" width="270" height="520"> 
</p>

* 지도에 뜨는 마커를 누르면 해당 이용자의 프로필 정보와 화상통화 걸기 버튼, 친구 추가 버튼이 보여집니다.
* 친구 목록 관리 및 화상 통화 기능이 아직 구현되지 않은 상태입니다.
<p align="center">
  <img src="https://user-images.githubusercontent.com/76734678/178501074-4e3adecf-fe66-40ed-bc39-b58608061bab.png" width="270" height="520"> 
</p>


