<div align="center">

<!-- logo -->

### EduNexus 학원예약 시스템 ✅
> **MSA 아키텍처 및 분산 환경 기술 스택(Kafka, gRPC) 학습 프로젝트**

[<img src="https://img.shields.io/badge/-readme.md-important?style=flat&logo=google-chrome&logoColor=white" />]() [<img src="https://img.shields.io/badge/-tech blog-blue?style=flat&logo=google-chrome&logoColor=white" />]() [<img src="https://img.shields.io/badge/release-v0.1.0-yellow?style=flat&logo=google-chrome&logoColor=white" />]() 
<br/> [<img src="https://img.shields.io/badge/프로젝트 기간-2024.12.3~-green?style=flat&logo=&logoColor=white" />]()

</div> 

<br/>

## 📢 프로젝트 회고 (Engineering Retrospective)
> **"기술의 화려함보다 문제 해결의 적합성이 중요하다는 것을 깨닫게 해 준 프로젝트"**
>
> 본 프로젝트는 MSA, Kafka, gRPC 등 최신 분산 처리 기술을 학습하고 적용하는 것을 목표로 시작했습니다. 하지만 개발 과정에서 **'목적 없는 기술 도입(Over-Engineering)'**이 가져오는 복잡도와 리소스 낭비를 직접 경험했습니다.

### 🛑 문제점 및 한계 (Failures)
- **목적 없는 기술 도입:** 트래픽이 적은 초기 단계 서비스임에도 불구하고 `Kafka`와 `MSA`를 도입하여, 비즈니스 로직 구현보다 인프라 설정과 유지보수에 과도한 시간이 소요되었습니다.
- **복잡도 폭증:** 단순한 조회 기능조차 여러 마이크로서비스를 거쳐야 했기에 디버깅 난이도가 급상승했습니다.
- **깊이의 부재:** 아키텍처를 조립하는 데 집중하느라, 정작 중요한 **'동시성 제어'**나 **'DB 쿼리 최적화'** 같은 백엔드의 본질적인 깊이를 놓쳤습니다.

### 💡 얻은 교훈 (Lessons Learned)
1. **기술은 도구일 뿐이다:** Kafka나 GraphQL은 대규모 트래픽과 복잡한 의존성이 검증되었을 때 도입해야 '득'이 된다는 것을 배웠습니다.
2. **신입 엔지니어의 핵심 역량:** 아키텍처의 넓이(Breadth)보다는 **기능 하나를 만들더라도 극한으로 파고드는 깊이(Depth)**와 **안정성**이 더 중요함을 체감했습니다.
3. **단계적 개발의 중요성:** 모놀리식으로 시작해 병목이 발생하는 지점부터 점진적으로 분리하는 것이 훨씬 효율적인 전략임을 깨달았습니다.

👉 **[Next Step]:** 본 프로젝트의 교훈을 바탕으로, 이후 프로젝트에서는 **단일 API의 성능 극한 튜닝 및 동시성 제어(Concurrency Control)**에 집중하는 방향으로 엔지니어링 역량을 강화하고 있습니다.

<br/>
<br/>

## 📝 소개
EduNexus 학원 예약시스템 - 전국의 모든 학원과 학생을 연결시켜주는 서비스 플랫폼입니다.

- 프로젝트 소개
EduNexus 학원 예약 시스템은 전국의 학원과 학생을 연결하여 편리한 수업 예약과 관리를 지원하는 서비스 플랫폼입니다. 학생과 학부모는 다양한 학원과 강의를 한눈에 비교하고 예약할 수 있으며, 학원은 강의 관리와 학생 출결 기록을 효율적으로 운영할 수 있습니다. 이 플랫폼은 학원과 학생 간의 원활한 소통과 학습 효율성을 극대화하는 것을 목표로 합니다.

## 🗂️ APIs
작성한 API는 아래에서 확인할 수 있습니다.

👉🏻 [API 바로보기](/backend/APIs.md)


<br />

## ⚙ 기술 스택

### Back-end
<div>
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/Java.png?raw=true" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/SpringBoot.png?raw=true" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/Qeurydsl.png?raw=true" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/SpringDataJPA.png?raw=true" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/Mysql.png?raw=true" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/Redis.png?raw=true" width="80">
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/Swagger.png?raw=true" width="80">
<img src="https://github.com/zbnerd/edunexus/blob/master/docs/skill/kafka.png?raw=true" width="80">
</div>

### Infra
<div>
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/Docker.png?raw=true" width="80">
  <img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/AWSEC2.png?raw=true" width="80">
</div>

### Tools
<div>
<img src="https://github.com/yewon-Noh/readme-template/blob/main/skills/Github.png?raw=true" width="80">
</div>

<br />

## 🛠️ 프로젝트 아키텍쳐
<img src="https://github.com/zbnerd/edunexus/blob/master/docs/project_architecture.png">



<br />

## 🤔 기술적 이슈와 해결 과정
- 윈도우 10 유령포트 점유문제
  - [윈도우10 유령포트 점유문제](https://velog.io/@mps86/%EC%9C%88%EB%8F%84%EC%9A%B0-10-%EC%9C%A0%EB%A0%B9%ED%8F%AC%ED%8A%B8-%EC%A0%90%EC%9C%A0-%EB%AC%B8%EC%A0%9C)
- DP 기법을 이용한 평점 평균 계산 로직 with Redis
  - [EduNexus DP 기법을 이용한 평점 평균 계산 로직](https://velog.io/@mps86/EduNexus-DP-%EA%B8%B0%EB%B2%95%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-Course-%ED%8F%89%EA%B7%A0-%ED%8F%89%EC%A0%90-%EA%B3%84%EC%82%B0-%EB%A1%9C%EC%A7%81)
- 평점 평균 계산 로직의 문제점
  - [평점 평균 계산 로직의 문제점](https://velog.io/@mps86/CourseRating-%ED%98%84-%EC%8B%9C%EC%8A%A4%ED%85%9C%EC%9D%98-%EB%AC%B8%EC%A0%9C%EC%A0%90)
- Kafka 기반 보상 트랜잭션을 통한 평점평균 시스템 Redis-DB 데이터 무결성 보장
  - [Kafka 기반 보상 트랜잭션을 통한 평점평균 시스템 Redis-DB 데이터 무결성 보장](https://velog.io/@mps86/Kafka-%EA%B8%B0%EB%B0%98-%EB%B3%B4%EC%83%81-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98%EC%9D%84-%ED%86%B5%ED%95%9C-%ED%8F%89%EC%A0%90%ED%8F%89%EA%B7%A0-%EC%8B%9C%EC%8A%A4%ED%85%9C-Redis-DB-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EB%AC%B4%EA%B2%B0%EC%84%B1-%EB%B3%B4%EC%9E%A5)
<br />

## 💁‍♂️ 프로젝트 팀원
|Backend|
|:---:|
|[이승준](https://github.com/zbnerd)|
