#!/bin/bash

echo "⛔️ 컨테이너와 볼륨 제거"
docker-compose down -v

echo "🔧 컨테이너 빌드 및 시작"
docker-compose up --build -d

echo "컨테이너 실행"

echo "⌛️ Redis 클러스터 노드 준비 대기"
sleep 5  # 노드가 완전히 뜰 시간

echo "🧩 Redis 클러스터 생성"
yes yes | docker exec -i redis-node-7000 redis-cli --cluster create \
  172.20.0.10:7000 \
  172.20.0.11:7001 \
  172.20.0.12:7002 \
  172.20.0.13:7003 \
  172.20.0.14:7004 \
  172.20.0.15:7005 \
  --cluster-replicas 1

echo "✅ 클러스터 구성 완료"