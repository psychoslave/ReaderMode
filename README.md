# ReaderMode

An IntelliJ-platform plugin that displays source code in a **reader view**:
tokens are visually replaced by more natural words and forms — without touching
the underlying source file.

Toggled via **View → Reader Mode**.  
Tested on PhpStorm 2024.3; the plugin also registers for Java and Kotlin
(optional dependencies, loaded only when the respective language plugin is
present).

---

## Features

### Compound identifiers → middot form

Any identifier written in a common multi-word naming convention is replaced
by its lowercase, middle-dot–separated reading:

| Source token       | Reader view        |
|--------------------|--------------------|
| `camelCase`        | `camel·case`       |
| `PascalCase`       | `pascal·case`      |
| `snake_case`       | `snake·case`       |
| `SCREAMING_SNAKE`  | `screaming·snake`  |
| `kebab-case`       | `kebab·case`       |
| `HTMLParser`       | `html·parser`      |
| `parseHTML`        | `parse·html`       |

The middle dot (U+00B7 `·`) was chosen because it is a genuine word-separator
glyph (used in Catalan orthography, ancient Greek manuscripts, etc.), visually
lighter than a hyphen or underscore, and unambiguous in code contexts.

### Bracket tokens → English words

Each bracket or brace character is replaced by a short English word:

| Token | Word  | Rhyme pair |
|-------|-------|------------|
| `(`   | `do`  | ↕ both end in **-o** |
| `)`   | `go`  | ↕ |
| `{`   | `tap` | ↕ both end in **-p** |
| `}`   | `hop` | ↕ |

### Member-access operator → "whose"

The `->` operator (PHP object member access) is rendered as **whose**:

| Token | Word    |
|-------|---------|
| `->`  | `whose` |

**Rationale — semantic ownership.**  
`->` denotes that a property or method belongs to an object. *Whose* is the
English possessive interrogative — the exact word we use to ask about
ownership. `$cart->total` reads *"cart whose total"*, which maps cleanly
onto the mental model: we are accessing something that the object owns.

**Rationale — prosodic flow.**  
The word slots naturally into spoken code:

> `$variable->methodName()` → *"variable **whose** method·name **do** **go**"*

Possession and invocation read as a natural phrase rather than a symbol
interrupt.

### Variable sigil → "see"

The `$` prefix on PHP variables is rendered as **see**:

| Source           | Reader view           |
|------------------|-----------------------|
| `$manager`       | `see manager`         |
| `$someClass`     | `see some·class`      |
| `$this`          | `see this`            |

**Rationale — deictic interjection.**  
In English, "See!" is used to point at or draw attention to something — the
exact gesture a sigil performs: it marks a name as a reference to a value.
`$manager` reads *"see manager"*: look here — manager.

**Rationale — locus noun.**  
A "see" (ecclesiastical) is the seat where autonomous authority is exercised.
By metaphor: a variable is the locus at which a value resides and from which
it acts — the place the name governs.

**Phonetic fit.**  
Like every other structural word in the vocabulary, "see" starts with **s** —
echoing the struck-S shape of `$` itself. It is a complete English word with
no apocope, three letters, and slots cleanly into spoken code:

> `$this->methodName()` → *"**see** this **whose** method·name **do** **go**"*

The possessive chain reads as a natural English noun phrase.

#### Word-choice rationale

The four bracket words were chosen to satisfy several constraints at once:

**Phonetic symmetry within each pair.**  
`do` / `go` share the `-o` ending; `tap` / `hop` share the `-p` ending.
Open and close brackets are thus audibly paired, mirroring the visual
pairing they form on the page.

**Short and percussive.**  
Each word is a single syllable. Deeply nested code like `f(g(h()))` reads
`f do g do h do go go go` — it scans and even has a rhythmic quality when
spoken aloud. Repetitions roll off the tongue: *tap tap tap … hop hop hop.*

**Interpretable as action words.**  
`do` and `go` are everyday imperatives. `tap` evokes an interjection or
onomatopoeia (a light knock on a surface); `hop` evokes a small jump or skip.
This gives each bracket a mild semantic flavour consistent with what brackets
actually *do* in code:
- `(…)` — you *do* something, then *go* on;
- `{…}` — you *tap* into a block, then *hop* out.

**Prosody when vocalised.**  
A function call like `__construct(SomeClass $instance)` reads aloud as:
> *"underscore underscore construct **do** some·class **see** instance **go**"*

A class body opening `SomeClass {` reads as:
> *"some·class **tap**"*

### Spacing

Bracket and operator folds are padded automatically so that no token ever
visually glues to its neighbour:

- a **leading space** is inserted when the preceding source character is not
  whitespace;
- a **trailing space** is inserted when the following source character is
  neither whitespace nor another bracket (which would contribute its own
  leading space, avoiding doubles).

Result for `__construct(SomeClass $instance)`:

```
__construct do some·class see instance go
```

---

## How it works

The plugin uses IntelliJ's **code-folding** API.  A fold region replaces a
source range with a placeholder string; expanding the fold restores the
original text.  No file is ever written to.

One fold is created per token:

```
Source:   makeSomething   (            arguments          )
Folds:    [make·something] [ do ]  [arguments] [ go]
Reads:     make·something   do     arguments    go
```

The folding builder (`ReaderModeFoldingBuilder`) is a single-pass
`PsiRecursiveElementVisitor` over all leaf PSI elements.  It is registered as
a `lang.foldingBuilder` extension for each supported language via optional
`<depends>` entries in `plugin.xml`, so it degrades gracefully when a language
plugin is absent.

State (enabled / disabled) is persisted across IDE restarts by
`ReaderModeService`, an application-level `PersistentStateComponent`.

The **View → Reader Mode** action immediately collapses or expands all
reader-mode folds in every open editor, so the switch feels instant.

---

## Project structure

```
src/main/kotlin/com/example/readermode/
  BracketRenderer.kt        structural token → word map, padding helpers
  MiddotConverter.kt        identifier word-splitting (regex-based)
  ReaderModeFoldingBuilder.kt  PSI visitor, fold descriptor builder
  ReaderModeService.kt      persistent enabled/disabled toggle
  ToggleReaderModeAction.kt View-menu action

src/main/resources/META-INF/
  plugin.xml                core descriptor
  readermode-java.xml       optional: registers for language="JAVA"
  readermode-kotlin.xml     optional: registers for language="kotlin"
  readermode-php.xml        optional: registers for language="PHP"
```

---

## Building & running

Requirements: JDK 17, Gradle 9 (wrapper included), `mise` for JDK management.

```bash
# Run a sandboxed PhpStorm instance with the plugin installed
./gradlew runIde

# Run unit tests
./gradlew test
```
