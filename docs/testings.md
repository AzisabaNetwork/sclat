---

# AI Agent Context: Minecraft Plugin Development (Koin + Kotest + Scenamatica)

## 1. Core Tech Stack

* **Language:** Kotlin
* **DI Framework:** **Koin** (Service Locator / Runtime DI)
* **Unit Testing:** **Kotest** (Behavior-driven testing)
* **E2E/Scenario Testing:** **Scenamatica** (Minecraft-specific scenario engine)
* **Mocking:** **MockK** (for unit isolation)

---

## 2. Dependency Injection Strategy (Koin)

* **Pattern:** Prefer **Constructor Injection** for business logic to keep it independent of the Koin runtime.
* **Verification:** Use `verify()` or `checkModules()` in a specialized test to validate the DI graph at build-time.
* **Environments:** * **Production:** Real modules loaded in `onEnable`.
* **Integration/E2E:** Load `testModule` overrides (e.g., Fakes for databases or external APIs).



---

## 3. Testing Philosophy: T-Wada Style

Maintain a sustainable, document-like test suite following the Red-Green-Refactor cycle.

### A. Unit Tests (Fast)

* **Location:** `src/test`
* **Scope:** Pure logic, math, permission calculations.
* **Constraint:** **No Bukkit/Paper API.** Use mocks for any Paper-specific classes.
* **Goal:** Instant feedback.

### B. Integration Tests (Medium)

* **Scope:** Koin module wiring and "Connectivity."
* **Key Tool:** `KoinExtension` in Kotest.
* **Fakes over Mocks:** Use `FakeUserRepository` instead of deep MockK setups to test stateful interactions without a real DB.

### C. E2E / Scenario Tests (Slow)

* **Tool:** **Scenamatica**.
* **Logic:** Defined in YAML "Specifications."
* **Goal:** Test the "Player Journey" (e.g., Joining -> Combat -> Death -> Respawn).
* **Verification:** Acts as the "source of truth" for plugin behavior on a real server instance.

---

## 4. Structural Organization

* **Physical Separation:** * `src/test`: Unit tests.
* `src/integrationTest` (or Kotest Tags): Wiring and DB tests.
* `scenarios/`: Scenamatica YAML files.


* **CI/CD Pipeline:** 1.  Run `test` (Unit) on every save/push.
2.  Run `verify()` (Koin) to prevent startup crashes.
3.  Run `scenamatica` (E2E) on Pull Requests.

---

## 5. Decision Matrix for AI Agent

| Task | Recommended Tool | Approach |
| --- | --- | --- |
| **New Logic/Math** | Kotest | TDD, Constructor Injection. |
| **New Service/Repo** | Koin | Add to module, run `verify()`. |
| **Event Handling** | Scenamatica | YAML scenario simulating the Event trigger. |
| **Bug Fix** | Kotest | Write failing unit test first, then fix. |

---
