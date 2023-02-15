java --version

./gradlew clean

./gradlew infoAthena
./gradlew infoMetrics
./gradlew infoWoocommerce

./gradlew build --no-build-cache

nohup java -jar athena/build/libs/athena-3.0.jar &
nohup java -jar metrics/build/libs/metrics-3.0.jar &
nohup java -jar woocommerce/build/libs/woocommerce-3.0.jar &
