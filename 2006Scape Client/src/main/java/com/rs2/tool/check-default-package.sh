#!/bin/sh
base="$(dirname "$0")/../../.."
missing=$(find "$base" -name '*.java' ! -path '*/com/rs2/*' -print)
if [ -n "$missing" ]; then
  echo "Java files without package statement:" >&2
  echo "$missing" >&2
  exit 1
fi
exit 0
