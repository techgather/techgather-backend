# collector Engine 구조
- 위치 : collector/engine/CollectEngine.kt
```
  크롤링 요청 
     ↓ (url)
┌---------┐   fetch   ┌---------┐
| Crawler |   ---->   | Fetcher | ----→ website
|         |   <----   |         | <----
└---------┘   html    └---------┘  html
     |
메타데이터 추출 요청
     ↓ (html)
┌---------------┐   fetch   ┌---------------------┐
|   Extractor   |   ---->   | ThumbnailDownloader | ---------→ website
| (메타데이터 추출) |   <----   |                     | <--------- 
└---------------┘   html    └---------------------┘ thumbnail url
     |
  중복 제거
     ↓ (messages)
┌-----------------┐
| DeduplicatePort | 
└-----------------┘
     |
    발행
     ↓ (messages)
┌-----------┐
| Publisher | 
└-----------┘
```

# 실행방법
java -jar -Dtarget=woowahan,toss {file}.jar 