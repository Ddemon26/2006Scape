#!/usr/bin/env python3
"""Simple JUnit test runner for 2006Scape client."""

import os
import subprocess
from pathlib import Path
from typing import Iterable

JUNIT_JAR = Path("libs/junit-4.13.2.jar")
HAMCREST_JAR = Path("libs/hamcrest-core-1.3.jar")
MOCKITO_JAR = Path("libs/mockito-core-3.12.4.jar")
BYTE_BUDDY_JAR = Path("libs/byte-buddy-1.12.22.jar")
OBJENESIS_JAR = Path("libs/objenesis-3.2.jar")
BUILD_DIR = Path("build/test_classes")

FAST_TESTS = [
    "cache.MRUCacheTest",
    "util.NodeHashTableTest",
    "util.ISAACRandomGenTest",
    "game.ItemPileTest",
    "util.NodeListTest",
    "util.NodeSubListTest",
]

ALL_TESTS = FAST_TESTS + ["integration.ClientIntegrationTest"]


def _check_junit() -> bool:
    missing = [p.name for p in [JUNIT_JAR, HAMCREST_JAR, MOCKITO_JAR, BYTE_BUDDY_JAR, OBJENESIS_JAR] if not p.exists()]
    if missing:
        print(
            "Missing libraries in libs/: " + ", ".join(missing)
        )
        return False
    return True


def _compile_sources():
    sources = list(Path("2006Scape Client/src/main/java").rglob("*.java"))
    test_sources = list(Path("2006Scape Client/src/test/java").rglob("*.java"))
    cp = os.pathsep.join([
        str(JUNIT_JAR),
        str(HAMCREST_JAR),
        str(MOCKITO_JAR),
        str(BYTE_BUDDY_JAR),
        str(OBJENESIS_JAR),
    ])
    os.makedirs(BUILD_DIR, exist_ok=True)
    cmd = [
        "javac",
        "-d",
        str(BUILD_DIR),
        "-cp",
        cp,
    ] + [str(p) for p in sources + test_sources]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print("Compilation failed:\n" + result.stderr)
        return False
    return True


def run_tests(test_classes: Iterable[str]):
    """Compile and run the given JUnit test classes."""
    if not _check_junit():
        return
    if not _compile_sources():
        return
    cp = os.pathsep.join([
        str(BUILD_DIR),
        str(JUNIT_JAR),
        str(HAMCREST_JAR),
        str(MOCKITO_JAR),
        str(BYTE_BUDDY_JAR),
        str(OBJENESIS_JAR),
    ])
    total = 0
    failures = 0
    for cls in test_classes:
        print(f"Running {cls}...")
        result = subprocess.run(
            ["java", "-cp", cp, "org.junit.runner.JUnitCore", cls],
            capture_output=True,
            text=True,
        )
        output = result.stdout.strip()
        if output:
            print(output)
        if result.returncode != 0:
            failures += 1
        else:
            # attempt to parse OK (n tests)
            for line in output.splitlines():
                if line.startswith("OK (") and "tests" in line:
                    try:
                        count = int(line.split("(")[1].split()[0])
                        total += count
                    except Exception:
                        pass
    print(
        "\nSummary: {0} failures across {1} test classes, {2} tests total".format(
            failures, len(test_classes), total
        )
    )


if __name__ == "__main__":
    run_tests(ALL_TESTS)
