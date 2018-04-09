#!/bin/sh

action=${1:-install}
dirs='lambdify-plugin lambdify-apigateway lambdify-project'

for module in $dirs; do
  cd ${module}
  ./mvnw clean ${action}
  cd - > /dev/null
done

