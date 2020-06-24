# History of phone-number releases

## 8.12.4-1 (2020-06-24)

- BREAKING CHANGES:
    * Calling region (code) is now renamed to dialing region (code)
      * `:phone-number.calling-region` is now `:phone-number.dialing-region`
      * `:phone-number.short/dialing-region` property is replaced by `:phone-number/dialing-region`

- Improvements:
    * Default dialing region support with `phone-number.core/*default-dialing-code*`
    * Dialing region derivation support
    * Improved support for maps as phone numbers (incl. dialing region retention)
    * Added validation for locale specification
    * Samples generator now uses custom, simpler phone number parser
    * Additional arities in `phone-number.format/global?` and
      `phone-number.format/regional?`
    * Input validation is more relaxed (no strict asserts based on first digits)
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

