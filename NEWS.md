# History of phone-number releases

## 8.12.4-2 (2020-06-27)

- Improvements:
    * Type-hinted `locale-specification` arguments as keywords
    * Improved fetching of the required data from phone numbers expressed as maps:
      * `phone-number.core/raw-input`
      * `phone-number.core/valid-input?`
    * Changed from accepting phone numbers only as strings to polymorphic:
      * `phone-number.core/short-emergency?`
      * `phone-number.core/short-to-emergency?`
    * Added namespaces and namespace aliases for shorter keywords notation:
      * `phone-number`
      * `phone-number.short`
      * `phone-number.sample`
      * `phone-number.dialing-region`
      * `phone-number.arg` (specs only)
      * `phone-number.args` (specs only)
      * `phone-number.prop` (specs only)

- Bug fixes:
    * Fixed spec for `phone-number.core/raw-input`

## 8.12.4-1 (2020-06-24)

- BREAKING CHANGES:
    * Calling region (code) is now renamed to dialing region (code)
      * `:phone-number.calling-region` is now `:phone-number.dialing-region`
      * `:phone-number.short/dialing-region` property is replaced by
        `:phone-number/dialing-region`
      * `phone-number.core/valid-for-region?` is now deprecated in favor of `phone-number.core/valid?`

- Improvements:
    * Default dialing region support with `phone-number.core/*default-dialing-region*`
    * Dialing region derivation support with `phone-number.core/*info-dialing-region-derived*`
    * Improved support for maps as phone numbers (incl. dialing region retention)
    * Added validation for locale specification (`phone-number.util/valid-locale?`)
    * Samples generator now uses custom, simpler phone number parser
    * Additional arities in `phone-number.format/global?` and
      `phone-number.format/regional?`
    * Input validation is more relaxed (no strict asserts based on first digits)
    * Added `phone-number.core/has-raw-input?`
    * Added `phone-number.core/numeric`
    * Added utility functions in `phone-number.util`:
      - `count-digits`
      - `char-ranges->set`
      - `available-locales`
    * Specs updated
    * Tests updated
    * Generative testing based on generators and specs

- Bug fixes:
    * Proper handling of corner cases (nil punning) at:
      - `phone-number.core/short-info`
      - `phone-number.core/time-zones-all-formats`
      - `phone-number.core/all-formats`
      - `phone-number.core/has-calling-code?`
      - `phone-number.core/raw-input`

## 8.12.4-0 (2020-06-11)

- Initial release.

