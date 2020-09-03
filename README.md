# Sono Beta 1.10.2

<div align="center">
<img src="misc/Sono.svg" alt="Sono Logo" width="150">
</div>

---

## Overview

Online interface available at [sonolang.com](https://www.sonolang.com).

Sono is a high-level object-oriented and procedural scripting language developed with linguistic capabilities in mind. For that it supports multiple operators revolving around phonological analysis based upon distinctive features.

While the included file [hayes.tsv](assets/hayes.tsv) is used for the example codes under [examples](examples), any set of TSV separated distinctive feature list may be used, with or without all the features expressed in [hayes.tsv](assets/hayes.tsv).

Place features in all-caps have sub-features that are considered to be `0` in quality when the corresponding place feature is `-`. For instance (using [hayes.tsv](assets/hayes.tsv)), the segment `/p/` is `-DOR` and the sub-features `[0high, 0low, 0front, 0back, 0tense]`. Any transformation that affects one of those sub-features will automatically activate the rest and set the place feature to `+`. If a transformation rule gives `[+front]`, `/p/` will be transformed to `[+DOR, -high, -low, +front, -back, -tense]`. Likewise, if a `+DOR` segment is transformed by `[-DOR]`, all sub-features will be nullified to quality `0`. In a custom TSV feature file, major features are determined the same way, with all lowercase features following them to be considered sub-features.

## Building

There are two pairs of files necessary to build, depending on your OS. For Windows, [build-jar.bat](build-jar.bat) and [build-lib.bat](build-lib.bat) will build the JAR file and the libraries and place them in their respective folders. For Linux/OSX, the same files exist as `.sh` versions and can be invoked in the same manner in a bash terminal.

### Windows

```sh
git clone https://github.com/Nallantli/Sono
cd Sono
./build-lib
./build-jar
```

### OSX/Linux

```sh
git clone https://github.com/Nallantli/Sono
cd Sono
sh build-lib.sh
sh build-jar.sh
```

Make sure to move `Sono/bin` do your desired location and add `/bin` to your system's `PATH` variable.

## Running

It is possible to run the JAR file directly, however it is recommended for the sake of brevity to use the files [bin/sono.bat](bin/sono.bat) for Windows or [bin/sono.sh](bin/sono.sh) for Linux/OSX, as these contain the necessary arguments to run the interpreter in UTF-8, necessary for IPA.

## Command Line Arguments

Packages may be installed from GitHub Repos with `-ig`. Use the URL as the parameter:

```sh
sono -ig "https://github.com/Nallantli/Sono-Linguistics-Package"
```

The first argument (if the user wishes to run a file) must be the path to the file. Otherwise the CLI interpreter will be opened, from which commands may be run per user input.

There are currently only two command line arguments: `-l` which disables all phonological features of the language (for use as a general scripting language) and `-d` which takes in a file path for phonological base data.

During the initial run, `-d` will be required to develop a cache for the phonological data, and this process will take time depending on the extent of the data file given. Thereafter however the interpreter will automatically load the cached data at a significantly faster rate. A cache can always be re-initialized with `-d`.

Example usages:

```sh
sono "filename.so"
sono -d "pathToData.tsv"
sono "filename.so" -l
sono "filename.so" -d "pathToData.tsv"
```

## Data Types

There are eight base data types:

Type | Notes | Examples of Literals
-|-|-
`Number` | Numerical values encompassing floating point numerals and integers | `1`, `1.1`, `1.0`, `0.1`
`Boolean` | `true` or `false`, comparsion boolean values | `true`, `false`
`String` | Sequence of text, immutable | `"Hello World"`
`Vector` | List of any values, including mixed types | `{1,2,3,4}`, `{"Hello", 1}`, `{{1,2}, 3}`
`Dictionary` | Associative array of `String` to any value; for an associative array with keys of type other than `String`, see `map.so` | `@{"hello" : "world", "salve" : "munde"}`
`Phone` | Phonological segment or phoneme, support for all segments in the user selected feature file and various secondary articulations (`◌̩`, `◌̠`, `◌̟`, `◌̪`, `◌̺`, `◌̥`, `◌̥`, `◌̃`, `◌ʷ`, `◌ʲ`, `◌ˠ`, `◌ˤ`, `◌ʰ`, `◌ː`, a segment may use multiple secondary articulations, but those that contrast with each other will give error *c.f. `*[o̟̠]`*)<br>Currently there is no support for X-SAMPA<br>Affricates must have an underscore to bind them | `'s'`, `'t_ɕ'`, `'rʷː'`
`Word` | Sequence of Phones, with the addition of syllable delimiters `.` and morpheme boundary markers `+` | `` `foʊnɒləd_ʒi` ``, `` `soʊ.noʊ` ``, `` `naː.wa+tɬ` ``
`Feature` | Distinctive feature and its quality | `+|long`, `-|LAB`
`Matrix` | Grouping of Features for transformations | `[+|long, -|tense]`
`Rule` | Phonological transformation rules using a combination of Phones, Strings, and Matrices<br>The initial character determines whether it is assimilatory: `S` indicates no assimilation (does not remove any segments), `Af` indicates forward assimilation (removes the following segment), `Ab` indicates backward assimilation (removes the previous segment) | `S |> 't' -> 't_ɬ' // "$" .. [-|high, +|low, -|front, -|back, -|tense]`
`Function` | Basic parameterized anonymous function | `(a, b) => {return a * b;}`

## Syntax

Please note that syntax may change during the course of updates due to the novelty of the language. After the beta is complete the final syntax will have been established.

The syntax is mostly C-style with minor alterations regarding flow segments and usage of semi-colons. All statements must end in a semi-colon (in the vein of C/++ and Java). The exception to this is single-lined scopes, where a semi-colon is not necessary. For example, the function `(a, b) => {return a * b;}` may also be written as `(a, b) => {return a * b}`.

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

Operators are strictly typed however, and thus a variable of type `Number` cannot be added to a variable of type `String`, etc. Explicit conversion is necessary using the following keywords. All keywords are reflexive for their own types, and reversible with the exception of `str` (if `a` is a `Vector`, then `vec word a == a`):

Keyword | Conversion | Values
-|-|-
`str` | Convert to a `String` | `Any`
`vec` | Converts to a `Vector` | `String`, `Matrix`, `Word`, `Dictionary`
`mat` | Converts to a `Matrix` | `Vector` (of Features)
`num` | Converts to a `Number` | `String`
`char` | Converts to a 1-element `String` value of a `Number` | `Number`
`code` | Converts to a `Number` value of a 1-element `String`'s character code | `String`
`word` | Converts to a `Word` | `String`, `Vector`
`feat` | Converts to a `Feature` | `String`
`hash` | Converts to a unique `Number` value for identifying the object or value | `Any`

### Operators

All basic arithmetic operators exist, including `**` for exponent. Any alterations or unique operators are noted below

Operator | Function | Values
-|-|-
`a + b` | Concatenates or adds values<br>In the case of two `Matrix` values, the returned `Matrix` will contain a combination of values that allow for `from` to return a natural class `Vector` encompassing values given by both `Matrix` values | `a` and `b` must be of the same type
`a ?> b` | Returns a `Matrix` of contrastive features between two values | `Phone`
`a from b` | Returns all `Phone` values from `b` that contain features expressed in `a` | `Matrix` and `Vector` (of Phones)
`a >> b` | Transforms `a` by `b` | `Word` and `Rule`, `Phone` and `Matrix`
`a until b` | Creates a `Vector` of values within the range of `a` and `b` (exclusive) | `Number`
`a === b` | Equivalent to `==` in all values except `Structure` values, for which it compares whether the objects are the same instance, overriding any implementation of `equals()` | `Any`
`a !== b` | Correspondent to the above | `Any`

### Objects

Objects are declared using two modifiers, either `static` or `struct`, and then the name of the object, followed by `class`.

Objects declared as `static` cannot be instantiated, they exist as "holders" for methods and values. Objects declared with `struct` however are able to be instantiated with the `new`.

`struct` objects _must_ have a method `init`, which is called during instantiation as the constructor method. Other methods may be overridden:

Method | Arguments | Use
-|-|-
`getString`|None|Returns a `String` representation of the object when the object is called with `str`
`getIndex`|Any single value|Overrides the `Vector` indexing operator `[]`
`getVector`|None|Returns a `Vector` representation of the object when the object is called with `vec`
`getLength`|None|Returns the length of the object when called with `length()`
`getHash`|None|Returns the hashcode of the object when called with `hash`
`equals`|Any single value|Overrides the `==` operator (and by extension, the `!=` operator)

While it is possible to override these methods to return values of any type, that practice may lead to some confusion (for instance, `getList` returning a `Number`).

An example object and its instantiation are as follows:

```sono
struct Point class {
	var x;
	var y;
	init(x, y) => {
		this.x = x;
		this.y = y
	}
	getStr() => {
		return "(" + str x + ", " + str y + ")";
	}
}

var p = new Point(1, 2);
print(p); # Prints "(1, 2)"
```

### Rules

Rule declarations have five components: Type, Search parameter, Transformation parameter, and the two boundary parameters.

```sono
<Type> |> <Search> -> <Transformation> // <Lower Bound> .. <Upper Bound>
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
S |> [+|syl, -|cons] -> [+|nasal] // null .. [-|syl, +|cons, +|nasal];
# `mæn` -> `mæ̃n`

# Vowel nasalized before a nasal consonant, and the consonant is deleted (French)
Af |> [+|syl, -|cons] -> [+|nasal] // null .. [-|syl, +|cons, +|nasal];
# `bɔn` -> `bɔ̃`

# Palatalized /h/ becomes [ç] before a vowel (Japanese)
S |> 'hʲ' -> 'ç' // null .. [+|syl];
# `hʲito` -> `çito`

# Final non-nasal consonant is devoiced (German)
S |> [-|syl, +|cons, +|voice, -|nasal] -> [-|voice] // null .. "#";
# `taːg` -> `taːk`
# `taːgə` -> `taːgə` (Unchanged)

# An epenthetic [ə] is inserted between sequential consonants (Not based on a real language)
S |> null -> 'ə' // [-|syl, +|cons] .. [-|syl, +|cons];
# `asta` -> `asəta`
```

In addition, there are further `Feature` assimilation options. Denoting features with numeric qualities (e.g. `1`, `2`, `3`, ...) creates a variable feature linked to the quality in the `Phone` or `Matrix` that corresponds to that integer read from left to right.

For instance, a rather complicated nasal assimilation rule in Japanese:

```sono
# The moraic nasal assimilates to the place of the following consonant
S |> 'ɴ' -> [2|LAB, 2|round, 2|ld, 2|COR, 2|ant, 2|dist, 2|DOR, 2|high, 2|low, 2|front, 2|back] // null .. [-|syl, +|cons, 2|LAB, 2|round, 2|ld, 2|COR, 2|ant, 2|dist, 2|DOR, 2|high, 2|low, 2|front, 2|back];
```

Place assimilation may be revised in the future using macros for the sake of brevity.

### Functions

Function syntax is expressed in two ways: anonymous or dedicated functions. The only difference in declaration is a lack of identifier with anonymous functions. For a dedicated function, one identifier can only be declared once (with the exception of prototypical functions) within a scope, and it will only exist within that scope. Anonymous functions can server as variables, callbacks, and generally more mutable values.

```sono
# Dedicated
f(x) => {
	return x * x;
}

# Anonymous
f = (x) => {
	return x * x;
}
```

The operator `=>` is used in both to signify the function body. Within the function body, there are two possible keywords for returning values. The keyword `return` will return the value by _value_, and does not affect the value referred to by the function:

```sono
var a = 0;

f() => {
	return a;
}

var b = f(); # 0
f() = 10; # 10
a; # 0
```

On the other hand, the keyword `refer` designates the opposite, it passes the value by reference:

```sono
var a = 0;

f() => {
	refer a;
}

var b = f(); # 0
f() = 10; # 10
a; # 10
```

Note that using `f()` returns the reference to the variable itself and thus when using `=` sets the value of the variable.

All functions are first-class values and can be used in any situation regarding variables.

### Loops

The for-loop syntax and while-loop syntax use the same keyword `do`, with the for-loop having an iteration keyword `in` indicating its span.

```sono
i in {0 until 10} do {
	# ....
}

true do {
	# ....
}
```

The iteration variable is mutable and if the loop happens to be iterating over a Vector variable, the contents may be altered:

```sono
var nums = {0, 1, 2, 3, 4};

print(nums); # {0, 1, 2, 3, 4}

i in nums do {
	i += 1;
}

print(nums); # {1, 2, 3, 4, 5}
```

### If-Then-Else

If-then-else sequences use `then` and `else`:

```sono
i == 1 then {
	# ....
} else {
	# ....
}
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
}
```

A final `else` statement is not required.

### Switch Statement

As a great man once said, a `switch` statement is always necessary. Switch statements can be much more efficient than long else-then sequences due to hashing the input and comparing it against values directly rather than going sequentially through all the if-statements.

The syntax is relatively similar to all other code blocks in the language, following the subsequent pattern:

```sono
KEY switch {
	0 goto {
		# ....
	}
	1 goto {
		# ....
	}
	# ....
}
```

To implement the `default` case, the keyword `else` is optionally used. A `default` case is not required, and omission of one just indicates that the switch statement will not be executed if the key value does not match any elements.

```sono
KEY switch {
	# ....
} else {
	# ....
}
```

### Try-Catch

Try-catch statements use the following form:

```sono
try {
	# ....
} catch {
	# ....
}
```

Within the `catch` statement, the variable `_e` is generated, indicating the message of the error.

The `catch` clause is optional.

### Loading External Files

The `load` keyword will search for a file of a given `String` key and load its content during compilation. It will search in the running directory first, and then proceed to the application's [`bin/lib`](bin/lib) directory. See [`examples`](examples) for usage samples.

## External Libraries

External libraries can be compiled as `.jar` files and imported with the `import` keyword. These `.jar` files must be built with the [`build-lib`](build-lib.bat) script and extend [`main.base.Library`](src/main/base/Library.java). In addition, they must be compiled in the package [`ext`](src/ext).

The commands of external libraries are activated using the keyword `_OUTER_CALL_` which requests a predefined function. The first parameter of the keyword is the `String` key of the function, while the second is the input of any type. See [`ext`](src/ext) for examples.

## Various Regards

*To* Andy Cline *for invaluable aid in setting up SSL*

*To* [Dr. Kevin Tang](https://github.com/tang-kevin) *for providing input towards the project's development and goals*