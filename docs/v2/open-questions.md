# V2 Open Questions

This file tracks the V2 decisions that are not fully locked yet. The major product direction is already chosen. The remaining questions are mostly about how much structure or flexibility V2 should expose in its first implementation.

## 1. How public should `core` remain

Current leaning:

- keep `core` public
- document it as an expert or advanced layer
- make `runtime` the default recommendation

Open question:

- do we eventually deprecate parts of `core`, or simply keep them as stable advanced APIs indefinitely

## 2. Should `LicenseVerifyService` stay where it is

Current leaning:

- keep `LicenseVerifyService` in `core` for the first V2 phase
- let `runtime` wrap it

Open question:

- after `runtime` stabilizes, do we leave `LicenseVerifyService` in place as an expert API, or replace it internally with a lower-level abstraction

## 3. How configurable should runtime behavior be

Candidates:

- verification cache duration
- hot reload enable or disable flag
- preferences node name
- expiry warning threshold

Open question:

- which of these belong in the initial public runtime config, and which should stay internal until there is a real usage need

## 4. What should be the primary public verification style

Current leaning:

- support all three:
  - `isValid()`
  - `verify()`
  - `verifyOrThrow()`
- present `verifyOrThrow()` as the main happy-path story

Open question:

- how heavily should the docs bias toward `verifyOrThrow()` versus showing `verify()` equally often for diagnostics and admin-oriented flows

## 5. How detailed should failure codes be

Current leaning:

- expose stable, caller-friendly failure codes
- include hardware-specific mismatch categories

Open question:

- do we stop at the currently proposed top-level failure codes plus mismatch details, or do we need one more layer of structured diagnostic detail for observability use cases

## 6. Unsupported operating system behavior

Current leaning:

- fail fast during runtime creation if no built-in provider exists and no custom provider is supplied

Open question:

- should this always be a hard failure, or should there be an opt-in lazy mode for applications that only install licenses in certain environments

## 7. Spring bean exposure

Current leaning:

- expose `LicenseRuntime` as the main Spring bean
- keep startup hooks and servlet filter logic in auto-configuration
- keep `LicenseVerifyService` as a compatibility bean during the first V2 phase

Open question:

- if compatibility beans remain, should Spring obtain them from a runtime-owned assembly object such as `LicenseRuntimeAssembly`, or through a narrower bridge exported by `runtime`

## 8. Future module growth

Possible future directions:

- issuer-side SDK
- CLI utilities
- adapters for non-Spring frameworks

Open question:

- which future directions should influence the V2 package and artifact naming now, and which should wait until there is a concrete implementation need

## 9. How much detailed verification metadata should `core` expose to `runtime`

Current leaning:

- keep `LicenseVerifyService.verify()` as the simple boolean expert API
- add one richer internal outcome path so `runtime` can build `LicenseVerificationResult` without duplicating cache and reload logic

Open question:

- should that richer path be a new `verifyDetailed()` method on `LicenseVerifyService`, or a separate internal helper assembled beside it
