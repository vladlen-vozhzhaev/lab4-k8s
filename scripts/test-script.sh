step() {
  local step=$1
  [[ $((step % 2)) -eq 0 ]] && replicas=1 || replicas=0

  printf "=== Step %d: scale %s to %s ===\n" "$step" "$deployment" "$replicas"

  kubectl scale deployment "$deployment" -n "$namespace" --replicas "$replicas"

  # Ждём, пока Deployment действительно достигнет нужного числа реплик
  kubectl rollout status deployment/"$deployment" -n "$namespace" --timeout=60s || true

  # Дополнительно: если реплик 0 — ждём, чтобы поды исчезли полностью
  if [[ $replicas -eq 0 ]]; then
    kubectl wait --for=delete pod -n "$namespace" \
      -l app.kubernetes.io/name="$deployment" --timeout=60s
    sleep 3   # даём nginx закрыть keepalive-соединения
  else
    kubectl wait --for=condition=Ready pod -n "$namespace" \
      -l app.kubernetes.io/name="$deployment" --timeout=180s
  fi

  newman run \
    --delay-request=100 \
    --folder=step"$step" \
    --export-environment "$variant"/postman/environment.json \
    --environment "$variant"/postman/environment.json \
    "$variant"/postman/collection.json

  printf "=== Step %d completed ===\n" "$step"
}