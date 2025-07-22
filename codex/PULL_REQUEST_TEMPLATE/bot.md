# 🤖 RuneBot Pull Request

> **PR Title format**: `[BOT] <type(scope)>: <summary>`\
> Example: `[BOT] refactor(rename): Class29 → SoundEnvelope`

---

## ✅ Pre‑flight Checklist

| Item                                | Status |
|-------------------------------------| ------ |
| `mvn -B verify -o` passes (offline) |        |
| `spotbugs:check` passes             |        |
| ≥ 80 % coverage on touched lines    |        |
| Net Δ lines < 5 000                 |        |
| ≤ 10 files modified                 |        |
| Branch rebased onto latest `main`   |        |
| PR labeled `bot`                    |        |
| No new external dependencies        |        |

*If any item cannot be checked, abort and open an Issue instead of a PR.*

---

## 🔍 What & Why

---

## 🗂️ Detailed Changes

---

## ♻️ Rename‑Specific Fields *(omit section if not a rename)*

| Old Identifier | New Identifier |
| -------------- | -------------- |
|                |                |

- **Batch**:&#x20;
- **Revert command**: `git revert -m 1 <commit-sha>`

---

## 📊 Diff Stat

```text
<!-- paste output of `git diff --stat` -->
```

---

## 🧪 Integration‑Test Log

[Successful CI run](<!-- URL -->)

---

## 📝 Rollback Plan

If this PR causes a failure on `main`, Section 9 of **AGENTS.md** applies and the agent will open an automatic 
revert PR using the command above.

