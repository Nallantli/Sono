# Sono Beta 1.2.4

<div align="center">
<img src="docs/Sono.svg" alt="Sono Logo" width="150">
</div>

---

## Overview

Sono is a high-level object-oriented and procedural scripting language developed with linguistic capabilities in mind. For that it supports multiple operators revolving around phonological analysis based upon distinctive features.

While the included file [hayes.tsv](assets/hayes.tsv) is used for the example codes under [examples/](examples), the use may load any set of qualities for the distinctive features so long as the same set of distinctive features is used. Currently the features supported are:

* `stress` (Stress)
* `long` (Length)
* `syl` (Syllabic)
* `cons` (Consonantal)
* `approx` (Approximant)
* `son` (Sonorant)
* `cont` (Continuant)
* `del` (Delayed Release)
* `nasal` (Nasal)
* `str` (Strident)
* `voice` (Voice)
* `sg` (Spread Glottis)
* `cg` (Constricted Glottis)
* `LAB` (Labial)
  * `round` (Rounding)
  * `ld` (Labiodental)
* `COR` (Coronal)
  * `ant` (Anterior)
  * `dist` (Distributed)
* `LAT` (Lateral)
* `DOR` (Dorsal)
  * `high` (High)
  * `low` (Low)
  * `front` (Front)
  * `back` (Back)
  * `tense` (Tense/ATR)

Place features (`LAB`, `COR`, `LAT`, and `DOR`) have sub-features that are considered to be `0` in quality when the corresponding place feature is `-`. For instance, the segment `/p/` is `-DOR` and the sub-features `[0high, 0low, 0front, 0back, 0tense]`. Any transformation that affects one of those sub-features will automatically activate the rest and set the place feature to `+`. If a transformation rule gives `[+front]`, `/p/` will be transformed to `[+DOR, -high, -low, +front, -back, -tense]`. Likewise, if a `+DOR` segment is transformed by `[-DOR]`, all sub-features will be nullified to quality `0`.

## Command Line Arguments

The first argument (if the user wishes to run a file) must be the path to the file. Otherwise the CLI interpreter will be opened, from which commands may be run per user input.

There are currently only three command line arguments: `-l` which disables all phonological features of the language (for use as a general scripting language), `-d` which takes in a file path for phonological base data, and `-g`, which activates debug mode which will create a stack trace for any caught error.

During the initial run, `-d` will be required to develop a cache for the phonological data, and this process will take time depending on the extent of the data file given. Thereafter however the interpreter will automatically load the cached data at a significantly faster rate.

Due to possible changes in code structure, caches from older versions of the interpreter may not be successfully loaded. This only affects updates which alter the contents of package [`main.phl`](src/main/phl). Otherwise, older caches can be successfully used. A cache can always be re-initalized with `-d`.

Example usages:

```sh
sono "filename"
sono -d "pathToData.tsv"
sono "filename" -l
sono "filename" -d "pathToData.tsv"
```

## Data Types

There are eight base data types:

Type | Notes | Examples of Literals
-|-|-
`Number` | Numerical values encompassing floating point numerals and integers | `1`, `1.1`, `1.0`, `0.1`
`String` | Sequence of text, immutable | `"Hello World"`
`Vector` | List of any values, including mixed types | `{1,2,3,4}`, `{"Hello", 1}`, `{{1,2}, 3}`
`Phone` | Phonological segment or phoneme, support for all segments in the user selected feature file and various secondary articulations (`◌̩`, `◌̠`, `◌̟`, `◌̪`, `◌̺`, `◌̥`, `◌̥`, `◌̃`, `◌ʷ`, `◌ʲ`, `◌ˠ`, `◌ˤ`, `◌ʰ`, `◌ː`, a segment may use multiple secondary articulations, but those that contrast with each other will give error *c.f. `*[o̟̠]`*)<br>Currently there is no support for X-SAMPA<br>Affricates must have an underscore to bind them | `'s'`, `'t_ɕ'`, `rʷː`
`Word` | Sequence of Phones, with the addition of syllable delimiters `.` and morpheme boundary markers `+` | `` `foʊnɒləd_ʒi` ``, `` `soʊ.noʊ` ``, `` `naː.wa+tɬ` ``
`Feature` | Distinctive feature and its quality | `+|long`, `-|LAB`
`Matrix` | Grouping of Features for transformations | `[+|long, -|tense]`
`Rule` | Phonological transformation rules using a combination of Phones, Strings, and Matrices<br>The initial character determines whether it is assimilatory: `S` indicates no assimilation (does not remove any segments), `Af` indicates forward assimilation (removes the following segment), `Ab` indicates backward assimilation (removes the previous segment) | `S : 't' -> 't_ɬ' // "$" ~ [-|high, +|low, -|front, -|back, -|tense]`
`Function` | Basic parameterized anonymous function | `(a, b) => {return a * b;};`

## Syntax

Please note that syntax may change during the course of updates due to the novelty of the language. After the beta is complete the final syntax will have been established.

The syntax is mostly C-style with minor alterations regarding flow segments and usage of semi-colons. All lines (including the end of scopes `{}`) must use a semi-colon. The exception to this is single-lined scopes, where a semi-colon is not necessary. For example, the function `(a, b) => {return a * b;};` may also be written as `(a, b) => {return a * b};`.

### Variables

There are two default, immutable variables: `_all` and `_base`. The former encompasses all generated phones, including those that exist in `_base` and their derivatives, while `_base` only contains the phones established in the TSV data file.

Variables can have any key that falls within `[a-zA-Z_][\da-zA-Z_]*`. Examples:

```sono
var a;
var _a;
var a1;
```

Variables must be declared before usage with the `var` keyword. Their type is initially declared as `null` with no values.

Variables can be set thereafter with the operator `=`, and will take on the type of whatever value proceeds the operator:

```sono
var a = 1; # Variable 'a' is type 'Number' with value '1'
a = "Hello World"; # Now variable 'a' is type 'String' with value '"Hello World"'
```

Operators are strictly typed however, and thus a variable of type `Number` cannot be added to a variable of type `String`, etc. Explicit conversion is necessary using the following keywords. All keywords are reflexive for their own types, and reversable with the exception of `str` (if `a` is a `Vector`, then `vec word a == a`):

Keyword | Conversion | Values
-|-|-
`str` | Convert to a `String` | Any value
`vec` | Converts to a `Vector` | `String`, `Matrix`, `Word`
`mat` | Converts to a `Matrix` | `Vector` (of Features)
`num` | Converts to a `Number` | `String`
`char` | Converts to the character `Number` value of a single-value `String` | `String`
`word` | Converts to a `Word` | `String`, `Vector`
`feat` | Converts to a `Feature` | `String`

### Operators

All basic arithmetic operators exist, including `**` for exponent. Any alterations or unique operators are noted below

Operator | Function | Values
-|-|-
`a + b` | Concatenates or adds values<br>In the case of two `Matrix` values, the returned `Matrix` will contain a combination of values that allow for `from` to return a natural class `Vector` encompassing values given by both `Matrix` values | `a` and `b` must be of the same type
`len a` | Returns the length of a value | `String`, `Vector`, `Matrix`, `Word`
`type a` | Returns a `String` representation of the value type | Any
`com a` | Returns a `Matrix` of features shared between two values | `Phone`
`a ?> b` | Returns a `Matrix` of contrastive features between two values | `Phone`
`a from b` | Returns all `Phone` values from `b` that contain features expressed in `a` | `Matrix` and `Vector` (of Phones)
`a >> b` | Transforms `a` by `b` | `Word` and `Rule`, `Phone` and `Matrix`
`a until b` | Creates a `Vector` of values within the range of `a` and `b` (exclusive) | `Number`

### Objects

Objects are declared using two modifiers, either `static` or `struct`, and then the name of the object, followed by `class`.

Objects declared as `static` cannot be instantiated, they exist as "holders" for methods and values. Objects declared with `struct` however are able to be instantiated with the `new`.

`struct` objects _must_ have a method `init`, which is called during instantiation as the constructor method. Other methods may be overrided:

Method | Arguments | Use
-|-|-
`getStr`|None|Returns a `String` representation of the object when the object is called with `str`
`getIndex`|Any single value|Overrides the `Vector` indexing operator `[]`
`getList`|None|Returns a `Vector` representation of the object when the object is called with `vec`
`getLen`|None|Returns the length of the object when called with `len`

While it is possible to override these methods to return values of any type, that practice may lead to some confusion (for instance, `getList` returning a `Number`).

There is currently no draw to implement an override for the `==` operator, as the scripter may wish to compare whether two objects point to the same object in one circumstance and to establish equivalency by value in a different circumstance. For that reason it is recommended to implement an `equals` method.

An example object and its instantiation are as follows:

```sono
struct Point class {
 var x;
 var y;
 init(x, y) => {
  this.x = x;
  this.y = y
 };
 getStr() => {
  return "(" + str x + ", " + str y + ")";
 };
};

var p = new Point(1, 2);
print(p); # Prints "(1, 2)"
```

### Rules

Rule declarations have five components: Type, Search parameter, Transformation parameter, and the two boundary parameters.

```sono
<Type> : <Search> -> <Transformation> // <Lower Bound> ~ <Upper Bound>
```

The Type is one of three values `S`, `Af`, and `Ab`. Their usage was explained above.

The Search parameter must be of type `Matrix` or `Phone`. The `Rule`, when applied, will search the sequence sequentially for a value applicable ot the Search parameter. The Search parameter can also be `null`, in which case the `Rule` will look only with concern to the boundary parameters.

The Transformation parameter must be of type `Vector`, containing `Matrix` or `Phone` values, or a `Matrix`, `Phone`, or `String` value that will be inserted as a 1-element `Vector`. A `Vector` value is used to transform into sequences of greater length than a single segment, e.g. `... -> {'n', 'd'}`. If the Search parameter is satisfied, the values within the Transformation parameter will be applied to the found `Phone`. For a `Matrix` value in the parameter, the application will generate a `Phone` with the values of the Search transformed by the `Matrix`. For a `Phone` value in the parameter, there is no transformation and the value is simply appended in place of the Search parameter. Using a `Vector` with multiple values allows for the creation of multiple new `Phone` values with their sequential transformations or replacements with respect to the Search parameter.

The Boundary parameters follow the same format as the Transformation parameter, but scan the sequence in the manner of the Search parameter. They must be declared as `Vector`. In addition to the values allowed for the Transformation parameter, Boundary parameters may also contain the following `String` values:

`"$"` Syllable boundary (matches to `.` in a sequence, `+` in a sequence, or the beginning or end of the sequence)

`"#"` Word boundary (matches only to the beginning or end of a sequence)

`"+"` Morpheme boundary (matches to `+` in a sequence)

The rules `"$"` and `"+"` only apply if the `Word` explicitly declares syllable or morpheme boundaries, the interpreter does not assume boundaries.

Some examples of rules include:

```sono
# Vowel nasalized before a nasal consonant (English)
S : [+|syl, -|cons] -> [+|nasal] // null ~ [-|syl, +|cons, +|nasal];
# `mæn` -> `mæ̃n`

# Vowel nasalized before a nasal consonant, and the consonant is deleted (French)
Af : [+|syl, -|cons] -> [+|nasal] // null ~ [-|syl, +|cons, +|nasal];
# `bɔn` -> `bɔ̃`

# Palatalized /h/ becomes [ç] before a vowel (Japanese)
S : 'hʲ' -> 'ç' // null ~ [+|syl];
# `hʲito` -> `çito`

# Final non-nasal consonant is devoiced (German)
S : [-|syl, +|cons, +|voice, -|nasal] -> [-|voice] // null ~ "#";
# `taːg` -> `taːk`
# `taːgə` -> `taːgə` (Unchanged)

# An epenthetic [ə] is inserted between sequential consonants (Not based on a real language)
S : null -> 'ə' // [-|syl, +|cons] ~ [-|syl, +|cons];
# `asta` -> `asəta`
```

In addition, there are further `Feature` assimilation options. Denoting features with numeric qualities (e.g. `1`, `2`, `3`, ...) creates a variable feature linked to the quality in the `Phone` or `Matrix` that corresponds to that integer read from left to right.

For instance, a rather complicated nasal assimilation rule in Japanese:

```sono
# The moraic nasal assimilates to the place of the following consonant
S : 'ɴ' -> [2|LAB, 2|round, 2|ld, 2|COR, 2|ant, 2|dist, 2|DOR, 2|high, 2|low, 2|front, 2|back] // null ~ [-|syl, +|cons, 2|LAB, 2|round, 2|ld, 2|COR, 2|ant, 2|dist, 2|DOR, 2|high, 2|low, 2|front, 2|back];
```

Place assimilation may be revised in the future using macros for the sake of brevity.

### Loops

The for-loop syntax and while-loop syntax use the same keyword `do`, with the for-loop having an iteration keyword `in` indicating its span.

```sono
i in {0 until 10} do {
 # ....
};

true do {
 # ....
};
```

The iteration variable is mutable and if the loop happens to be iterating over a Vector variable, the contents may be altered:

```sono
var nums = {0, 1, 2, 3, 4};

print(nums); # {0, 1, 2, 3, 4}

i in nums do {
 i += 1;
};

print(nums); # {1, 2, 3, 4, 5}
```

### If-Then-Else

If-then-else sequences use `then` and `else`:

```sono
i == 1 then {
 # ....
} else {
 # ....
};
```

Else-if segments can be easily chained on using another `then`:

```sono
i == 1 then {
 # ....
} else i == 2 then {
 # ....
} else i == 3 then {
 # ....
} else {
 # ....
};
```

A final `else` statement is not required.

### Try-Catch

Try-catch statements use the following form:

```sono
try {
 # ....
} catch {
 # ....
};
```

Within the `catch` statement, the variables `_e` and `_trace` are generated, the former indicating the message of the error and the latter indicating a trace of the operations preceding that error.

The `catch` clause is optional.

### Loading External Files

The `load` keyword will search for a file of a given `String` key and load its content during compilation. It will search in the running directory first, and then proceed to the application's [`bin/lib`](bin/lib) directory. See [`examples`](examples) for usage samples.

## External Libraries

External libraries can be compiled as `.jar` files and imported with the `import` keyword. These `.jar` files must be built with the [`build-lib`](build-lib.bat) script and extend [`main.base.Library`](src/main/base/Library.java). In addition, they must be compiled in the package [`ext`](src/ext).

The commands of external libraries are activated using the keyword `_OUTER_CALL_` which requests a predefined function. The first parameter of the keyword is the `String` key of the function, while the second is the input of any type. See [`ext`](src/ext) for examples.